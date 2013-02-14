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

package org.flite.cach3.level2.aop;

import org.apache.commons.lang.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.annotations.groups.*;
import org.flite.cach3.aop.*;
import org.flite.cach3.api.*;
import org.flite.cach3.level2.annotations.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

@Aspect
@Order((Ordered.HIGHEST_PRECEDENCE / 2) - 10)
public class L2UpdateSingleCacheAdvice extends L2CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(L2UpdateSingleCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.level2.annotations.L2UpdateSingleCache)")
   	public void updateSingle() {}

    @AfterReturning(pointcut="updateSingle()", returning="retVal")
   	public Object cacheUpdateSingle(final JoinPoint jp, final Object retVal) throws Throwable {
           try {
               doUpdate(jp, retVal);
           } catch (Throwable ex) {
               LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
           }
           return retVal;
   	}

//    @Pointcut("@annotation(org.flite.cach3.annotations.groups.UpdateSingleCaches)")
//	public void updateSingles() {}
//
//	@AfterReturning(pointcut="updateSingles()", returning="retVal")
//	public Object cacheUpdateSingles(final JoinPoint jp, final Object retVal) throws Throwable {
//        try {
//            doUpdate(jp, retVal);
//        } catch (Throwable ex) {
//            LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
//        }
//        return retVal;
//    }

    private void doUpdate(final JoinPoint jp, final Object retVal) throws Throwable {
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return;
        }

        final Method methodToCache = getMethodToCache(jp);
        List<L2UpdateSingleCache> lAnnotations;

//        if (methodToCache.getAnnotation(UpdateSingleCache.class) != null) {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(L2UpdateSingleCache.class));
//        } else {
//            lAnnotations = Arrays.asList(methodToCache.getAnnotation(UpdateSingleCaches.class).value());
//        }

        for (int i = 0; i < lAnnotations.size(); i++) {
            // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
            // but do not let it surface up past the AOP injection itself.
            try {
                final AnnotationInfo info = getAnnotationInfo(lAnnotations.get(i), methodToCache.getName());
                final String baseKey = getBaseKey(annotationData, retVal, jp.getArgs(), methodToCache.toString());
                final String cacheKey = buildCacheKey(baseKey, annotationData);
                final Object dataObject = getIndexObject(annotationData.getDataIndex(), retVal, jp.getArgs(), methodToCache.toString());
                final Object submission = (dataObject == null) ? new PertinentNegativeNull() : dataObject;
                cache.set(cacheKey, annotationData.getJitteredExpiration(), submission);

            } catch (Exception ex) {
                LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
            }
        }
    }


    /*default*/ static AnnotationInfo getAnnotationInfo(final L2UpdateSingleCache annotation, final String targetMethodName) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    L2UpdateSingleCache.class.getName()
            ));
        }

        final String keyPrefix = annotation.keyPrefix();
        if (!AnnotationConstants.DEFAULT_STRING.equals(keyPrefix)
                && keyPrefix != null
                && keyPrefix.length() > 0) {
            result.add(new AnnotationTypes.KeyPrefix(keyPrefix));
        }

        final Integer keyIndex = annotation.keyIndex();
        if (keyIndex < 0) {
            throw new InvalidParameterException(String.format(
                    "KeyIndex for annotation [%s] must be 0 or greater on [%s]",
                    L2UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.KeyIndex(keyIndex));

        final String keyTemplate = annotation.keyTemplate();
        if (StringUtils.isNotBlank(keyTemplate) && !AnnotationConstants.DEFAULT_STRING.equals(keyTemplate)) {
            result.add(new AnnotationTypes.KeyTemplate(keyTemplate));
        }

        final int dataIndex = annotation.dataIndex();
        if (dataIndex < -1) {
            throw new InvalidParameterException(String.format(
                    "DataIndex for annotation [%s] must be -1 or greater on [%s]",
                    L2UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.DataIndex(dataIndex));

        final Duration window = annotation.window();
        if (window == Duration.UNDEFINED) {
            throw new InvalidParameterException(String.format(
                    "Window for annotation [%s] must be a defined value on [%s]",
                    L2UpdateSingleCache.class.getName(),
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
                    L2UpdateSingleCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.Namespace(namespace));

        return result;
    }
}
