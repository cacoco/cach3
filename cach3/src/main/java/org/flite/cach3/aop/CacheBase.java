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
import org.apache.commons.lang.*;
import org.apache.commons.lang.math.*;
import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.aspectj.lang.*;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.flite.cach3.config.*;
import org.flite.cach3.exceptions.*;

import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class CacheBase {

	static final String SEPARATOR = ":";

//	protected MemcachedClientIF cache;
	CacheKeyMethodStore methodStore;
    protected Cach3State state;
    protected VelocityContextFactory factory;

    public void setFactory(VelocityContextFactory factory) {
        this.factory = factory;
    }

    public void setState(Cach3State state) {
        this.state = state;
    }

    protected boolean isCacheDisabled() {
        return state == null ? false : state.isCacheDisabled();
    }

    protected int getJitterDefault() {
        return state.getJitterDefault();
    }

	public void setMethodStore(CacheKeyMethodStore methodStore) {
		this.methodStore = methodStore;
	}

	public static Method getMethodToCache(final JoinPoint jp) throws NoSuchMethodException {
		final Signature sig = jp.getSignature();
		if (!(sig instanceof MethodSignature)) {
			throw new InvalidAnnotationException("This annotation is only valid on a method.");
		}
		final MethodSignature msig = (MethodSignature) sig;
		final Object target = jp.getTarget();
		return target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
	}

	public static String generateObjectId(final Method keyMethod, final Object keyObject) throws Exception {
		final String objectId = (String) keyMethod.invoke(keyObject, null);
		if (objectId == null || objectId.length() < 1) {
			throw new RuntimeException("Got an empty key value from " + keyMethod.getName());
		}
		return objectId;
	}

    protected static final char[] WS = new char[] {' ', '\n', '\t'};
    protected static String buildCacheKey(final String objectId, final AnnotationData data) {
        return buildCacheKey(objectId, data.getNamespace(), data.getKeyPrefix());
    }

    public static String buildCacheKey(final String objectId, final String namespace, final String prefix) {
        if (objectId == null || objectId.length() < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }
        final StringBuilder result = new StringBuilder(namespace).append(SEPARATOR);
        if (StringUtils.isNotBlank(prefix)) {
            result.append(prefix);
        }
        result.append(objectId);
        if (result.length() > 255) {
            throw new InvalidParameterException("Ids for objects in the cache must not exceed 255 characters: [" + result.toString() + "]");
        }
        final String resultString = result.toString();
        if (StringUtils.containsAny(resultString, WS)) {
            throw new InvalidParameterException("Ids for objects in the cache must not have whitespace: [" + result.toString() + "]");
        }
        return resultString;
    }

    public static Object getIndexObject(final int index,
                                           final Object retVal,
                                           final Object[] args,
                                           final String methodString) throws Exception {
        return getIndexObject(true, index, retVal, args, methodString);
	}

    public static Object getIndexObject(final int index,
                                           final Object[] args,
                                           final String methodString) throws Exception {
        return getIndexObject(false, index, null, args, methodString);
	}

    public static Object getIndexObject(final boolean allowRetVal,
                                           final int index,
                                           final Object retVal,
                                           final Object[] args,
                                           final String methodString) throws Exception {
        if (index < -1 || (index == -1 && !allowRetVal)) {
            throw new InvalidParameterException(String.format(
					"An index of %s is invalid",
					index));
        }
		if (args.length <= index) {
			throw new InvalidParameterException(String.format(
					"An index of %s is too big for the number of arguments in [%s]",
					index,
					methodString));
		}
		final Object indexObject = index == -1 ? retVal : args[index];
		if (indexObject == null) {
			throw new InvalidParameterException(String.format(
					"The argument passed into [%s] at index %s is null.",
					methodString,
					index));
		}
		return indexObject;
	}

	public static Object validateReturnValueAsKeyObject(final Object returnValue,
                                             final Method methodToCache) throws Exception {
		if (returnValue == null) {
			throw new InvalidParameterException(String.format(
					"The result of the method [%s] is null, which will not give an appropriate cache key.",
					methodToCache.toString()));
		}
		return returnValue;
	}

	protected Method getKeyMethod(final Object keyObject) throws NoSuchMethodException {
        return getKeyMethod(keyObject, methodStore);
	}

    public static Method getKeyMethod(final Object keyObject, final CacheKeyMethodStore methodStore) throws NoSuchMethodException {
        final Method storedMethod = methodStore.find(keyObject.getClass());
        if (storedMethod != null) { return storedMethod; }
        final Method[] methods = keyObject.getClass().getDeclaredMethods();
        Method targetMethod = null;
        for (final Method method : methods) {
            if (method != null && method.getAnnotation(CacheKeyMethod.class) != null) {
                if (method.getParameterTypes().length > 0) {
                    throw new InvalidAnnotationException(String.format(
                            "Method [%s] must have 0 arguments to be annotated with [%s]",
                            method.toString(),
                            CacheKeyMethod.class.getName()));
                }
                if (!String.class.equals(method.getReturnType())) {
                    throw new InvalidAnnotationException(String.format(
                            "Method [%s] must return a String to be annotated with [%s]",
                            method.toString(),
                            CacheKeyMethod.class.getName()));
                }
                if (targetMethod != null) {
                    throw new InvalidAnnotationException(String.format(
                            "Class [%s] should have only one method annotated with [%s]. See [%s] and [%s]",
                            keyObject.getClass().getName(),
                            CacheKeyMethod.class.getName(),
                            targetMethod.getName(),
                            method.getName()));
                }
                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            targetMethod = keyObject.getClass().getMethod("toString", null);
        }

        methodStore.add(keyObject.getClass(), targetMethod);

        return targetMethod;
    }

    public static void verifyReturnTypeIsList(final Method method, final Class annotationClass) {
		if (verifyTypeIsList(method.getReturnType())) { return; }
		throw new InvalidAnnotationException(String.format(
				"The annotation [%s] is only valid on a method that returns a [%s]. " +
				"[%s] does not fulfill this requirement by returning [%s].",
				annotationClass.getName(),
				List.class.getName(),
				method.toString(),
                method.getReturnType().getName()
		));
	}

	protected static boolean verifyTypeIsList(final Class clazz) {
        return java.util.List.class.isAssignableFrom(clazz);
	}

    protected String getBaseKey(final AnnotationData annotationData,
                                final Object retVal,
                                final Object[] args,
                                final String methodString) throws Exception {
        return getBaseKey(annotationData.getKeyTemplate(),
                annotationData.getKeyIndex(),
                retVal,
                args,
                methodString,
                factory,
                methodStore);
    }

    public static String getBaseKey(final String keyTemplate,
                                    final Integer keyIndex,
                                    final Object retVal,
                                    final Object[] args,
                                    final String methodString,
                                    final VelocityContextFactory factory,
                                    final CacheKeyMethodStore methodStore) throws Exception {
        if (StringUtils.isBlank(keyTemplate)) {
            final Object keyObject = getIndexObject(keyIndex, retVal, args, methodString);
            return generateObjectId(getKeyMethod(keyObject, methodStore), keyObject);
        }

        final VelocityContext context = factory.getNewExtendedContext();
        context.put("args", args);
        context.put("retVal", retVal);

        final StringWriter writer = new StringWriter(250);
        Velocity.evaluate(context, writer, CacheBase.class.getSimpleName() , keyTemplate);
        final String result = writer.toString();
        if (keyTemplate.equals(result)) { throw new InvalidParameterException("Calculated key is equal to the velocityTemplate."); }
        return result;
    }

    protected List<String> getBaseKeys(final List<Object> keyObjects,
                                        final AnnotationData annotationData,
                                        final Object retVal,
                                        final Object[] args) throws Exception {
        return getBaseKeys(keyObjects, annotationData.getKeyTemplate(), retVal, args, factory, methodStore);
    }

    public static List<String> getBaseKeys(final List<Object> keyObjects,
                                        final String template,
                                        final Object retVal,
                                        final Object[] args,
                                        final VelocityContextFactory factory,
                                        final CacheKeyMethodStore methodStore) throws Exception {
        final List<String> results = new ArrayList<String>();
        for (int ix = 0; ix < keyObjects.size(); ix++) {
            final Object object = keyObjects.get(ix);
            final String base;
            if (StringUtils.isBlank(template)) {
                final Method method = getKeyMethod(object, methodStore);
                base = generateObjectId(method, object);
            } else {
                final VelocityContext context = factory.getNewExtendedContext();
                context.put("args", args);
                context.put("index", ix);
                context.put("indexObject", object);
                context.put("retVal", retVal);

                final StringWriter writer = new StringWriter(250);
                Velocity.evaluate(context, writer, CacheBase.class.getSimpleName() , template);
                base = writer.toString();
                if (template.equals(base)) { throw new InvalidParameterException("Calculated key is equal to the velocityTemplate."); }
            }
            results.add(base);
        }

        return results;
    }

    protected <L extends CacheListener> List<L> getPertinentListeners(final Class<L> clazz,
                                                                      final String namespace) throws Exception {
        if (clazz == null) { throw new InvalidParameterException("Clazz type must be defined."); }
        if (namespace == null || namespace.length() == 0) { throw new InvalidParameterException("Namespace must be defined."); }

        final List<L> results = new ArrayList<L>();
        final List<L> baseList = state.getListeners(clazz);
        if (baseList != null && !baseList.isEmpty()) {
            for (final L base : baseList) {
                if (base.getNamespacesOfInterest() == null
                        || base.getNamespacesOfInterest().isEmpty()
                        || base.getNamespacesOfInterest().contains(namespace)) {
                    results.add(base);
                }
            }
        }

        return results;
    }

    protected MemcachedClientIF getMemcachedClient() {
        return state.getMemcachedClient();
    }

    /*default*/ static final int JITTER_BOUND = 60 * 60 * 24 * 30;
    public static int calculateJitteredExpiration(final int expiration, final int jitter) {
        if (jitter <= 0 || jitter > 99) { return expiration; }
        if (expiration >= JITTER_BOUND) { return expiration; }

        final double seed = RandomUtils.nextDouble() * expiration * jitter;
        final int difference = (int) Math.floor(seed/100);
        return (expiration - difference);
    }

}
