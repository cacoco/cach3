package org.flite.cach3.config;

import org.apache.commons.lang.math.*;
import org.flite.cach3.annotations.UpdateAssignCache;
import org.flite.cach3.api.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

import static org.testng.AssertJUnit.*;

/**
 * Copyright (c) 2011 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

public class ConfigurationHelperTest {

    @Test
    public void testSetDisabled() {
        try {
            ConfigurationHelper.setCacheDisabled(null, true);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }

        final Cach3State state = new Cach3State();
        for (int ix = 0; ix < 3; ix++) {
            final boolean result = RandomUtils.nextBoolean();
            ConfigurationHelper.setCacheDisabled(state, result);
            assertEquals(result, state.isCacheDisabled());
        }
    }

    @Test
    public void testSetListeners() {

        try {
            ConfigurationHelper.addCacheListeners(null, null);
            fail("Expected Exception");
        } catch (InvalidParameterException ex) { }

        final Cach3State state = new Cach3State();

        final Map<Class, Integer> classes = new HashMap<Class, Integer>();
        classes.put(InvalidateAssignCacheListener.class, 1 + RandomUtils.nextInt(10));
        classes.put(InvalidateSingleCacheListener.class, 1 + RandomUtils.nextInt(10));
        classes.put(InvalidateMultiCacheListener.class, 1 + RandomUtils.nextInt(10));
        classes.put(ReadThroughAssignCacheListener.class, 1 + RandomUtils.nextInt(10));
        classes.put(ReadThroughSingleCacheListener.class, 1 + RandomUtils.nextInt(10));
        classes.put(ReadThroughMultiCacheListener.class, 1 + RandomUtils.nextInt(10));
        classes.put(UpdateAssignCacheListener.class, 1 + RandomUtils.nextInt(10));
        classes.put(UpdateSingleCacheListener.class, 1 + RandomUtils.nextInt(10));
        classes.put(UpdateMultiCacheListener.class, 1 + RandomUtils.nextInt(10));

        classes.put(CacheListener.class, 1 + RandomUtils.nextInt(10));

        final List<CacheListener> listeners = new ArrayList<CacheListener>();

        for (Map.Entry<Class, Integer> entry : classes.entrySet()) {
            final Class clazz = entry.getKey();
            for (int ix = 0; ix < ((Integer)entry.getValue()).intValue(); ix++) {
                final CacheListener listener = (CacheListener) Proxy.newProxyInstance(CacheListener.class.getClassLoader(),
                        new Class[] {clazz},
                        new MyInvocationHandler());
                listeners.add(listener);
            }
        }
        Collections.shuffle(listeners);

        ConfigurationHelper.addCacheListeners(state, listeners.toArray(new CacheListener[] {}));

        assertEquals(classes.get(InvalidateAssignCacheListener.class).intValue(), state.getIAListeners().size());
        assertEquals(classes.get(InvalidateSingleCacheListener.class).intValue(), state.getISListeners().size());
        assertEquals(classes.get(InvalidateMultiCacheListener.class).intValue(), state.getIMListeners().size());
        assertEquals(classes.get(ReadThroughAssignCacheListener.class).intValue(), state.getRTAListeners().size());
        assertEquals(classes.get(ReadThroughSingleCacheListener.class).intValue(), state.getRTSListeners().size());
        assertEquals(classes.get(ReadThroughMultiCacheListener.class).intValue(), state.getRTMListeners().size());
        assertEquals(classes.get(UpdateAssignCacheListener.class).intValue(), state.getUAListeners().size());
        assertEquals(classes.get(UpdateSingleCacheListener.class).intValue(), state.getUSListeners().size());
        assertEquals(classes.get(UpdateMultiCacheListener.class).intValue(), state.getUMListeners().size());

    }

    private static class MyInvocationHandler implements InvocationHandler {
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            System.out.println("Name: " + method.getName());
            if ("hashCode".equals(method.getName())) {
                return this.hashCode();
            } else if ("equals".equals(method.getName())) {
                return this.hashCode() == objects[0].hashCode();
            }
            return this.hashCode();
        }
    }
}
