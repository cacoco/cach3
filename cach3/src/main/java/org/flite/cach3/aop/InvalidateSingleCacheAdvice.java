package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.annotations.groups.InvalidateSingleCaches;
import org.flite.cach3.api.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Copyright (c) 2011 Flite, Inc
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE / 2)
public class InvalidateSingleCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(InvalidateSingleCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.InvalidateSingleCache)")
    public void invalidateSingle() {
    }

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
    public void invalidateSingles() {
    }

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
                final AnnotationData annotationData =
                        AnnotationDataBuilder.buildAnnotationData(lAnnotations.get(i),
                                InvalidateSingleCache.class,
                                methodToCache.getName());
                final String baseKey = getBaseKey(annotationData, retVal, jp.getArgs(), methodToCache.toString());
                final String cacheKey = buildCacheKey(baseKey, annotationData);

                LOG.debug("Invalidating cache for key " + cacheKey);
                cache.delete(cacheKey);

                // Notify the observers that a cache interaction happened.
                final List<InvalidateSingleCacheListener> listeners = getPertinentListeners(InvalidateSingleCacheListener.class, annotationData.getNamespace());
                if (listeners != null && !listeners.isEmpty()) {
                    for (final InvalidateSingleCacheListener listener : listeners) {
                        try {
//                        listener.triggeredInvalidateSingleCache(annotationData.getNamespace(), annotationData.getKeyPrefix(), keyObject);
                            listener.triggeredInvalidateSingleCache(annotationData.getNamespace(), annotationData.getKeyPrefix(), baseKey, retVal, jp.getArgs());
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




}
