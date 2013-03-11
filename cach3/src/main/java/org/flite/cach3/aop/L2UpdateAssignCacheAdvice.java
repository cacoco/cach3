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

import com.google.common.collect.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.*;
import java.security.*;

@Aspect
@Order((Ordered.HIGHEST_PRECEDENCE / 2) - 10)
public class L2UpdateAssignCacheAdvice extends L2CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(L2UpdateAssignCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.L2UpdateAssignCache)")
    public void updateL2Assign() {}

    @AfterReturning(pointcut="updateL2Assign()", returning="retVal")
    public Object cacheUpdateL2Assign(final JoinPoint jp, final Object retVal) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return retVal;
        }

        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        try {
            final Method methodToCache = getMethodToCache(jp);
            final L2UpdateAssignCache annotation = methodToCache.getAnnotation(L2UpdateAssignCache.class);
            final AnnotationInfo info = getAnnotationInfo(annotation, methodToCache.getName());
            final String cacheKey = buildCacheKey(info.<String>getAsType(AnnotationTypes.ASSIGN_KEY, ""),
                    info.<String>getAsType(AnnotationTypes.NAMESPACE, ""),
                    info.<String>getAsType(AnnotationTypes.KEY_PREFIX, ""));

            final int dataIndex = info.<Integer>getAsType(AnnotationTypes.DATA_INDEX, -2).intValue();
            final Object dataObject = dataIndex == -1
                    ? retVal
                    : CacheBase.getIndexObject(dataIndex, jp.getArgs(), methodToCache.toString());
            final Object submission = (dataObject == null) ? new PertinentNegativeNull() : dataObject;
            boolean cacheable = true;
            if (submission instanceof CacheConditionally) {
               cacheable = ((CacheConditionally) submission).isCacheable();
            }
            if (cacheable) {
                getCache().setBulk(ImmutableMap.of(cacheKey, submission), info.<Duration>getAsType(AnnotationTypes.WINDOW, null));
            }
		} catch (Exception ex) {
			LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
		}

        return retVal;
    }

    /*default*/ static AnnotationInfo getAnnotationInfo(final L2UpdateAssignCache annotation, final String targetMethodName) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    L2UpdateAssignCache.class.getName()
            ));
        }

        final String assignKey = annotation.assignedKey();
        if (AnnotationConstants.DEFAULT_STRING.equals(assignKey)
                || assignKey == null
                || assignKey.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "AssignedKey for annotation [%s] must be defined on [%s]",
                    L2UpdateAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.AssignKey(assignKey));

        final int dataIndex = annotation.dataIndex();
        if (dataIndex < -1) {
            throw new InvalidParameterException(String.format(
                    "DataIndex for annotation [%s] must be -1 or greater on [%s]",
                    L2UpdateAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.DataIndex(dataIndex));

        final Duration window = annotation.window();
        if (window == Duration.UNDEFINED) {
            throw new InvalidParameterException(String.format(
                    "Window for annotation [%s] must be a defined value on [%s]",
                    L2UpdateAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.Window(window));

        final String namespace = annotation.namespace();
        if (AnnotationConstants.DEFAULT_STRING.equals(namespace)
                || namespace == null
                || namespace.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "Namespace for annotation [%s] must be defined on [%s]",
                    L2UpdateAssignCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.Namespace(namespace));

        return result;
    }
}
