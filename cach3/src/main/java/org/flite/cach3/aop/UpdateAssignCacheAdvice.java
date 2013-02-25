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
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.annotations.groups.UpdateAssignCaches;
import org.flite.cach3.api.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.*;
import java.util.*;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE / 2)
public class UpdateAssignCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateAssignCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.UpdateAssignCache)")
    public void updateAssign() {}

    @AfterReturning(pointcut="updateAssign()", returning="retVal")
    public Object cacheUpdateAssign(final JoinPoint jp, final Object retVal) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return retVal;
        }

        final MemcachedClientIF cache = getMemcachedClient();
        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        try {
            final Method methodToCache = getMethodToCache(jp);
            final UpdateAssignCache annotation = methodToCache.getAnnotation(UpdateAssignCache.class);
            final AnnotationData annotationData = AnnotationDataBuilder.buildAnnotationData(annotation,
                            UpdateAssignCache.class,
                            methodToCache.getName(),
                            getJitterDefault());
            final String cacheKey = buildCacheKey(annotationData.getAssignedKey(), annotationData);
            final Object dataObject = annotationData.getDataIndex() == -1
                    ? retVal
                    : getIndexObject(annotationData.getDataIndex(), jp.getArgs(), methodToCache.toString());
            final Object submission = (dataObject == null) ? new PertinentNegativeNull() : dataObject;
            boolean cacheable = true;
            if (submission instanceof CacheConditionally) {
                cacheable = ((CacheConditionally) submission).isCacheable();
            }
            if (cacheable) {
			    cache.set(cacheKey, annotationData.getJitteredExpiration(), submission);
            }

            // Notify the observers that a cache interaction happened.
            final List<UpdateAssignCacheListener> listeners = getPertinentListeners(UpdateAssignCacheListener.class,annotationData.getNamespace());
            if (listeners != null && !listeners.isEmpty()) {
                for (final UpdateAssignCacheListener listener : listeners) {
                    try {
                        listener.triggeredUpdateAssignCache(annotationData.getNamespace(), annotationData.getAssignedKey(), dataObject, retVal, jp.getArgs());
                    } catch (Exception ex) {
                        LOG.warn("Problem when triggering a listener.", ex);
                    }
                }
            }
		} catch (Exception ex) {
			LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
		}

        return retVal;
    }


    @Pointcut("@annotation(org.flite.cach3.annotations.groups.UpdateAssignCaches)")
    public void updateAssigns() {}

    @AfterReturning(pointcut = "updateAssigns()", returning = "retVal")
    public Object cacheUpdateAssigns(final JoinPoint jp, final Object retVal) throws Throwable {
        try {
            doUpdate(jp, retVal);
        } catch (Throwable ex) {
            LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
        }
        return retVal;
    }



    private void doUpdate(final JoinPoint jp, final Object retVal) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return;
        }

        final MemcachedClientIF cache = getMemcachedClient();
        final Method methodToCache = getMethodToCache(jp);
        List<UpdateAssignCache> lAnnotations;

        if (methodToCache.getAnnotation(UpdateAssignCache.class) != null) {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(UpdateAssignCache.class));
        } else {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(UpdateAssignCaches.class).value());
        }

        for (int i = 0; i < lAnnotations.size(); i++) {
            try {
                // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
                // but do not let it surface up past the AOP injection itself.

                final AnnotationData annotationData = AnnotationDataBuilder.buildAnnotationData(lAnnotations.get(i),
                                UpdateAssignCache.class,
                                methodToCache.getName(),
                                getJitterDefault());
                final String cacheKey = buildCacheKey(annotationData.getAssignedKey(), annotationData);
                final Object dataObject = annotationData.getDataIndex() == -1
                        ? retVal
                        : getIndexObject(annotationData.getDataIndex(), jp.getArgs(), methodToCache.toString());
                final Object submission = (dataObject == null) ? new PertinentNegativeNull() : dataObject;
                cache.set(cacheKey, annotationData.getJitteredExpiration(), submission);

                // Notify the observers that a cache interaction happened.
                final List<UpdateAssignCacheListener> listeners = getPertinentListeners(UpdateAssignCacheListener.class,annotationData.getNamespace());
                if (listeners != null && !listeners.isEmpty()) {
                    for (final UpdateAssignCacheListener listener : listeners) {
                        try {
                            listener.triggeredUpdateAssignCache(annotationData.getNamespace(), annotationData.getAssignedKey(), dataObject, retVal, jp.getArgs());
                        } catch (Exception ex) {
                            LOG.warn("Problem when triggering a listener.", ex);
                        }
                    }
                }
            } catch (Exception ex) {
                LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
            }
        }

    }



}
