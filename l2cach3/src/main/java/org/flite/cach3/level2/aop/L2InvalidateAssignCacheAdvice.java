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

import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.level2.annotations.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

@Aspect
@Order((Ordered.HIGHEST_PRECEDENCE / 2) - 10)
public class L2InvalidateAssignCacheAdvice extends L2CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(L2InvalidateAssignCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.level2.annotations.L2InvalidateAssignCache)")
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


//    @Pointcut("@annotation(org.flite.cach3.annotations.groups.InvalidateAssignCaches)")
//    public void invalidateAssigns() {}
//
//    @AfterReturning(pointcut = "invalidateAssigns()", returning = "retVal")
//    public Object cacheInvalidateAssigns(final JoinPoint jp, final Object retVal) throws Throwable {
//        try {
//            doInvalidate(jp, retVal);
//        } catch (Throwable ex) {
//            LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
//        }
//        return retVal;
//    }

    private void doInvalidate(final JoinPoint jp, final Object retVal) throws Throwable {
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return;
        }

        final Method methodToCache = getMethodToCache(jp);
        List<L2InvalidateAssignCache> lAnnotations;

//        if (methodToCache.getAnnotation(InvalidateAssignCache.class) != null) {
            lAnnotations = Arrays.asList(methodToCache.getAnnotation(L2InvalidateAssignCache.class));
//        } else {
//            lAnnotations = Arrays.asList(methodToCache.getAnnotation(InvalidateAssignCaches.class).value());
//        }

        for (int i = 0; i < lAnnotations.size(); i++) {
            // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
            // but do not let it surface up past the AOP injection itself.
            try {
                final AnnotationInfo info = getAnnotationInfo(lAnnotations.get(i), methodToCache.getName());
                final String cacheKey = buildCacheKey(info.<String>getAsType(AnnotationTypes.ASSIGN_KEY, ""),
                        info.<String>getAsType(AnnotationTypes.NAMESPACE, ""),
                        info.<String>getAsType(AnnotationTypes.KEY_PREFIX, ""));
                if (cacheKey == null || cacheKey.trim().length() == 0) {
                    throw new InvalidParameterException("Unable to find a cache key");
                }
                getCache().invalidateBulk(Arrays.asList(cacheKey));
            } catch (Throwable ex) {
                LOG.warn("Caching on " + jp.toShortString() + " aborted due to an error.", ex);
            }
        }
    }

    /*default*/ static AnnotationInfo getAnnotationInfo(final L2InvalidateAssignCache annotation, final String targetMethodName) {
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
