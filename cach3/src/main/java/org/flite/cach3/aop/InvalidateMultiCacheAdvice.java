package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.apache.commons.logging.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
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
public class InvalidateMultiCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(InvalidateMultiCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.InvalidateMultiCache)")
    public void invalidateMulti() {}

    @AfterReturning(pointcut="invalidateMulti()", returning="retVal")
    public Object cacheInvalidateMulti(final JoinPoint jp, final Object retVal) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return retVal;
        }

        final MemcachedClientIF cache = getMemcachedClient();
        // For Invalidate*Cache, an AfterReturning aspect is fine. We will only apply our caching
        // after the underlying method completes successfully, and we will have the same
        // access to the method params.

        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        try {
            final Method methodToCache = getMethodToCache(jp);
            final InvalidateMultiCache annotation = methodToCache.getAnnotation(InvalidateMultiCache.class);
            final AnnotationData annotationData =
                    AnnotationDataBuilder.buildAnnotationData(annotation,
                            InvalidateMultiCache.class,
                            methodToCache.getName());
            final List<Object> keyObjects = (List<Object>) getIndexObject(annotationData.getKeyIndex(), retVal, jp.getArgs(), methodToCache.toString());
            final List<String> baseKeys = getBaseKeys(keyObjects, annotationData, retVal, jp.getArgs());
            for (final String base : baseKeys) {
                final String cacheKey = buildCacheKey(base, annotationData);
                cache.delete(cacheKey);
            }

            // Notify the observers that a cache interaction happened.
            final List<InvalidateMultiCacheListener> listeners = getPertinentListeners(InvalidateMultiCacheListener.class,annotationData.getNamespace());
            if (listeners != null && !listeners.isEmpty()) {
                for (final InvalidateMultiCacheListener listener : listeners) {
                    try {
                        listener.triggeredInvalidateMultiCache(annotationData.getNamespace(), annotationData.getKeyPrefix(), baseKeys, retVal, jp.getArgs());
                    } catch (Exception ex) {
                        LOG.warn("Problem when triggering a listener.", ex);
                    }
                }
            }
        } catch (Throwable ex) {
            LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
        }
        return retVal;
    }

}
