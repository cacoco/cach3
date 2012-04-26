package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.annotations.groups.InvalidateAssignCaches;
import org.flite.cach3.api.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.*;
import java.security.*;
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
public class InvalidateAssignCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(InvalidateAssignCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.InvalidateAssignCache)")
    public void invalidateAssign() {}

    @AfterReturning(pointcut="invalidateAssign()", returning="retVal")
    public Object cacheInvalidateAssign(final JoinPoint jp, final Object retVal) throws Throwable {
        try {
            doInvalidate(jp, retVal);
        } catch (Throwable ex) {
            LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
        }
        return retVal;
    }



    @Pointcut("@annotation(org.flite.cach3.annotations.groups.InvalidateAssignCaches)")
    public void invalidateAssigns() {}

    @AfterReturning(pointcut = "invalidateAssigns()", returning = "retVal")
    public Object cacheInvalidateAssigns(final JoinPoint jp, final Object retVal) throws Throwable {
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
        List<InvalidateAssignCache> lAnnotations;

        if (methodToCache.getAnnotation(InvalidateAssignCache.class) != null) {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(InvalidateAssignCache.class));
        } else {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(InvalidateAssignCaches.class).value());
        }

        for (int i = 0; i < lAnnotations.size(); i++) {
            // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
            // but do not let it surface up past the AOP injection itself.
            try {
                final AnnotationData annotationData =
                        AnnotationDataBuilder.buildAnnotationData(lAnnotations.get(i),
                                InvalidateAssignCache.class,
                                methodToCache.getName());

                final String cacheKey = buildCacheKey(annotationData.getAssignedKey(), annotationData);
                if (cacheKey == null || cacheKey.trim().length() == 0) {
                    throw new InvalidParameterException("Unable to find a cache key");
                }
                cache.delete(cacheKey);

                // Notify the observers that a cache interaction happened.
                final List<InvalidateAssignCacheListener> listeners = getPertinentListeners(InvalidateAssignCacheListener.class, annotationData.getNamespace());
                if (listeners != null && !listeners.isEmpty()) {
                    for (final InvalidateAssignCacheListener listener : listeners) {
                        try {
                            listener.triggeredInvalidateAssignCache(annotationData.getNamespace(), annotationData.getAssignedKey(), retVal, jp.getArgs());
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
