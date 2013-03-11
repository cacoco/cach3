/*
 * Copyright (c) 2011-2013 Flite, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.flite.cach3.aop;

import com.google.common.collect.*;
import org.apache.commons.lang.*;
import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;

@Aspect
@Order((Ordered.HIGHEST_PRECEDENCE / 2) - 10)
public class L2ReadThroughSingleCacheAdvice extends L2CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(L2ReadThroughSingleCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.L2ReadThroughSingleCache)")
   	public void getSingle() {}

    @Around("getSingle()")
   	public Object cacheSingle(final ProceedingJoinPoint pjp) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
           LOG.debug("Caching is disabled.");
           return pjp.proceed();
        }

   		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
   		// but do not let it surface up past the AOP injection itself.
        final String baseKey;
   		final String cacheKey;
   		final L2ReadThroughSingleCache annotation;
        final AnnotationInfo info;
        final Object[] args = pjp.getArgs();
   		try {
   		    final Method methodToCache = getMethodToCache(pjp);
            annotation = methodToCache.getAnnotation(L2ReadThroughSingleCache.class);
            info = getAnnotationInfo(annotation, methodToCache.getName());
   			baseKey = generateBaseKeySingle(args, info, methodToCache.toString());
            cacheKey = buildCacheKey(baseKey,
                    info.<String>getAsType(AnnotationTypes.NAMESPACE, null),
                    info.<String>getAsType(AnnotationTypes.KEY_PREFIX, null));

            final Map<String, Object> results = getCache().getBulk(Arrays.asList(cacheKey), info.<Duration>getAsType(AnnotationTypes.WINDOW, null));
   			final Object result = results == null ? null : results.get(cacheKey);
   			if (result != null) {
//   				LOG.debug("Cache hit for key " + cacheKey);
   				return (result instanceof PertinentNegativeNull) ? null : result;
   			}
   		} catch (Throwable ex) {
   			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
   			return pjp.proceed();
   		}

   		final Object result = pjp.proceed();

   		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
   		// but do not let it surface up past the AOP injection itself.
   		try {
   			final Object submission = (result == null) ? new PertinentNegativeNull() : result;
            boolean cacheable = true;
            if (submission instanceof CacheConditionally) {
               cacheable = ((CacheConditionally) submission).isCacheable();
            }
            if (cacheable) {
                getCache().setBulk(ImmutableMap.of(cacheKey, submission),  info.<Duration>getAsType(AnnotationTypes.WINDOW, null));
            }
   		} catch (Throwable ex) {
   			LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
   		}
   		return result;
   	}

    protected String getObjectId(final Object keyObject) throws Exception {
   		final Method keyMethod = getKeyMethod(keyObject);
   		return generateObjectId(keyMethod, keyObject);
   	}

    protected String generateBaseKeySingle(final Object[] args,
                                           final AnnotationInfo info,
                                           final String methodString) throws Exception {
        final String keyTemplate = info.<String>getAsType(AnnotationTypes.KEY_TEMPLATE, null);
        if (StringUtils.isBlank(keyTemplate)) {
            return getObjectId(CacheBase.getIndexObject(info.<Integer>getAsType(AnnotationTypes.KEY_INDEX, Integer.MIN_VALUE), args, methodString));
        }

        final VelocityContext context = factory.getNewExtendedContext();
        context.put("args", args);

        final StringWriter writer = new StringWriter(250);
        Velocity.evaluate(context, writer, this.getClass().getSimpleName(), keyTemplate);
        final String result = writer.toString();
        if (keyTemplate.equals(result)) { throw new InvalidParameterException("Calculated key is equal to the velocityTemplate."); }
        return result;
    }

    static AnnotationInfo getAnnotationInfo(final L2ReadThroughSingleCache annotation, final String targetMethodName) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    L2ReadThroughSingleCache.class.getName()
            ));
        }

        final String keyPrefix = annotation.keyPrefix();
        if (!AnnotationConstants.DEFAULT_STRING.equals(keyPrefix)
                && keyPrefix != null
                && keyPrefix.length() > 0) {
            result.add(new AnnotationTypes.KeyPrefix(keyPrefix));
        }

        final Integer keyIndex = annotation.keyIndex();
        final boolean keyIndexDefined = keyIndex >= -1;

        final String keyTemplate = annotation.keyTemplate();
        final boolean keyTemplateDefined = !AnnotationConstants.DEFAULT_STRING.equals(keyTemplate)
                && StringUtils.isNotBlank(keyTemplate);

        if (keyIndexDefined == keyTemplateDefined) {
            throw new InvalidParameterException(String.format(
                    "Exactly one of [keyIndex,keyTemplate] must be defined for annotation [%s] on [%s]",
                    L2ReadThroughMultiCache.class.getName(),
                    targetMethodName
            ));
        }

        if (keyIndexDefined) {
            if (keyIndex < 0) {
                throw new InvalidParameterException(String.format(
                        "KeyIndex for annotation [%s] must be 0 or greater on [%s]",
                        L2ReadThroughSingleCache.class.getName(),
                        targetMethodName
                ));
            }
            result.add(new AnnotationTypes.KeyIndex(keyIndex));
        }

        if (keyTemplateDefined) {
            result.add(new AnnotationTypes.KeyTemplate(keyTemplate));
        }

        final Duration window = annotation.window();
        if (window == Duration.UNDEFINED) {
            throw new InvalidParameterException(String.format(
                    "Window for annotation [%s] must be a defined value on [%s]",
                    L2ReadThroughSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.Window(window));

        final String namespace = annotation.namespace();
        if (AnnotationConstants.DEFAULT_STRING.equals(namespace)
                || namespace == null
                || namespace.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "Namespace for annotation [%s] must be defined on [%s]",
                    L2ReadThroughSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.Namespace(namespace));

        return result;
    }
}