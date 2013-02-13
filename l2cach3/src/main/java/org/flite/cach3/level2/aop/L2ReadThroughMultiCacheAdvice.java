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

import com.google.common.collect.*;
import org.apache.commons.lang.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.aop.*;
import org.flite.cach3.level2.annotations.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.security.*;
import java.util.*;

@Aspect
@Order((Ordered.HIGHEST_PRECEDENCE / 2) - 10)
public class L2ReadThroughMultiCacheAdvice extends L2CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(L2ReadThroughMultiCacheAdvice.class);

//    private static final Class expectedClass = L2ReadThroughMultiCache.class;

    @Pointcut("@annotation(org.flite.cach3.level2.annotations.L2ReadThroughMultiCache)")
    public void getMulti() {}

    @Around("getMulti()")
    public Object cacheMulti(final ProceedingJoinPoint pjp) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            System.out.println("Cache Disabled!");
            return pjp.proceed();
        }

        AnnotationInfo info;
        final MultiCacheCoordinator coord = new MultiCacheCoordinator();
        Object [] args = pjp.getArgs();
        try {
            // Get the target method being invoked, and make sure it returns the correct info.
            coord.setMethod(getMethodToCache(pjp));
            verifyReturnTypeIsList(coord.getMethod(), ReadThroughMultiCache.class);

            // Get the annotation associated with this method, and make sure the values are valid.
            final L2ReadThroughMultiCache annotation = coord.getMethod().getAnnotation(L2ReadThroughMultiCache.class);

//            // TODO: Remove soon?
//            coord.setL2annotationData(L2AnnotationDataBuilder.buildAnnotationData(
//                    annotation, L2ReadThroughMultiCache.class, coord.getMethod().getName()));
            info = getAnnotationInfo(annotation, coord.getMethod().getName());

            // Get the list of objects that will provide the keys to all the cache values.
            coord.setKeyObjects(ReadThroughMultiCacheAdvice.getKeyObjectList(info.<Integer>getAsType(AnnotationTypes.KEY_INDEX,null), pjp, coord.getMethod()));

            // Create key->object and object->key mappings.
            coord.setHolder(ReadThroughMultiCacheAdvice.convertIdObjectsToKeyMap(coord.getKeyObjects(),
                    info.<String>getAsType(AnnotationTypes.NAMESPACE, ""),
                    info.<String>getAsType(AnnotationTypes.KEY_PREFIX, ""),
                    info.<String>getAsType(AnnotationTypes.KEY_TEMPLATE, ""),
                    factory,
                    methodStore,
                    args));

            // Get the full list of cache keys and ask the cache for the corresponding values.
			coord.setInitialKey2Result(cache.getBulk(coord.getKey2Obj().keySet(), info.<Duration>getAsType(AnnotationTypes.WINDOW, null)));

			// We've gotten all positive cache results back, so build up a results list and return it.
			if (coord.getMissObjects().size() < 1) {
				return coord.generateResultList();
			}

			// Create the new list of arguments with a subset of the key objects that aren't in the cache.
			args = coord.modifyArgumentList(args, info.<Integer>getAsType(AnnotationTypes.KEY_INDEX, null));
        } catch (Throwable ex) {
            // If there's an exception somewhere in the caching code, then just bail out
            // and call through to the target method with the original parameters.
            LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            return pjp.proceed();
        }

        /*
		Call the target method with the new subset of arguments.
		We are calling this outside of the try/catch block in case there are some
		'not our fault' problems with the target method. (Connection issues, etc...)
		Though, this decision could go either way, really.
		 */
		final List results = (List) pjp.proceed(args);

        try {

            if (results.size() != coord.getMissObjects().size()) {
                throw new RuntimeException("Did not receive a correlated amount of data from the target method.");
            }

            final String[] cacheBaseIds = new String[results.size()];
            final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            for (int ix = 0; ix < results.size(); ix++) {
                final Object keyObject = coord.getMissObjects().get(ix);
                final Object resultObject = results.get(ix) == null ? new PertinentNegativeNull() : results.get(ix);
                final String cacheKey = coord.getObj2Key().get(keyObject);
                final String cacheBase = coord.getObj2Base().get(keyObject);
                builder.put(cacheKey, resultObject);
                cacheBaseIds[ix] = cacheBase;
            }
            final ImmutableMap<String, Object> input = builder.build();
            cache.setBulk(input, info.<Duration>getAsType(AnnotationTypes.WINDOW, null));
            coord.getKey2Result().putAll(input);

            return coord.generateResultList();
        } catch (Throwable ex) {
      			LOG.warn("Caching on " + pjp.toShortString()
      					+ " aborted due to an error. The underlying method will be called twice.", ex);
      			return pjp.proceed();
        }
    }


    static class MultiCacheCoordinator extends ReadThroughMultiCacheAdvice.MultiCacheCoordinator {
    }

    static AnnotationInfo getAnnotationInfo(final L2ReadThroughMultiCache annotation, final String targetMethodName) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    L2ReadThroughMultiCache.class.getName()
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
                    L2ReadThroughMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.KeyIndex(keyIndex));

        final String keyTemplate = annotation.keyTemplate();
        if (StringUtils.isNotBlank(keyTemplate) && !AnnotationConstants.DEFAULT_STRING.equals(keyTemplate)) {
            result.add(new AnnotationTypes.KeyTemplate(keyTemplate));
        }

        final Duration window = annotation.window();
        if (window == Duration.UNDEFINED) {
            throw new InvalidParameterException(String.format(
                    "Window for annotation [%s] must be a defined value on [%s]",
                    L2ReadThroughMultiCache.class.getName(),
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
                    L2ReadThroughMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AnnotationTypes.Namespace(namespace));

        return result;
    }
}
