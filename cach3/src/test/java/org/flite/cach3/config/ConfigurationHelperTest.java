package org.flite.cach3.config;

import net.spy.memcached.*;
import org.apache.commons.lang.math.*;
import org.flite.cach3.api.*;
import org.testng.annotations.*;

import java.security.*;

import static org.testng.AssertJUnit.*;

/**
Copyright (c) 2011-2012 Flite, Inc

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
public class ConfigurationHelperTest {

    @Test
    public void testSetDisabled() throws Exception {
        try {
            ConfigurationHelper.setCacheDisabled(null, true);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }

        final Cach3State state = new Cach3State();
        state.setProvider(new MemcachedClientProvider() {
            public MemcachedClientIF getMemcachedClient() {
                return null;
            }
        });

        for (int ix = 0; ix < 3; ix++) {
            final boolean result = RandomUtils.nextBoolean();
            ConfigurationHelper.setCacheDisabled(state, result);
            assertEquals(result, state.isCacheDisabled());
        }
    }

    @Test
    public void testSetJitterDefault() throws Exception {
        try {
            ConfigurationHelper.setJitterDefault(null, 10);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }

        final Cach3State state = new Cach3State();
        state.setProvider(new MemcachedClientProvider() {
            public MemcachedClientIF getMemcachedClient() {
                return null;
            }
        });

        for (int ix = 0; ix < 3; ix++) {
            final int result = RandomUtils.nextInt(99);
            ConfigurationHelper.setJitterDefault(state, result);
            assertEquals(result, state.getJitterDefault());
        }
    }

//    @Test
//    public void testSetListeners() {
//
//        try {
//            ConfigurationHelper.addCacheListeners(null, new CacheListener[] {});
//            fail("Expected Exception");
//        } catch (InvalidParameterException ex) { }
//
//        final Cach3State state = new Cach3State();
//
//        final Map<Class, Integer> classes = new HashMap<Class, Integer>();
//        classes.put(InvalidateAssignCacheListener.class, 1 + RandomUtils.nextInt(10));
//        classes.put(InvalidateSingleCacheListener.class, 1 + RandomUtils.nextInt(10));
//        classes.put(InvalidateMultiCacheListener.class, 1 + RandomUtils.nextInt(10));
//        classes.put(ReadThroughAssignCacheListener.class, 1 + RandomUtils.nextInt(10));
//        classes.put(ReadThroughSingleCacheListener.class, 1 + RandomUtils.nextInt(10));
//        classes.put(ReadThroughMultiCacheListener.class, 1 + RandomUtils.nextInt(10));
//        classes.put(UpdateAssignCacheListener.class, 1 + RandomUtils.nextInt(10));
//        classes.put(UpdateSingleCacheListener.class, 1 + RandomUtils.nextInt(10));
//        classes.put(UpdateMultiCacheListener.class, 1 + RandomUtils.nextInt(10));
//
//        classes.put(CacheListener.class, 1 + RandomUtils.nextInt(10));
//
//        final List<CacheListener> listeners = new ArrayList<CacheListener>();
//
//        for (Map.Entry<Class, Integer> entry : classes.entrySet()) {
//            final Class clazz = entry.getKey();
//            for (int ix = 0; ix < ((Integer)entry.getValue()).intValue(); ix++) {
//                final CacheListener listener = (CacheListener) Proxy.newProxyInstance(CacheListener.class.getClassLoader(),
//                        new Class[] {clazz},
//                        new MyInvocationHandler());
//                listeners.add(listener);
//            }
//        }
//        listeners.add(null);
//        Collections.shuffle(listeners);
//
//        ConfigurationHelper.addCacheListeners(state, listeners.toArray(new CacheListener[] {}));
//
//        assertEquals(classes.get(InvalidateAssignCacheListener.class).intValue(), state.getListeners(InvalidateAssignCacheListener.class).size());
//        assertEquals(classes.get(InvalidateSingleCacheListener.class).intValue(), state.getListeners(InvalidateSingleCacheListener.class).size());
//        assertEquals(classes.get(InvalidateMultiCacheListener.class).intValue(), state.getListeners(InvalidateMultiCacheListener.class).size());
//        assertEquals(classes.get(ReadThroughAssignCacheListener.class).intValue(), state.getListeners(ReadThroughAssignCacheListener.class).size());
//        assertEquals(classes.get(ReadThroughSingleCacheListener.class).intValue(), state.getListeners(ReadThroughSingleCacheListener.class).size());
//        assertEquals(classes.get(ReadThroughMultiCacheListener.class).intValue(), state.getListeners(ReadThroughMultiCacheListener.class).size());
//        assertEquals(classes.get(UpdateAssignCacheListener.class).intValue(), state.getListeners(UpdateAssignCacheListener.class).size());
//        assertEquals(classes.get(UpdateSingleCacheListener.class).intValue(), state.getListeners(UpdateSingleCacheListener.class).size());
//        assertEquals(classes.get(UpdateMultiCacheListener.class).intValue(), state.getListeners(UpdateMultiCacheListener.class).size());
//    }
//
//    private static class MyInvocationHandler implements InvocationHandler {
//        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
//            if ("hashCode".equals(method.getName())) {
//                return this.hashCode();
//            } else if ("equals".equals(method.getName())) {
//                return this.hashCode() == objects[0].hashCode();
//            }
//            return this.hashCode();
//        }
//    }
}
