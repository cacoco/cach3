package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.flite.cach3.annotations.ReadThroughAssignCache;
import org.flite.cach3.api.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.Method;
import java.util.*;

/**
Copyright (c) 2011-2012 Flite, Inc

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE / 2)
public class ReadThroughAssignCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(ReadThroughAssignCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.ReadThroughAssignCache)")
    public void getSingleAssign() {}

    @Around("getSingleAssign()")
    public Object cacheAssign(final ProceedingJoinPoint pjp) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return pjp.proceed();
        }

        final MemcachedClientIF cache = getMemcachedClient();
        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        final String cacheKey;
        final AnnotationData annotationData;
        try {
            final Method methodToCache = getMethodToCache(pjp);
            final ReadThroughAssignCache annotation = methodToCache.getAnnotation(ReadThroughAssignCache.class);
            annotationData =
                    AnnotationDataBuilder.buildAnnotationData(annotation,
                            ReadThroughAssignCache.class,
                            methodToCache.getName());
            cacheKey = buildCacheKey(annotationData.getAssignedKey(), annotationData);
            final Object result = cache.get(cacheKey);
            if (result != null) {
                LOG.debug("Cache hit for key " + cacheKey);
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
            cache.set(cacheKey, annotationData.getExpiration(), submission);

            // Notify the observers that a cache interaction happened.
            final List<ReadThroughAssignCacheListener> listeners = getPertinentListeners(ReadThroughAssignCacheListener.class,annotationData.getNamespace());
            if (listeners != null && !listeners.isEmpty()) {
                for (final ReadThroughAssignCacheListener listener : listeners) {
                    try {
                        listener.triggeredReadThroughAssignCache(annotationData.getNamespace(), annotationData.getAssignedKey(), result, pjp.getArgs());
                    } catch (Exception ex) {
                        LOG.warn("Problem when triggering a listener.", ex);
                    }
                }
            }
        } catch (Throwable ex) {
            LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
        }
        return result;
    }
}
