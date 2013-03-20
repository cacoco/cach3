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
import org.apache.commons.lang.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.flite.cach3.exceptions.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

@Aspect
@Order((Ordered.HIGHEST_PRECEDENCE / 2) - 10)
public class L2UpdateMultiCacheAdvice extends L2CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(L2UpdateMultiCacheAdvice.class);

    @Pointcut("@annotation(org.flite.cach3.annotations.L2UpdateMultiCache)")
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

//    @Pointcut("@annotation(org.flite.cach3.annotations.groups.UpdateMultiCaches)")
//	public void updateMultis() {}
//
//	@AfterReturning(pointcut="updateMultis()", returning="retVal")
//	public Object cacheUpdateMultis(final JoinPoint jp, final Object retVal) throws Throwable {
//        try {
//            doUpdate(jp, retVal);
//        } catch (Throwable ex) {
//            LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
//        }
//        return retVal;
//    }

    private void doUpdate(final JoinPoint jp, final Object retVal) throws Throwable {
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return;
        }

        final Method methodToCache = getMethodToCache(jp);
        List<L2UpdateMultiCache> lAnnotations;

//        if (methodToCache.getAnnotation(L2UpdateMultiCache.class) != null) {
        lAnnotations = Arrays.asList(methodToCache.getAnnotation(L2UpdateMultiCache.class));
//        } else {
//            lAnnotations = Arrays.asList();
//            lAnnotations = Arrays.asList(methodToCache.getAnnotation(L2UpdateMultiCaches.class).value());
//        }

        for (int i = 0; i < lAnnotations.size(); i++) {
            try {
                // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
                // but do not let it surface up past the AOP injection itself.
                final AnnotationInfo info = getAnnotationInfo(lAnnotations.get(i), methodToCache.getName());

                final List<Object> dataList = (List<Object>) UpdateMultiCacheAdvice.getIndexObject(info.getAsInteger(AType.DATA_INDEX), retVal, jp.getArgs(), methodToCache.toString());
                final List<Object> keyObjects = UpdateMultiCacheAdvice.getKeyObjects(info.getAsInteger(AType.KEY_INDEX), retVal, jp, methodToCache);
                final List<String> baseKeys = UpdateMultiCacheAdvice.getBaseKeys(keyObjects, info.getAsString(AType.KEY_TEMPLATE, null), retVal, jp.getArgs(), factory, methodStore);
                final List<String> cacheKeys = new ArrayList<String>(baseKeys.size());
                for (final String base : baseKeys) {
                    cacheKeys.add(buildCacheKey(base,
                            info.getAsString(AType.NAMESPACE,null),
                            info.getAsString(AType.KEY_PREFIX,null)));
                }
                updateCache(cacheKeys, dataList, methodToCache, info.<Duration>getAsType(AType.WINDOW, null), getCache());
            } catch (Exception ex) {
                LOG.warn("Updating caching via " + jp.toShortString() + " aborted due to an error.", ex);
            }
        }
    }

    protected static void updateCache(final List<String> cacheKeys,
   	                           final List<Object> returnList,
   	                           final Method methodToCache,
   	                           final Duration window,
                               final LogicalCacheIF cache) {
   		if (returnList.size() != cacheKeys.size()) {
   			throw new InvalidAnnotationException(String.format(
   					"The key generation objects, and the resulting objects do not match in size for [%s].",
   					methodToCache.toString()
   			));
   		}

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
   		for (int ix = 0; ix < returnList.size(); ix++) {
            final Object resultObject = returnList.get(ix);
            boolean cacheable = true;
            if (resultObject instanceof CacheConditionally) {
               cacheable = ((CacheConditionally) resultObject).isCacheable();
            }
            if (cacheable) {
                builder.put(cacheKeys.get(ix), resultObject);
            }
   		}

        cache.setBulk(builder.build(), window);
   	}


    /*default*/ static AnnotationInfo getAnnotationInfo(final L2UpdateMultiCache annotation, final String targetMethodName) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    L2UpdateMultiCache.class.getName()
            ));
        }

        final String keyPrefix = annotation.keyPrefix();
        if (!AnnotationConstants.DEFAULT_STRING.equals(keyPrefix)
                && keyPrefix != null
                && keyPrefix.length() > 0) {
            result.add(new AType.KeyPrefix(keyPrefix));
        }

        final Integer keyIndex = annotation.keyIndex();
        if (keyIndex < 0) {
            throw new InvalidParameterException(String.format(
                    "KeyIndex for annotation [%s] must be 0 or greater on [%s]",
                    L2UpdateMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.KeyIndex(keyIndex));

        final String keyTemplate = annotation.keyTemplate();
        if (StringUtils.isNotBlank(keyTemplate) && !AnnotationConstants.DEFAULT_STRING.equals(keyTemplate)) {
            result.add(new AType.KeyTemplate(keyTemplate));
        }

        final int dataIndex = annotation.dataIndex();
        if (dataIndex < -1) {
            throw new InvalidParameterException(String.format(
                    "DataIndex for annotation [%s] must be -1 or greater on [%s]",
                    L2UpdateMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.DataIndex(dataIndex));

        final Duration window = annotation.window();
        if (window == Duration.UNDEFINED) {
            throw new InvalidParameterException(String.format(
                    "Window for annotation [%s] must be a defined value on [%s]",
                    L2UpdateMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Window(window));

        final String namespace = annotation.namespace();
        if (AnnotationConstants.DEFAULT_STRING.equals(namespace)
                || namespace == null
                || namespace.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "Namespace for annotation [%s] must be defined on [%s]",
                    L2UpdateMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Namespace(namespace));

        return result;
    }
}
