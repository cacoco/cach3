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
import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.flite.cach3.config.VelocityContextFactory;
import org.flite.cach3.exceptions.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE / 2)
public class ReadThroughMultiCacheAdvice extends CacheBase {
	private static final Logger LOG = LoggerFactory.getLogger(ReadThroughMultiCacheAdvice.class);

	@Pointcut("@annotation(org.flite.cach3.annotations.ReadThroughMultiCache)")
	public void getMulti() {}

	@Around("getMulti()")
	public Object cacheMulti(final ProceedingJoinPoint pjp) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return pjp.proceed();
        }

        final MemcachedClientIF cache = getMemcachedClient();
		// This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
		// but do not let it surface up past the AOP injection itself.
		final MultiCacheCoordinator coord = new MultiCacheCoordinator();
        AnnotationInfo info;
		Object [] args = pjp.getArgs();
		try {
			// Get the target method being invoked, and make sure it returns the correct info.
			coord.setMethod(getMethodToCache(pjp));
			verifyReturnTypeIsList(coord.getMethod(), ReadThroughMultiCache.class);

			// Get the annotation associated with this method, and make sure the values are valid.
            final ReadThroughMultiCache annotation = coord.getMethod().getAnnotation(ReadThroughMultiCache.class);

            info = getAnnotationInfo(annotation, coord.getMethod().getName(), getJitterDefault());

			// Get the list of objects that will provide the keys to all the cache values.
			coord.setKeyObjects(getKeyObjectList(info.getAsInteger(AType.KEY_INDEX,null), pjp, coord.getMethod()));

			// Create key->object and object->key mappings.
			coord.setHolder(convertIdObjectsToKeyMap(coord.getKeyObjects(),
                    info.getAsString(AType.NAMESPACE),
                    info.getAsString(AType.KEY_PREFIX),
                    info.getAsString(AType.KEY_TEMPLATE),
                    factory,
                    methodStore,
                    args));

			// Get the full list of cache keys and ask the cache for the corresponding values.
			coord.setInitialKey2Result(cache.getBulk(coord.getKey2Obj().keySet()));

			// We've gotten all positive cache results back, so build up a results list and return it.
			if (coord.getMissObjects().size() < 1) {
				return coord.generateResultList();
			}

			// Create the new list of arguments with a subset of the key objects that aren't in the cache.
			args = coord.modifyArgumentList(args, info.getAsInteger(AType.KEY_INDEX, null));
		} catch (Throwable ex) {
            // If there's an exception somewhere in the caching code, then just bail out
            // and call through to the target method with the original parameters.
            if (LOG.isDebugEnabled()) {
                LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error.", ex);
            } else {
                LOG.warn("Caching on " + pjp.toShortString() + " aborted due to an error: " + ex.getMessage());
            }
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
			for (int ix = 0; ix < results.size(); ix++) {
				final Object keyObject = coord.getMissObjects().get(ix);
				final Object resultObject = results.get(ix) == null ? new PertinentNegativeNull() : results.get(ix);
				final String cacheKey = coord.obj2Key.get(keyObject);
                final String cacheBase = coord.obj2Base.get(keyObject);
                boolean cacheable = true;
                if (resultObject instanceof CacheConditionally) {
                    cacheable = ((CacheConditionally) resultObject).isCacheable();
                }
                if (cacheable) {
                    cache.set(cacheKey,
                            calculateJitteredExpiration(info.getAsInteger(AType.EXPIRATION), info.getAsInteger(AType.JITTER)),
                            resultObject);
                }
				coord.getKey2Result().put(cacheKey, resultObject);
                cacheBaseIds[ix] = cacheBase;
			}

            // Notify the observers that a cache interaction happened.
            final List<ReadThroughMultiCacheListener> listeners = getPertinentListeners(ReadThroughMultiCacheListener.class, info.getAsString(AType.NAMESPACE));
            if (listeners != null && !listeners.isEmpty()) {
                for (final ReadThroughMultiCacheListener listener : listeners) {
                    try {
                        listener.triggeredReadThroughMultiCache(info.getAsString(AType.NAMESPACE),
                                info.getAsString(AType.KEY_PREFIX, null),
                                Arrays.asList(cacheBaseIds),
                                results,
                                args);
                    } catch (Exception ex) {
                        LOG.warn("Problem when triggering a listener.", ex);
                    }
                }
            }

			return coord.generateResultList();
		} catch (Throwable ex) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("Caching on " + pjp.toShortString()
                        + " aborted due to an error. (The underlying method will be called twice.)", ex);
            } else {
                LOG.warn("Caching on " + pjp.toShortString()
                        + " aborted due to an error. (The underlying method will be called twice.): "
                        + ex.getMessage());
            }
			return pjp.proceed();
		}
	}

    public static MapHolder convertIdObjectsToKeyMap(final List<Object> idObjects,
                                                 final String namespace,
                                                 final String prefix,
                                                 final String template,
                                                 final VelocityContextFactory factory,
                                                 final CacheKeyMethodStore methodStore,
                                                 final Object[] args) throws Exception {
        final MapHolder holder = new MapHolder();
        for (int ix = 0; ix < idObjects.size(); ix++) {
            final Object obj = idObjects.get(ix);
            if (obj == null) { throw new InvalidParameterException("One of the passed in key objects is null"); }

            final String base;
            if (StringUtils.isBlank(template)) {
                final Method method = getKeyMethod(obj,methodStore);
                base = generateObjectId(method, obj);
            } else {
                final VelocityContext context = factory.getNewExtendedContext();
                context.put("args", args);
                context.put("index", ix);
                context.put("indexObject", obj);

                final StringWriter writer = new StringWriter(250);
                Velocity.evaluate(context, writer, ReadThroughMultiCacheAdvice.class.getSimpleName() , template);
                base = writer.toString();
                if (template.equals(base)) { throw new InvalidParameterException("Calculated key is equal to the velocityTemplate."); }
            }
            final String key = buildCacheKey(base, namespace, prefix);

            if (holder.getObj2Key().get(obj) == null) {
                holder.getObj2Key().put(obj, key);
                holder.getObj2Base().put(obj, base);
            }
            if (holder.getKey2Obj().get(key) == null) {
                holder.getKey2Obj().put(key, obj);
            }
        }

        return holder;
    }

    public static List<Object> getKeyObjectList(final int keyIndex,
	                                            final JoinPoint jp,
	                                            final Method method) throws Exception {
        final Object keyObjects = getIndexObject(keyIndex, jp.getArgs(), method.toString());
		if (verifyTypeIsList(keyObjects.getClass())) { return (List<Object>) keyObjects;}
		throw new InvalidAnnotationException(String.format(
				"The parameter object found at keyIndex [%s] is not a [%s], but is of type [%s]. " +
				"[%s] does not fulfill the requirements.",
				keyIndex,
				List.class.getName(),
                keyObjects.getClass().getName(),
				method.toString()
		));
	}

	public static class MapHolder {
		final Map<String, Object> key2Obj = new HashMap<String, Object>();
		final Map<Object, String> obj2Key = new HashMap<Object, String>();
        final Map<Object, String> obj2Base = new HashMap<Object, String>();

		public Map<String, Object> getKey2Obj() {
			return key2Obj;
		}

        public Map<Object, String> getObj2Key() {
			return obj2Key;
		}

        public Map<Object, String> getObj2Base() {
            return obj2Base;
        }
    }

	public static class MultiCacheCoordinator {
		private Method method;
        private List<Object> keyObjects = new ArrayList<Object>();
		private Map<String, Object> key2Obj = new HashMap<String, Object>();
		private Map<Object, String> obj2Key = new HashMap<Object, String>();
        private Map<Object, String> obj2Base = new HashMap<Object, String>();
		private Map<String, Object> key2Result = new HashMap<String, Object>();
		private List<Object> missObjects = new ArrayList<Object>();

		public Method getMethod() {
			return method;
		}

		public void setMethod(Method method) {
			this.method = method;
		}

        public List<Object> getKeyObjects() {
			return keyObjects;
		}

		public void setKeyObjects(List<Object> keyObjects) {
			this.keyObjects.addAll(keyObjects);
		}

		public void setHolder(MapHolder holder) {
			key2Obj.putAll(holder.getKey2Obj());
			obj2Key.putAll(holder.getObj2Key());
            obj2Base.putAll(holder.getObj2Base());
		}

		public Map<String, Object> getKey2Obj() {
			return key2Obj;
		}

		public Map<Object, String> getObj2Key() {
			return obj2Key;
		}

		public Map<String, Object> getKey2Result() {
			return key2Result;
		}

		public void setInitialKey2Result(Map<String, Object> key2Result) {
			if (key2Result == null) {
				throw new RuntimeException("There was an error retrieving cache values.");
			}
			this.key2Result.putAll(key2Result);

			final Set<Object> missObjectSet = new HashSet<Object>();
			for (final String key : this.key2Obj.keySet()) {
				if (this.key2Result.get(key) == null) {
					missObjectSet.add(key2Obj.get(key));
				}
			}
			this.missObjects.addAll(missObjectSet);
		}

		public List<Object> generateResultList() {
			final List<Object> results = new ArrayList<Object>();
			for (int ix = 0; ix < keyObjects.size(); ix++) {
				final Object keyObject = keyObjects.get(ix);
				final String cacheKey = obj2Key.get(keyObject);
				final Object keyResult = key2Result.get(cacheKey);
				if (keyResult == null) {
					throw new RuntimeException(String.format(
							"Unable to fulfill data for the key item [%s] with key value of [%s].",
							keyObject.toString(),
							obj2Key.get(keyObject)));
				}
				results.add(keyResult instanceof PertinentNegativeNull ? null : keyResult);
			}

			return results;
		}

		public List<Object> getMissObjects() {
			return missObjects;
		}

		public Object[] modifyArgumentList(final Object[] args, final int keyIndex) {
			args[keyIndex] = this.missObjects;
			return args;
		}

        public Map<Object, String> getObj2Base() {
            return obj2Base;
        }
    }

    static AnnotationInfo getAnnotationInfo(final ReadThroughMultiCache annotation,
                                            final String targetMethodName,
                                            final int jitterDefault) {
        final AnnotationInfo result = new AnnotationInfo();

        if (annotation == null) {
            throw new InvalidParameterException(String.format(
                    "No annotation of type [%s] found.",
                    ReadThroughMultiCache.class.getName()
            ));
        }

        final String namespace = annotation.namespace();
        if (AnnotationConstants.DEFAULT_STRING.equals(namespace)
                || namespace == null
                || namespace.length() < 1) {
            throw new InvalidParameterException(String.format(
                    "Namespace for annotation [%s] must be defined on [%s]",
                    ReadThroughMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Namespace(namespace));

        final String keyPrefix = annotation.keyPrefix();
        if (!AnnotationConstants.DEFAULT_STRING.equals(keyPrefix)) {
            if (StringUtils.isBlank(keyPrefix)) {
                throw new InvalidParameterException(String.format(
                        "KeyPrefix for annotation [%s] must not be defined as an empty string on [%s]",
                        ReadThroughMultiCache.class.getName(),
                        targetMethodName
                ));
            }
            result.add(new AType.KeyPrefix(keyPrefix));
        }

        final Integer keyIndex = annotation.keyIndex();
        if (keyIndex < 0) {
            throw new InvalidParameterException(String.format(
                    "KeyIndex for annotation [%s] must be 0 or greater on [%s]",
                    ReadThroughMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.KeyIndex(keyIndex));

        final String keyTemplate = annotation.keyTemplate();
        if (!AnnotationConstants.DEFAULT_STRING.equals(keyTemplate)) {
            if (StringUtils.isBlank(keyTemplate)) {
                throw new InvalidParameterException(String.format(
                        "KeyTemplate for annotation [%s] must not be defined as an empty string on [%s]",
                        ReadThroughMultiCache.class.getName(),
                        targetMethodName
                ));
            }
            result.add(new AType.KeyTemplate(keyTemplate));
        }

        final int expiration = annotation.expiration();
        if (expiration < 0) {
            throw new InvalidParameterException(String.format(
                    "Expiration for annotation [%s] must be 0 or greater on [%s]",
                    ReadThroughMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Expiration(expiration));

        final int jitter = annotation.jitter();
        if (jitter < -1 || jitter > 99) {
            throw new InvalidParameterException(String.format(
                    "Jitter for annotation [%s] must be -1 <= jitter <= 99 on [%s]",
                    ReadThroughMultiCache.class.getName(),
                    targetMethodName
            ));
        }
        result.add(new AType.Jitter(jitter == -1 ? jitterDefault : jitter));

        return result;
    }

}
