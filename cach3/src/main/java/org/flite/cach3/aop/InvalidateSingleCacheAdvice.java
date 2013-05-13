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

import net.spy.memcached.*;
import org.apache.commons.lang.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.annotations.groups.InvalidateSingleCaches;
import org.flite.cach3.api.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE / 2)
public class InvalidateSingleCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(InvalidateSingleCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.InvalidateSingleCache)")
    public void invalidateSingle() { }

    // For Invalidate*Cache, an AfterReturning aspect is fine. We will only apply our caching
    // after the underlying method completes successfully, and we will have the same
    // access to the method params.

    // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
    // but do not let it surface up past the AOP injection itself.

    @AfterReturning(pointcut = "invalidateSingle()", returning = "retVal")
    public Object cacheInvalidateSingle(final JoinPoint jp, final Object retVal) throws Throwable {
        try {
            doInvalidate(jp, retVal);
        } catch (Throwable ex) {
            LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
        }
        return retVal;
    }

    @Pointcut("@annotation(org.flite.cach3.annotations.groups.InvalidateSingleCaches)")
    public void invalidateSingles() { }

    @AfterReturning(pointcut = "invalidateSingles()", returning = "retVal")
    public Object cacheInvalidateSingles(final JoinPoint jp, final Object retVal) throws Throwable {
        try {
            doInvalidate(jp, retVal);
        } catch (Throwable ex) {
            LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
        }
        return retVal;
    }


    private void doInvalidate(final JoinPoint jp, final Object retVal) throws Throwable {
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return;
        }

        final MemcachedClientIF cache = getMemcachedClient();
        final Method methodToCache = getMethodToCache(jp);
        List<InvalidateSingleCache> lAnnotations;

        if (methodToCache.getAnnotation(InvalidateSingleCache.class) != null) {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(InvalidateSingleCache.class));
        } else {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(InvalidateSingleCaches.class).value());
        }

        for (int i = 0; i < lAnnotations.size(); i++) {
            // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
            // but do not let it surface up past the AOP injection itself.
            try {
                final AnnotationInfo info = getAnnotationInfo(lAnnotations.get(i), methodToCache.getName());
                final String baseKey = getBaseKey(
                        info.getAsString(AType.KEY_TEMPLATE),
                        info.getAsInteger(AType.KEY_INDEX, null),
                        retVal,
                        jp.getArgs(),
                        methodToCache.toString(),
                        factory,
                        methodStore);
                final String cacheKey = buildCacheKey(baseKey, info.getAsString(AType.NAMESPACE), info.getAsString(AType.KEY_PREFIX));

                LOG.debug("Invalidating cache for key " + cacheKey);
                cache.delete(cacheKey);

                // Notify the observers that a cache interaction happened.
                final List<InvalidateSingleCacheListener> listeners = getPertinentListeners(InvalidateSingleCacheListener.class, info.getAsString(AType.NAMESPACE));
                if (listeners != null && !listeners.isEmpty()) {
                    for (final InvalidateSingleCacheListener listener : listeners) {
                        try {
                            listener.triggeredInvalidateSingleCache(info.getAsString(AType.NAMESPACE), info.getAsString(AType.KEY_PREFIX, null), baseKey, retVal, jp.getArgs());
                        } catch (Exception ex) {
                            LOG.warn("Problem when triggering a listener.", ex);
                        }
                    }
                }
            } catch (Throwable ex) {
                LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
            }
        }
    }

    /*default*/ static AnnotationInfo getAnnotationInfo(final InvalidateSingleCache annotation, final String targetMethodName) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    InvalidateSingleCache.class.getName()
            ));
        }

        final String namespace = annotation.namespace();
        if (AnnotationConstants.DEFAULT_STRING.equals(namespace)
                || namespace == null
                || namespace.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "Namespace for annotation [%s] must be defined on [%s]",
                    InvalidateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Namespace(namespace));

        final String keyPrefix = annotation.keyPrefix();
        if (!AnnotationConstants.DEFAULT_STRING.equals(keyPrefix)) {
            if (StringUtils.isBlank(keyPrefix)) {
                throw new InvalidParameterException(String.format(
                        "KeyPrefix for annotation [%s] must not be defined as an empty string on [%s]",
                        InvalidateSingleCache.class.getName(),
                        targetMethodName
                ));
            }
            result.add(new AType.KeyPrefix(keyPrefix));
        }

        final Integer keyIndex = annotation.keyIndex();
        if (keyIndex != AnnotationConstants.DEFAULT_KEY_INDEX && keyIndex < -1) {
            throw new InvalidParameterException(String.format(
                    "KeyIndex for annotation [%s] must be -1 or greater on [%s]",
                    InvalidateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        final boolean keyIndexDefined = keyIndex >= -1;

        final String keyTemplate = annotation.keyTemplate();
        if (StringUtils.isBlank(keyTemplate)) {
            throw new InvalidParameterException(String.format(
                    "KeyTemplate for annotation [%s] must not be defined as an empty string on [%s]",
                    InvalidateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        final boolean keyTemplateDefined = !AnnotationConstants.DEFAULT_STRING.equals(keyTemplate);

        if (keyIndexDefined == keyTemplateDefined) {
            throw new InvalidParameterException(String.format(
                    "Exactly one of [keyIndex,keyTemplate] must be defined for annotation [%s] on [%s]",
                    InvalidateSingleCache.class.getName(),
                    targetMethodName
            ));
        }

        if (keyIndexDefined) {
            result.add(new AType.KeyIndex(keyIndex));
        }

        if (keyTemplateDefined) {
            result.add(new AType.KeyTemplate(keyTemplate));
        }

        return result;
    }


}
