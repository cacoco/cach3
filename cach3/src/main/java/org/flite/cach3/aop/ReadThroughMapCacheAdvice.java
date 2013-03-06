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

import com.google.common.collect.ArrayTable;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.flite.cach3.annotations.ReadThroughMapCache;
import org.flite.cach3.api.CacheConditionally;
import org.flite.cach3.api.ReadThroughMultiCacheListener;
import org.flite.cach3.config.VelocityContextFactory;
import org.flite.cach3.exceptions.InvalidAnnotationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.*;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE / 2)
public class ReadThroughMapCacheAdvice extends CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(ReadThroughMapCacheAdvice.class);

    private static final String COL_KEY_OBJ = "KeyObjects";
    private static final String COL_RETRIEVED = "Retrieved";
    private static final String COL_RESULTS = "Results";
    private static final List<String> COL_NAMES = Arrays.asList(COL_KEY_OBJ, COL_RESULTS, COL_RETRIEVED);

    @Pointcut("@annotation(org.flite.cach3.annotations.ReadThroughMapCache)")
    public void getMap() {}

    @Around("getMap()")
    public Object cacheMap(final ProceedingJoinPoint pjp) throws Throwable {
        // If we've disabled the caching programmatically (or via properties file) just flow through.
        if (isCacheDisabled()) {
            LOG.debug("Caching is disabled.");
            return pjp.proceed();
        }

        final MemcachedClientIF cache = getMemcachedClient();
        // This is injected caching.  If anything goes wrong in the caching, LOG the crap outta it,
        // but do not let it surface up past the AOP injection itself.
//        final MapCacheCoordinator coord = new MapCacheCoordinator();
        final ArrayTable<String, String, Object> dataTable;
        AnnotationData data;
        Object [] args = pjp.getArgs();
        try {
            // Get the target method being invoked, and make sure it returns the correct info.
            final Method method = getMethodToCache(pjp);
            verifyReturnTypeIsList(method, ReadThroughMapCache.class);

            // Get the annotation associated with this method, and make sure the values are valid.
            final ReadThroughMapCache annotation = method.getAnnotation(ReadThroughMapCache.class);

            data = AnnotationDataBuilder.buildAnnotationData(
                    annotation, ReadThroughMapCache.class, method.getName(), getJitterDefault());

            // Get the list of objects that will provide the keys to all the cache values.
            final Iterable<Object> keyObjects = getKeyObjects(data.getKeyIndex(), pjp, method);
            final Map<String, Object> keyMap = convertIdObjectsToKeyMap(
                    keyObjects,
                    data.getNamespace(),
                    data.getKeyPrefix(),
                    data.getKeyTemplate(),
                    factory,
                    methodStore,
                    args);
            dataTable = ArrayTable.create(keyMap.keySet(), COL_NAMES);
            for (final Map.Entry<String, Object> entry : keyMap.entrySet()) {
                dataTable.put(entry.getKey(), COL_KEY_OBJ, entry.getValue());
            }

            // Get the full list of cache keys and ask the cache for the corresponding values.
            final Map<String, Object> retrieved = cache.getBulk(dataTable.rowKeyList());
            for (final Map.Entry<String, Object> entry : retrieved.entrySet()) {
                final Object value = entry.getValue();
                dataTable.put(entry.getKey(), COL_RETRIEVED, value);
                dataTable.put(entry.getKey(), COL_RESULTS, value instanceof PertinentNegativeNull ? null : value);
            }

            // If we've gotten all positive cache results back, build up a results list and return it.
            if (!dataTable.column(COL_RETRIEVED).containsValue(null)) {
                return generateResultMap(dataTable);
            }

            // TODO: !!!! Need to use the KeyObjects as the row identifier. That's the main way things are used.
            final Set<Object> new
            // Create the new list of arguments with a subset of the key objects that aren't in the cache.
            args = coord.modifyArgumentList(args, data.getKeyIndex());
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
                            data.getJitteredExpiration(),
                            resultObject);
                }
                coord.getKey2Result().put(cacheKey, resultObject);
                cacheBaseIds[ix] = cacheBase;
            }

            // Notify the observers that a cache interaction happened.
            final List<ReadThroughMultiCacheListener> listeners = getPertinentListeners(ReadThroughMultiCacheListener.class, data.getNamespace());
            if (listeners != null && !listeners.isEmpty()) {
                for (final ReadThroughMultiCacheListener listener : listeners) {
                    try {
                        listener.triggeredReadThroughMultiCache(data.getNamespace(),
                                data.getKeyPrefix(),
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
            LOG.warn("Caching on " + pjp.toShortString()
                    + " aborted due to an error. The underlying method will be called twice.", ex);
            return pjp.proceed();
        }
    }

    public static Iterable<Object> getKeyObjects(final int keyIndex,
                                                 final JoinPoint jp,
                                                 final Method method) throws Exception {
        final Object keyObjects = getIndexObject(keyIndex, jp.getArgs(), method.toString());
        if (verifyTypeIsIterable(keyObjects.getClass())) { return (Iterable<Object>) keyObjects;}
        throw new InvalidAnnotationException(String.format(
                "The parameter object found at keyIndex [%s] is not a [%s], but is of type [%s]. " +
                        "[%s] does not fulfill the requirements.",
                keyIndex,
                Iterable.class.getName(),
                keyObjects.getClass().getName(),
                method.toString()
        ));
    }

    protected static boolean verifyTypeIsIterable(final Class clazz) {
        return java.util.List.class.isAssignableFrom(clazz);
    }

    public static Map<String, Object> convertIdObjectsToKeyMap(final Iterable<Object> idObjects,
                                                     final String namespace,
                                                     final String prefix,
                                                     final String template,
                                                     final VelocityContextFactory factory,
                                                     final CacheKeyMethodStore methodStore,
                                                     final Object[] args) throws Exception {
        final Map<String, Object> holder = new HashMap<String, Object>();
        for (final Object obj : idObjects) {
            if (obj == null) { throw new InvalidParameterException("One of the passed in key objects is null"); }

            final String base;
            if (StringUtils.isBlank(template)) {
                final Method method = getKeyMethod(obj,methodStore);
                base = generateObjectId(method, obj);
            } else {
                final VelocityContext context = factory.getNewExtendedContext();
                context.put("args", args);
                context.put("indexObject", obj);

                final StringWriter writer = new StringWriter(250);
                Velocity.evaluate(context, writer, ReadThroughMultiCacheAdvice.class.getSimpleName(), template);
                base = writer.toString();
                if (template.equals(base)) { throw new InvalidParameterException("Calculated key is equal to the velocityTemplate."); }
            }
            final String key = buildCacheKey(base, namespace, prefix);

            holder.put(key, obj);
        }

        return holder;
    }

    protected static Map<Object, Object> generateResultMap(final ArrayTable<String, String, Object> dataTable) {
        final Map<Object, Object> results = new HashMap<Object, Object>();
        final Map<String, Object> keys = dataTable.column(COL_KEY_OBJ);
        final Map<String, Object> vals = dataTable.column(COL_RESULTS);
        for (final Map.Entry<String, Object> entry : keys.entrySet()) {
            final Object newKey = entry.getValue();
            final Object newVal = vals.get(entry.getKey());
            results.put(newKey, newVal);
        }
        return results;
    }

    public static class MapCacheCoordinator {
//        private Method method;
//        private List<Object> keyObjects = new ArrayList<Object>();
//        private Map<String, Object> key2Obj = new HashMap<String, Object>();
//        private Map<Object, String> obj2Key = new HashMap<Object, String>();
//        private Map<Object, String> obj2Base = new HashMap<Object, String>();
//        private Map<String, Object> key2Result = new HashMap<String, Object>();
//        private List<Object> missObjects = new ArrayList<Object>();
//
//        public Method getMethod() {
//            return method;
//        }
//
//        public void setMethod(Method method) {
//            this.method = method;
//        }
//
//        public List<Object> getKeyObjects() {
//            return keyObjects;
//        }
//
//        public void setKeyObjects(List<Object> keyObjects) {
//            this.keyObjects.addAll(keyObjects);
//        }
//
//        public void setHolder(MapHolder holder) {
//            key2Obj.putAll(holder.getKey2Obj());
//            obj2Key.putAll(holder.getObj2Key());
//            obj2Base.putAll(holder.getObj2Base());
//        }
//
//        public Map<String, Object> getKey2Obj() {
//            return key2Obj;
//        }
//
//        public Map<Object, String> getObj2Key() {
//            return obj2Key;
//        }
//
//        public Map<String, Object> getKey2Result() {
//            return key2Result;
//        }
//
//        public void setInitialKey2Result(Map<String, Object> key2Result) {
//            if (key2Result == null) {
//                throw new RuntimeException("There was an error retrieving cache values.");
//            }
//            this.key2Result.putAll(key2Result);
//
//            final Set<Object> missObjectSet = new HashSet<Object>();
//            for (final String key : this.key2Obj.keySet()) {
//                if (this.key2Result.get(key) == null) {
//                    missObjectSet.add(key2Obj.get(key));
//                }
//            }
//            this.missObjects.addAll(missObjectSet);
//        }
//
//        public List<Object> generateResultList() {
//            final List<Object> results = new ArrayList<Object>();
//            for (int ix = 0; ix < keyObjects.size(); ix++) {
//                final Object keyObject = keyObjects.get(ix);
//                final String cacheKey = obj2Key.get(keyObject);
//                final Object keyResult = key2Result.get(cacheKey);
//                if (keyResult == null) {
//                    throw new RuntimeException(String.format(
//                            "Unable to fulfill data for the key item [%s] with key value of [%s].",
//                            keyObject.toString(),
//                            obj2Key.get(keyObject)));
//                }
//                results.add(keyResult instanceof PertinentNegativeNull ? null : keyResult);
//            }
//
//            return results;
//        }
//
//        public List<Object> getMissObjects() {
//            return missObjects;
//        }
//
//        public Object[] modifyArgumentList(final Object[] args, final int keyIndex) {
//            args[keyIndex] = this.missObjects;
//            return args;
//        }
//
//        public Map<Object, String> getObj2Base() {
//            return obj2Base;
//        }
    }
}