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

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.ReadThroughMultiCache;
import org.flite.cach3.aop.AnnotationData;
import org.flite.cach3.aop.ReadThroughMultiCacheAdvice;
import org.flite.cach3.level2.annotations.L2ReadThroughMultiCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Order((Ordered.HIGHEST_PRECEDENCE / 2) - 10)
public class L2ReadThroughMultiCacheAdvice extends L2CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(L2ReadThroughMultiCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.level2.annotations.L2ReadThroughMultiCache)")
    public void getMulti() {}

    @Around("getMulti()")
    public Object cacheMulti(final ProceedingJoinPoint pjp) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return pjp.proceed();
        }

        final MultiCacheCoordinator coord = new MultiCacheCoordinator();
        Object [] args = pjp.getArgs();
        try {
            // Get the target method being invoked, and make sure it returns the correct info.
            coord.setMethod(getMethodToCache(pjp));
            verifyReturnTypeIsList(coord.getMethod(), ReadThroughMultiCache.class);

            // Get the annotation associated with this method, and make sure the values are valid.
            final L2ReadThroughMultiCache annotation = coord.getMethod().getAnnotation(L2ReadThroughMultiCache.class);

            coord.setL2annotationData(L2AnnotationDataBuilder.buildAnnotationData(
                    annotation, L2ReadThroughMultiCache.class, coord.getMethod().getName()));

            // Get the list of objects that will provide the keys to all the cache values.
            coord.setKeyObjects(ReadThroughMultiCacheAdvice.getKeyObjectList(coord.getL2annotationData().getKeyIndex(), pjp, coord.getMethod()));

            // Create key->object and object->key mappings.
            coord.setHolder(ReadThroughMultiCacheAdvice.convertIdObjectsToKeyMap(coord.getKeyObjects(),
                    coord.getL2annotationData().getNamespace(),
                    coord.getL2annotationData().getKeyPrefix(),
                    coord.getL2annotationData().getKeyTemplate(),
                    factory,
                    methodStore,
                    args));


        } catch (Throwable ex) {
            // If there's an exception somewhere in the caching code, then just bail out
            // and call through to the target method with the original parameters.
            LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            return pjp.proceed();
        }

        return pjp.proceed();
    }


    static class MultiCacheCoordinator extends ReadThroughMultiCacheAdvice.MultiCacheCoordinator {
        private L2AnnotationData l2annotationData;

        public L2AnnotationData getL2annotationData() {
            return l2annotationData;
        }

        public void setL2annotationData(L2AnnotationData l2annotationData) {
            this.l2annotationData = l2annotationData;
        }
    }

}