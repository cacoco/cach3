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

import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.transcoders.IntegerTranscoder;
import net.spy.memcached.transcoders.LongTranscoder;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.flite.cach3.annotations.AnnotationConstants;
import org.flite.cach3.annotations.UpdateMultiCache;
import org.flite.cach3.annotations.UpdateSingleCache;
import org.flite.cach3.annotations.groups.UpdateSingleCaches;
import org.flite.cach3.api.CacheConditionally;
import org.flite.cach3.api.UpdateSingleCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE / 2)
public class UpdateSingleCacheAdvice extends CacheBase {
	private static final Logger LOG = LoggerFactory.getLogger(UpdateSingleCacheAdvice.class);

	@Pointcut("@annotation(org.flite.cach3.annotations.UpdateSingleCache)")
	public void updateSingle() {}

	@AfterReturning(pointcut="updateSingle()", returning="retVal")
	public Object cacheUpdateSingle(final JoinPoint jp, final Object retVal) throws Throwable {
        try {
            doUpdate(jp, retVal);
        } catch (Throwable ex) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
            } else {
                LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error: " + ex.getMessage());
            }
        }
        return retVal;
	}

    @Pointcut("@annotation(org.flite.cach3.annotations.groups.UpdateSingleCaches)")
	public void updateSingles() {}

	@AfterReturning(pointcut="updateSingles()", returning="retVal")
	public Object cacheUpdateSingles(final JoinPoint jp, final Object retVal) throws Throwable {
        try {
            doUpdate(jp, retVal);
        } catch (Throwable ex) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
            } else {
                LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error: " + ex.getMessage());
            }
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
        List<UpdateSingleCache> lAnnotations;

        if (methodToCache.getAnnotation(UpdateSingleCache.class) != null) {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(UpdateSingleCache.class));
        } else {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(UpdateSingleCaches.class).value());
        }

        for (int i = 0; i < lAnnotations.size(); i++) {
            // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
            // but do not let it surface up past the AOP injection itself.
            try {
                final AnnotationInfo info = getAnnotationInfo(lAnnotations.get(i), methodToCache.getName(), getJitterDefault());
                final String baseKey = CacheBase.getBaseKey(
                        info.getAsString(AType.KEY_TEMPLATE),
                        info.getAsInteger(AType.KEY_INDEX, null),
                        retVal,
                        jp.getArgs(),
                        methodToCache.toString(),
                        factory,
                        methodStore);
                final String cacheKey = buildCacheKey(baseKey,
                        info.getAsString(AType.NAMESPACE),
                        info.getAsString(AType.KEY_PREFIX));
                Object dataObject = getIndexObject(info.getAsInteger(AType.DATA_INDEX, null), retVal, jp.getArgs(), methodToCache.toString());
                dataObject = UpdateSingleCacheAdvice.getMergedData(dataObject, info.getAsString(AType.DATA_TEMPLATE, null), retVal, jp.getArgs(), factory);
                final Class dataTemplateType = (Class)info.getAsType(AType.DATA_TEMPLATE_TYPE, String.class);
                final Object submission = (dataObject == null) ? new PertinentNegativeNull() : applyDataTemplateType(dataObject, dataTemplateType);

                boolean cacheable = true;
                if (submission instanceof CacheConditionally) {
                    cacheable = ((CacheConditionally) submission).isCacheable();
                }

                if (cacheable) {
                    cache.set(cacheKey, calculateJitteredExpiration(info.getAsInteger(AType.EXPIRATION), info.getAsInteger(AType.JITTER)), submission);
                }

                // Notify the observers that a cache interaction happened.
                final List<UpdateSingleCacheListener> listeners = getPertinentListeners(UpdateSingleCacheListener.class, info.getAsString(AType.NAMESPACE));
                if (listeners != null && !listeners.isEmpty()) {
                    for (final UpdateSingleCacheListener listener : listeners) {
                        try {
                            listener.triggeredUpdateSingleCache(info.getAsString(AType.NAMESPACE), info.getAsString(AType.KEY_PREFIX, null), baseKey, dataObject, retVal, jp.getArgs());
                        } catch (Exception ex) {
                            LOG.warn("Problem when triggering a listener.", ex);
                        }
                    }
                }
            } catch (Exception ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
                } else {
                    LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error: " + ex.getMessage());
                }
            }
        }
    }

    /*default*/ static AnnotationInfo getAnnotationInfo(final UpdateSingleCache annotation,
                                                        final String targetMethodName,
                                                        final int jitterDefault) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    UpdateSingleCache.class.getName()
            ));
        }

        final String namespace = annotation.namespace();
        if (AnnotationConstants.DEFAULT_STRING.equals(namespace)
                || namespace == null
                || namespace.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "Namespace for annotation [%s] must be defined on [%s]",
                    UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Namespace(namespace));

        final String keyPrefix = annotation.keyPrefix();
        if (!AnnotationConstants.DEFAULT_STRING.equals(keyPrefix)) {
            if (StringUtils.isBlank(keyPrefix)) {
                throw new InvalidParameterException(String.format(
                        "KeyPrefix for annotation [%s] must not be defined as an empty string on [%s]",
                        UpdateSingleCache.class.getName(),
                        targetMethodName
                ));
            }
            result.add(new AType.KeyPrefix(keyPrefix));
        }

        final Integer keyIndex = annotation.keyIndex();
        if (keyIndex != AnnotationConstants.DEFAULT_KEY_INDEX && keyIndex < -1) {
            throw new InvalidParameterException(String.format(
                    "KeyIndex for annotation [%s] must be -1 or greater on [%s]",
                    UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        final boolean keyIndexDefined = keyIndex >= -1;

        final String keyTemplate = annotation.keyTemplate();
        if (StringUtils.isBlank(keyTemplate)) {
            throw new InvalidParameterException(String.format(
                    "KeyTemplate for annotation [%s] must not be defined as an empty string on [%s]",
                    UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        final boolean keyTemplateDefined = !AnnotationConstants.DEFAULT_STRING.equals(keyTemplate);

        if (keyIndexDefined == keyTemplateDefined) {
            throw new InvalidParameterException(String.format(
                    "Exactly one of [keyIndex,keyTemplate] must be defined for annotation [%s] on [%s]",
                    UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }

        if (keyIndexDefined) {
            result.add(new AType.KeyIndex(keyIndex));
        }

        if (keyTemplateDefined) {
            result.add(new AType.KeyTemplate(keyTemplate));
        }

        final int dataIndex = annotation.dataIndex();
        if (dataIndex < -1) {
            throw new InvalidParameterException(String.format(
                    "DataIndex for annotation [%s] must be -1 or greater on [%s]",
                    UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.DataIndex(dataIndex));

        final String dataTemplate = annotation.dataTemplate();
        if (!AnnotationConstants.DEFAULT_STRING.equals(dataTemplate)) {
            if (StringUtils.isBlank(dataTemplate)) {
                throw new InvalidParameterException(String.format(
                        "DataTemplate for annotation [%s] must not be defined as an empty string on [%s]",
                        UpdateMultiCache.class.getName(),
                        targetMethodName
                ));
            }
            result.add(new AType.DataTemplate(dataTemplate));
        }

        final Class dataTemplateType = annotation.dataTemplateType();
        if (!String.class.equals(dataTemplateType)) {
            result.add(new AType.DataTemplateType(dataTemplateType));
        }

        final int expiration = annotation.expiration();
        if (expiration < 0) {
            throw new InvalidParameterException(String.format(
                    "Expiration for annotation [%s] must be 0 or greater on [%s]",
                    UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Expiration(expiration));

        final int jitter = annotation.jitter();
        if (jitter < -1 || jitter > 99) {
            throw new InvalidParameterException(String.format(
                    "Jitter for annotation [%s] must be -1 <= jitter <= 99 on [%s]",
                    UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Jitter(jitter == -1 ? jitterDefault : jitter));

        return result;
    }
}
