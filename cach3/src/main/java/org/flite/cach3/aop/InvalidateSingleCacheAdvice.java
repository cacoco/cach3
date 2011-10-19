package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flite.cach3.annotations.InvalidateSingleCache;
import org.flite.cach3.api.*;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.*;

/**
Copyright (c) 2011 Flite, Inc

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
public class InvalidateSingleCacheAdvice extends CacheBase {
    private static final Log LOG = LogFactory.getLog(InvalidateSingleCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.InvalidateSingleCache)")
    public void invalidateSingle() {}

    @Around("invalidateSingle()")
    public Object cacheInvalidateSingle(final ProceedingJoinPoint pjp) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return pjp.proceed();
        }

        final MemcachedClientIF cache = getMemcachedClient();
        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        String cacheKey = null;
        Object keyObject = null;
        final AnnotationData annotationData;
        try {
            final Method methodToCache = getMethodToCache(pjp);
            final InvalidateSingleCache annotation = methodToCache.getAnnotation(InvalidateSingleCache.class);
            annotationData =
                    AnnotationDataBuilder.buildAnnotationData(annotation,
                            InvalidateSingleCache.class,
                            methodToCache.getName());
            if (annotationData.getKeyIndex() > -1) {
                keyObject = getIndexObject(annotationData.getKeyIndex(), pjp, methodToCache);
                final Method keyMethod = getKeyMethod(keyObject);
                final String objectId = generateObjectId(keyMethod, keyObject);
                cacheKey = buildCacheKey(objectId, annotationData);
            }
        } catch (Throwable ex) {
            LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        try {
            // If we have a -1 key index, then build the cacheKey now.
            if (annotationData.getKeyIndex() == -1) {
                keyObject = result;
                final Method keyMethod = getKeyMethod(result);
                final String objectId = generateObjectId(keyMethod, result);
                cacheKey = buildCacheKey(objectId, annotationData);
            }
            if (cacheKey == null || cacheKey.trim().length() == 0) {
                throw new InvalidParameterException("Unable to find a cache key");
            }
            cache.delete(cacheKey);

            // Notify the observers that a cache interaction happened.
            final List<InvalidateSingleCacheListener> listeners = getPertinentListeners(InvalidateSingleCacheListener.class,annotationData.getNamespace());
            if (listeners != null && !listeners.isEmpty()) {
                for (final InvalidateSingleCacheListener listener : listeners) {
                    try {
                        listener.triggeredInvalidateSingleCache(annotationData.getNamespace(), keyObject);
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
