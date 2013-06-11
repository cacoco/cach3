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
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.flite.cach3.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.flite.cach3.api.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.Method;
import java.security.*;
import java.util.*;

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
        final AnnotationInfo info;
        try {
            final Method methodToCache = getMethodToCache(pjp);
            final ReadThroughAssignCache annotation = methodToCache.getAnnotation(ReadThroughAssignCache.class);
            info = getAnnotationInfo(annotation, methodToCache.getName(), getJitterDefault());
            cacheKey = buildCacheKey(info.getAsString(AType.ASSIGN_KEY),
                    info.getAsString(AType.NAMESPACE),
                    info.getAsString(AType.KEY_PREFIX));
            final Object result = cache.get(cacheKey);
            if (result != null) {
                return (result instanceof PertinentNegativeNull) ? null : result;
            }
        } catch (Throwable ex) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            } else {
                LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error: " + ex.getMessage());
            }
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
                cache.set(cacheKey,
                        calculateJitteredExpiration(info.getAsInteger(AType.EXPIRATION), info.getAsInteger(AType.JITTER)),
                        submission);
            }

            // Notify the observers that a cache interaction happened.
            final List<ReadThroughAssignCacheListener> listeners = getPertinentListeners(ReadThroughAssignCacheListener.class, info.getAsString(AType.NAMESPACE));
            if (listeners != null && !listeners.isEmpty()) {
                for (final ReadThroughAssignCacheListener listener : listeners) {
                    try {
                        listener.triggeredReadThroughAssignCache(info.getAsString(AType.NAMESPACE),
                                info.getAsString(AType.ASSIGN_KEY, null),
                                result,
                                pjp.getArgs());
                    } catch (Exception ex) {
                        LOG.warn("Problem when triggering a listener.", ex);
                    }
                }
            }
        } catch (Throwable ex) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            } else {
                LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error: " + ex.getMessage());
            }
        }
        return result;
    }

    static AnnotationInfo getAnnotationInfo(final ReadThroughAssignCache annotation,
                                            final String targetMethodName,
                                            final int jitterDefault) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    ReadThroughAssignCache.class.getName()
            ));
        }

        final String namespace = annotation.namespace();
        if (AnnotationConstants.DEFAULT_STRING.equals(namespace)
                || namespace == null
                || namespace.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "Namespace for annotation [%s] must be defined on [%s]",
                    ReadThroughAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Namespace(namespace));

        final String assignKey = annotation.assignedKey();
        if (AnnotationConstants.DEFAULT_STRING.equals(assignKey)
                || assignKey == null
                || assignKey.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "AssignedKey for annotation [%s] must be defined on [%s]",
                    ReadThroughAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.AssignKey(assignKey));

        final int expiration = annotation.expiration();
        if (expiration < 0) {
            throw new InvalidParameterException(String.format(
                    "Expiration for annotation [%s] must be 0 or greater on [%s]",
                    ReadThroughAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Expiration(expiration));

        final int jitter = annotation.jitter();
        if (jitter < -1 || jitter > 99) {
            throw new InvalidParameterException(String.format(
                    "Jitter for annotation [%s] must be -1 <= jitter <= 99 on [%s]",
                    ReadThroughAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Jitter(jitter == -1 ? jitterDefault : jitter));

        return result;
    }
}
