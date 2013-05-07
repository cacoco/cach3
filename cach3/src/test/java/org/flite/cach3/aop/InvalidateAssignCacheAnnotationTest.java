package org.flite.cach3.aop;

import net.vidageek.mirror.dsl.Mirror;
import org.flite.cach3.annotations.AnnotationConstants;
import org.flite.cach3.annotations.AnnotationsTest;
import org.flite.cach3.annotations.InvalidateAssignCache;
import org.flite.cach3.annotations.L2InvalidateAssignCache;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.testng.AssertJUnit.*;

/**
 * Copyright (c) 2006-2013 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class InvalidateAssignCacheAnnotationTest {

    private static final String NS = "NS";
    private static final String KEY = "bubba-gump";

    @Test
    public void testNull() {
        try {
            InvalidateAssignCacheAdvice.getAnnotationInfo(null, "bubba");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }
    }

    @Test
    public void testNS() throws Exception {
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            InvalidateAssignCacheAdvice.getAnnotationInfo(m01.getAnnotation(InvalidateAssignCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(InvalidateAssignCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            InvalidateAssignCacheAdvice.getAnnotationInfo(m02.getAnnotation(InvalidateAssignCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(InvalidateAssignCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            L2InvalidateAssignCacheAdvice.getAnnotationInfo(m01.getAnnotation(L2InvalidateAssignCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateAssignCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            L2InvalidateAssignCacheAdvice.getAnnotationInfo(m02.getAnnotation(L2InvalidateAssignCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateAssignCache.class.getName()));
        }
    }

    @Test
    public void testKey() {
        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            InvalidateAssignCacheAdvice.getAnnotationInfo(m04.getAnnotation(InvalidateAssignCache.class), m04.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(InvalidateAssignCache.class.getName()));
        }

        try {
            final Method m06 = new Mirror().on(this.getClass()).reflect().method("m06").withAnyArgs();
            InvalidateAssignCacheAdvice.getAnnotationInfo(m06.getAnnotation(InvalidateAssignCache.class), m06.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(InvalidateAssignCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            L2InvalidateAssignCacheAdvice.getAnnotationInfo(m04.getAnnotation(L2InvalidateAssignCache.class), m04.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateAssignCache.class.getName()));
        }

        try {
            final Method m06 = new Mirror().on(this.getClass()).reflect().method("m06").withAnyArgs();
            L2InvalidateAssignCacheAdvice.getAnnotationInfo(m06.getAnnotation(L2InvalidateAssignCache.class), m06.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateAssignCache.class.getName()));
        }

    }

    @Test
    public void testGood() {
        final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();

        final AnnotationInfo r1 = InvalidateAssignCacheAdvice.getAnnotationInfo(m07.getAnnotation(InvalidateAssignCache.class), m07.getName());
        assertEquals(NS, r1.getAsString(AType.NAMESPACE));
        assertEquals(KEY, r1.getAsString(AType.ASSIGN_KEY));

        final AnnotationInfo r2 = L2InvalidateAssignCacheAdvice.getAnnotationInfo(m07.getAnnotation(L2InvalidateAssignCache.class), m07.getName());
        assertEquals(NS, r2.getAsString(AType.NAMESPACE));
        assertEquals(KEY, r2.getAsString(AType.ASSIGN_KEY));

        // Make sure no other unexpected annotation datas are defined.
        final List<String> types = Arrays.asList(AType.KEY_INDEX,
                AType.KEY_TEMPLATE, AType.KEY_PREFIX, AType.WINDOW,
                AType.DATA_INDEX, AType.EXPIRATION, AType.JITTER);

        for (final String type : types) {
            assertNull(type, r1.get(type));
            assertNull(type, r2.get(type));
        }
    }

    @L2InvalidateAssignCache(namespace = AnnotationConstants.DEFAULT_STRING, assignedKey = KEY)
    @InvalidateAssignCache(namespace = AnnotationConstants.DEFAULT_STRING, assignedKey = KEY)
    public String m01() { return null; }

    @L2InvalidateAssignCache(namespace = "", assignedKey = KEY)
    @InvalidateAssignCache(namespace = "", assignedKey = KEY)
    public String m02() { return null; }

//    // Seems we can't assign a null. So, maybe the test is not necessary.
//    @InvalidateAssignCache(namespace = null, assignedKey = "bubba")
//    public String m03() { return null; }


    @L2InvalidateAssignCache(namespace = NS, assignedKey = AnnotationConstants.DEFAULT_STRING)
    @InvalidateAssignCache(namespace = NS, assignedKey = AnnotationConstants.DEFAULT_STRING)
    public String m04() { return null; }

//    // Seems we can't assign a null. So, maybe the test is not necessary.
//    @InvalidateAssignCache(namespace = NS, assignedKey = null)
//    public String m05() { return null; }

    @L2InvalidateAssignCache(namespace = NS, assignedKey = "")
    @InvalidateAssignCache(namespace = NS, assignedKey = "")
    public String m06() { return null; }

    @L2InvalidateAssignCache(namespace = NS, assignedKey = KEY)
    @InvalidateAssignCache(namespace = NS, assignedKey = KEY)
    public String m07() { return null; }

}
