package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.apache.commons.logging.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.annotations.groups.UpdateMultiCaches;
import org.flite.cach3.api.*;
import org.flite.cach3.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

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
@Order(Ordered.HIGHEST_PRECEDENCE / 2)
public class UpdateMultiCacheAdvice extends CacheBase {
	private static final Logger LOG = LoggerFactory.getLogger(UpdateMultiCacheAdvice.class);

	@Pointcut("@annotation(org.flite.cach3.annotations.UpdateMultiCache)")
	public void updateMulti() {}

	// For Update*Cache, an AfterReturning aspect is fine. We will only apply our caching
    // after the underlying method completes successfully, and we will have the same
    // access to the method params.

    @AfterReturning(pointcut="updateMulti()", returning="retVal")
	public Object cacheUpdateMulti(final JoinPoint jp, final Object retVal) throws Throwable {
        try {
            doUpdate(jp, retVal);
        } catch (Throwable ex) {
            LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
        }
        return retVal;
	}


    @Pointcut("@annotation(org.flite.cach3.annotations.groups.UpdateMultiCaches)")
	public void updateMultis() {}

	@AfterReturning(pointcut="updateMultis()", returning="retVal")
	public Object cacheUpdateMultis(final JoinPoint jp, final Object retVal) throws Throwable {
        try {
            doUpdate(jp, retVal);
        } catch (Throwable ex) {
            LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
        }
        return retVal;
    }


    private void doUpdate(final JoinPoint jp, final Object retVal) throws Throwable {
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return;
        }

        final MemcachedClientIF cache = getMemcachedClient();
        final Method methodToCache = getMethodToCache(jp);
        List<UpdateMultiCache> lAnnotations;

        if (methodToCache.getAnnotation(UpdateMultiCache.class) != null) {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(UpdateMultiCache.class));
        } else {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(UpdateMultiCaches.class).value());
        }

        for (int i = 0; i < lAnnotations.size(); i++) {
            try {
                // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
                // but do not let it surface up past the AOP injection itself.
                final AnnotationData annotationData =
                        AnnotationDataBuilder.buildAnnotationData(lAnnotations.get(i),
                                UpdateMultiCache.class, methodToCache.getName());
                final List<Object> dataList = (List<Object>) getIndexObject(annotationData.getDataIndex(), retVal, jp.getArgs(), methodToCache.toString());
                final List<Object> keyObjects = getKeyObjects(annotationData.getKeyIndex(), retVal, jp, methodToCache);
                final List<String> baseKeys = getBaseKeys(keyObjects, annotationData, retVal, jp.getArgs());
                final List<String> cacheKeys = new ArrayList<String>(baseKeys.size());
                for (final String base : baseKeys) {
                    cacheKeys.add(buildCacheKey(base, annotationData));
                }
                updateCache(cacheKeys, dataList, methodToCache, annotationData, cache);

                // Notify the observers that a cache interaction happened.
                final List<UpdateMultiCacheListener> listeners = getPertinentListeners(UpdateMultiCacheListener.class,annotationData.getNamespace());
                if (listeners != null && !listeners.isEmpty()) {
                    for (final UpdateMultiCacheListener listener : listeners) {
                        try {
                            listener.triggeredUpdateMultiCache(annotationData.getNamespace(), annotationData.getKeyPrefix(), baseKeys, dataList, retVal, jp.getArgs());
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



	protected void updateCache(final List<String> cacheKeys,
	                           final List<Object> returnList,
	                           final Method methodToCache,
	                           final AnnotationData annotationData,
                               final MemcachedClientIF cache) {
		if (returnList.size() != cacheKeys.size()) {
			throw new InvalidAnnotationException(String.format(
					"The key generation objects, and the resulting objects do not match in size for [%s].",
					methodToCache.toString()
			));
		}

		for (int ix = 0; ix < returnList.size(); ix++) {
			final Object result = returnList.get(ix);
			final String cacheKey = cacheKeys.get(ix);
			final Object cacheObject = result != null ? result : new PertinentNegativeNull();
			cache.set(cacheKey, annotationData.getExpiration(), cacheObject);
		}
	}

	protected List<Object> getKeyObjects(final int keyIndex,
	                             final Object returnValue,
	                             final JoinPoint jp,
	                             final Method methodToCache) throws Exception {
		final Object keyObject = keyIndex == -1
									? validateReturnValueAsKeyObject(returnValue, methodToCache)
									: getIndexObject(keyIndex, jp.getArgs(), methodToCache.toString());
		if (verifyTypeIsList(keyObject.getClass())) {
			return (List<Object>) keyObject;
		}
		throw new InvalidAnnotationException(String.format(
				"The parameter object found at dataIndex [%s] is not a [%s] but is of type [%s]. " +
				"[%s] does not fulfill the requirements.",
				UpdateMultiCache.class.getName(),
				List.class.getName(),
                keyObject.getClass().getName(),
				methodToCache.toString()
		));
	}
}
