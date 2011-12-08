package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.apache.commons.lang.*;
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
public class CacheBase {

	static final String SEPARATOR = ":";

//	protected MemcachedClientIF cache;
	CacheKeyMethodStore methodStore;
    private Cach3State state;

    public void setState(Cach3State state) {
        this.state = state;
    }

    protected boolean isCacheDisabled() {
        return state == null ? false : state.isCacheDisabled();
    }

	public void setMethodStore(CacheKeyMethodStore methodStore) {
		this.methodStore = methodStore;
	}

	protected Method getMethodToCache(final JoinPoint jp) throws NoSuchMethodException {
		final Signature sig = jp.getSignature();
		if (!(sig instanceof MethodSignature)) {
			throw new InvalidAnnotationException("This annotation is only valid on a method.");
		}
		final MethodSignature msig = (MethodSignature) sig;
		final Object target = jp.getTarget();
		return target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
	}

	protected static String generateObjectId(final Method keyMethod, final Object keyObject) throws Exception {
		final String objectId = (String) keyMethod.invoke(keyObject, null);
		if (objectId == null || objectId.length() < 1) {
			throw new RuntimeException("Got an empty key value from " + keyMethod.getName());
		}
		return objectId;
	}

    protected static final char[] WS = new char[] {' ', '\n', '\t'};
    protected static String buildCacheKey(final String objectId, final AnnotationData data) {
        if (objectId == null || objectId.length() < 1) {
            throw new InvalidParameterException("Ids for objects in the cache must be at least 1 character long.");
        }
        final StringBuilder result = new StringBuilder(data.getNamespace()).append(SEPARATOR);
        if (StringUtils.isNotBlank(data.getKeyPrefix())) {
            result.append(data.getKeyPrefix());
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

    @Deprecated
    protected Object getIndexObject(final int index,
	                             final JoinPoint jp,
	                             final Method methodToCache) throws Exception {
        if (index < 0) {
            throw new InvalidParameterException(String.format(
					"An index of %s is invalid",
					index));
        }
        final Object[] args = jp.getArgs();
		if (args.length <= index) {
			throw new InvalidParameterException(String.format(
					"An index of %s is too big for the number of arguments in [%s]",
					index,
					methodToCache.toString()));
		}
		final Object indexObject = args[index];
		if (indexObject == null) {
			throw new InvalidParameterException(String.format(
					"The argument passed into [%s] at index %s is null.",
					methodToCache.toString(),
					index));
		}
		return indexObject;
	}

    protected static Object getIndexObject(final int index,
                                           final Object retVal,
                                           final Object[] args,
                                           final String methodString) throws Exception {
        return getIndexObject(true, index, retVal, args, methodString);
	}

    protected static Object getIndexObject(final int index,
                                           final Object[] args,
                                           final String methodString) throws Exception {
        return getIndexObject(false, index, null, args, methodString);
	}

    private static Object getIndexObject(final boolean allowRetVal,
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

	protected Object validateReturnValueAsKeyObject(final Object returnValue,
                                             final Method methodToCache) throws Exception {
		if (returnValue == null) {
			throw new InvalidParameterException(String.format(
					"The result of the method [%s] is null, which will not give an appropriate cache key.",
					methodToCache.toString()));
		}
		return returnValue;
	}

	protected Method getKeyMethod(final Object keyObject) throws NoSuchMethodException {
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

	protected void validateAnnotationExists(final Object annotation, final Class annotationClass) {
		if (annotation == null) {
			throw new InvalidParameterException(String.format(
					"No annotation of type [%s] found.",
					annotationClass.getName()
			));
		}
	}

	protected void validateAnnotationIndex(final int value,
	                                       final boolean acceptNegOne,
	                                       final Class annotationClass,
	                                       final Method method) {
		if (value < -1 || (!acceptNegOne && value < 0)) {
			throw new InvalidParameterException(String.format(
					"KeyIndex for annotation [%s] must be %s or greater on [%s]",
					annotationClass.getName(),
					acceptNegOne ? "-1" : "0",
					method.toString()
			));
		}
	}

	public void validateAnnotationNamespace(final String value,
	                                        final Class annotationClass,
	                                        final Method method) {
		if (AnnotationConstants.DEFAULT_STRING.equals(value)
				|| value == null
				|| value.length() < 1) {
			throw new InvalidParameterException(String.format(
					"Namespace for annotation [%s] must be defined on [%s]",
					annotationClass.getName(),
					method.toString()
			));
		}
	}

	public void validateAnnotationExpiration(final int value,
	                                         final Class annotationClass,
	                                         final Method method) {
		if (value < 0) {
			throw new InvalidParameterException(String.format(
					"Expiration for annotation [%s] must be 0 or greater on [%s]",
					annotationClass.getName(),
					method.toString()
			));
		}
	}

	// TODO: Replace by List.class.isInstance(Object obj)
	protected void verifyReturnTypeIsList(final Method method, final Class annotationClass) {
		if (verifyTypeIsList(method.getReturnType())) { return; }
		throw new InvalidAnnotationException(String.format(
				"The annotation [%s] is only valid on a method that returns a [%s]. " +
				"[%s] does not fulfill this requirement.",
				ReadThroughMultiCache.class.getName(),
				List.class.getName(),
				method.toString()
		));
	}

	// TODO: Replace by List.class.isInstance(Object obj)
	protected boolean verifyTypeIsList(final Class clazz) {
		if (List.class.equals(clazz)) { return true; }
		final Type[] types = clazz.getGenericInterfaces();
		if (types != null) {
			for (final Type type : types) {
				if (type != null) {
					if (type instanceof ParameterizedType) {
						final ParameterizedType ptype = (ParameterizedType) type;
						if (List.class.equals(ptype.getRawType())) { return true; }
					} else {
						if (List.class.equals(type)) { return true; }
					}
				}
			}
		}

		return false;
	}

    @Deprecated
    protected List<String> getCacheKeys(final List<Object> keyObjects,
                                        final AnnotationData annotationData) throws Exception {
        final List<String> results = new ArrayList<String>();
        for (final Object object : keyObjects) {
            final Method keyMethod = getKeyMethod(object);
            final String objectId = generateObjectId(keyMethod, object);
            results.add(buildCacheKey(objectId, annotationData));
        }

        return results;
    }

    protected List<String> getBaseKeys(final List<Object> keyObjects,
                                        final AnnotationData annotationData,
                                        final Object retVal,
                                        final Object[] args) throws Exception {
        final List<String> results = new ArrayList<String>();
        for (int ix = 0; ix < keyObjects.size(); ix++) {
            final Object object = keyObjects.get(ix);
            final String template = annotationData.getKeyTemplate();
            final String base;
            if (StringUtils.isBlank(template)) {
                final Method method = getKeyMethod(object);
                base = generateObjectId(method, object);
            } else {
                final VelocityContext context = new VelocityContext();
                context.put("StringUtils", StringUtils.class);
                context.put("args", args);
                context.put("index", ix);
                context.put("indexObject", object);
                context.put("retVal", retVal);

                final StringWriter writer = new StringWriter(250);
                Velocity.evaluate(context, writer, "", template);
                base = writer.toString();
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
}
