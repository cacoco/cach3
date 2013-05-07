package org.flite.cach3.aop;

import net.vidageek.mirror.dsl.Mirror;
import org.flite.cach3.annotations.AnnotationConstants;
import org.flite.cach3.annotations.InvalidateAssignCache;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

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
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            InvalidateAssignCacheAdvice.getAnnotationInfo(m03.getAnnotation(InvalidateAssignCache.class), m03.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(InvalidateAssignCache.class.getName()));
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
    }

    @Test
    public void testGood() {
        final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
        final AnnotationInfo result = InvalidateAssignCacheAdvice.getAnnotationInfo(m07.getAnnotation(InvalidateAssignCache.class), m07.getName());

        assertEquals(NS, result.getAsString(AType.NAMESPACE));
        assertEquals(KEY, result.getAsString(AType.ASSIGN_KEY));

        final List<String> types = Arrays.asList(AType.KEY_INDEX,
                AType.KEY_TEMPLATE, AType.KEY_PREFIX, AType.WINDOW,
                AType.DATA_INDEX, AType.EXPIRATION, AType.JITTER);

        for (final String type : types) {
            assertNull(type, result.get(type));
        }
    }

    @InvalidateAssignCache(namespace = AnnotationConstants.DEFAULT_STRING, assignedKey = KEY)
    public String m01() { return null; }

//    // Seems we can't assign a null. So, maybe the test is not necessary.
//    @InvalidateAssignCache(namespace = null, assignedKey = "bubba")
//    public String m02() { return null; }

    @InvalidateAssignCache(namespace = "", assignedKey = KEY)
    public String m03() { return null; }

    @InvalidateAssignCache(namespace = NS, assignedKey = AnnotationConstants.DEFAULT_STRING)
    public String m04() { return null; }

//    // Seems we can't assign a null. So, maybe the test is not necessary.
//    @InvalidateAssignCache(namespace = NS, assignedKey = null)
//    public String m05() { return null; }

    @InvalidateAssignCache(namespace = NS, assignedKey = "")
    public String m06() { return null; }

    @InvalidateAssignCache(namespace = NS, assignedKey = KEY)
    public String m07() { return null; }


}
