package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.apache.commons.logging.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.flite.cach3.exceptions.*;

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
    private static final Log LOG = LogFactory.getLog(InvalidateMultiCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.InvalidateMultiCache)")
    public void invalidateMulti() {}

    @Around("invalidateMulti()")
    public Object cacheInvalidateMulti(final ProceedingJoinPoint pjp) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return pjp.proceed();
        }

        final MemcachedClientIF cache = getMemcachedClient();
        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        List<String> cacheKeys = null;
        List<Object> keyObjects = null;
        final AnnotationData annotationData;
        final String methodDescription;
        try {
            final Method methodToCache = getMethodToCache(pjp);
            methodDescription = methodToCache.toString();
            final InvalidateMultiCache annotation = methodToCache.getAnnotation(InvalidateMultiCache.class);
            annotationData =
                    AnnotationDataBuilder.buildAnnotationData(annotation,
                            InvalidateMultiCache.class,
                            methodToCache.getName());
            if (annotationData.getKeyIndex() > -1) {
                final Object keyObject = getIndexObject(annotationData.getKeyIndex(), pjp, methodToCache);
                keyObjects = convertToKeyObjects(keyObject, annotationData.getKeyIndex(), methodDescription);
                cacheKeys = getCacheKeys(keyObjects, annotationData);
            }
        } catch (Throwable ex) {
            LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            return pjp.proceed();
        }

        final Object result = pjp.proceed();

        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        try {
            // If we have a -1 key index, then build the cacheKeys now.
            if (annotationData.getKeyIndex() == -1) {
                keyObjects = convertToKeyObjects(result, annotationData.getKeyIndex(), methodDescription);
                cacheKeys = getCacheKeys(keyObjects, annotationData);
            }
            if (cacheKeys != null && cacheKeys.size() > 0) {
                for (final String key : cacheKeys) {
                    if (key != null && key.trim().length() > 0) {
                        cache.delete(key);
                    }
                }
            }

            // Notify the observers that a cache interaction happened.
            final List<InvalidateMultiCacheListener> listeners = getPertinentListeners(InvalidateMultiCacheListener.class,annotationData.getNamespace());
            if (listeners != null && !listeners.isEmpty()) {
                for (final InvalidateMultiCacheListener listener : listeners) {
                    try {
                        listener.triggeredInvalidateMultiCache(annotationData.getNamespace(), annotationData.getKeyPrefix(), keyObjects);
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

    protected List<Object> convertToKeyObjects(final Object keyObject,
                                               final int keyIndex,
                                               final String methodDescription) throws Exception {
        if (verifyTypeIsList(keyObject.getClass())) {
            return (List<Object>) keyObject;
        }
        throw new InvalidAnnotationException(String.format(
                "The parameter object found at dataIndex [%s] is not a [%s]. " +
                "[%s] does not fulfill the requirements.",
                keyIndex,
                List.class.getName(),
                methodDescription
        ));
    }
}
