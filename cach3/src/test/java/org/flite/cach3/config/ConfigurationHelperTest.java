package org.flite.cach3.config;

import org.apache.commons.lang.math.*;
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

        for (Map.Entry entry : classes.entrySet()) {
            
        }



        final CacheListener l1 = (CacheListener) Proxy.newProxyInstance(CacheListener.class.getClassLoader(),
                new Class[] {CacheListener.class},
                new MyInvocationHandler());

        final CacheListener l2 = (CacheListener) Proxy.newProxyInstance(CacheListener.class.getClassLoader(),
                new Class[] {CacheListener.class},
                new MyInvocationHandler());

        assertFalse(l1 == l2);
        assertFalse(l1.hashCode() == l2.hashCode());
        assertFalse(l1.equals(l2));
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
