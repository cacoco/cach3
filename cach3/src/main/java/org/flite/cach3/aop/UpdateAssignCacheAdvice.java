package org.flite.cach3.aop;

import org.apache.commons.logging.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;

import java.lang.reflect.*;

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
public class UpdateAssignCacheAdvice extends CacheBase {
    private static final Log LOG = LogFactory.getLog(UpdateAssignCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.UpdateAssignCache)")
    public void updateAssign() {}

    @AfterReturning(pointcut="updateAssign()", returning="retVal")
    public Object cacheUpdateAssign(final JoinPoint jp, final Object retVal) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return retVal;
        }

        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
        try {
            final Method methodToCache = getMethodToCache(jp);
            final UpdateAssignCache annotation = methodToCache.getAnnotation(UpdateAssignCache.class);
            final AnnotationData annotationData = AnnotationDataBuilder.buildAnnotationData(annotation,
                            UpdateAssignCache.class,
                            methodToCache.getName());
            final String cacheKey = buildCacheKey(annotationData.getAssignedKey(), annotationData);
            final Object dataObject = annotationData.getDataIndex() == -1
                    ? retVal
                    : getIndexObject(annotationData.getDataIndex(), jp, methodToCache);
            final Object submission = (dataObject == null) ? new PertinentNegativeNull() : dataObject;
			cache.set(cacheKey, annotationData.getExpiration(), submission);
		} catch (Exception ex) {
			LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
		}

        return retVal;

    }
}
