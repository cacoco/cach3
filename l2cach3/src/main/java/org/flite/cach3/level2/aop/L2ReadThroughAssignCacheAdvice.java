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

package org.flite.cach3.level2.aop;

import com.google.common.collect.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.aop.*;
import org.flite.cach3.api.*;
import org.flite.cach3.level2.annotations.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

@Aspect
@Order((Ordered.HIGHEST_PRECEDENCE / 2) - 10)
public class L2ReadThroughAssignCacheAdvice extends L2CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(L2ReadThroughAssignCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.level2.annotations.L2ReadThroughAssignCache)")
    public void getL2SingleAssign() {}

    @Around("getL2SingleAssign()")
    public Object cacheL2Assign(final ProceedingJoinPoint pjp) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return pjp.proceed();
        }

        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        final String cacheKey;
        final AnnotationInfo info;
        try {
            final Method methodToCache = getMethodToCache(pjp);
            final L2ReadThroughAssignCache annotation = methodToCache.getAnnotation(L2ReadThroughAssignCache.class);
            info = getAnnotationInfo(annotation, methodToCache.getName());
            cacheKey = buildCacheKey(info.<String>getAsType(AnnotationTypes.ASSIGN_KEY, ""),
                    info.<String>getAsType(AnnotationTypes.NAMESPACE, ""),
                    info.<String>getAsType(AnnotationTypes.KEY_PREFIX, ""));
            final Map<String, Object> results = getCache().getBulk(Arrays.asList(cacheKey), info.<Duration>getAsType(AnnotationTypes.WINDOW, null));
            final Object result = results == null ? null : results.get(cacheKey);
            if (result != null) {
//                LOG.debug("Cache hit for key " + cacheKey);
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
                getCache().setBulk(ImmutableMap.of(cacheKey, submission), info.<Duration>getAsType(AnnotationTypes.WINDOW, null));
            }
        } catch (Throwable ex) {
            LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
        }
        return result;
    }

    static AnnotationInfo getAnnotationInfo(final L2ReadThroughAssignCache annotation, final String targetMethodName) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    L2ReadThroughAssignCache.class.getName()
            ));
        }

        final String assignKey = annotation.assignedKey();
        if (AnnotationConstants.DEFAULT_STRING.equals(assignKey)
                || assignKey == null
                || assignKey.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "AssignedKey for annotation [%s] must be defined on [%s]",
                    L2ReadThroughAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.AssignKey(assignKey));

        final Duration window = annotation.window();
        if (window == Duration.UNDEFINED) {
            throw new InvalidParameterException(String.format(
                    "Window for annotation [%s] must be a defined value on [%s]",
                    L2ReadThroughAssignCache.class.getName(),
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
                    L2ReadThroughAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.Namespace(namespace));

        return result;
    }
}
