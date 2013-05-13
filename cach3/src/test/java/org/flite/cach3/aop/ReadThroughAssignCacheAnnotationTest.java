package org.flite.cach3.aop;

import net.vidageek.mirror.dsl.Mirror;
import org.apache.commons.lang.math.RandomUtils;
import org.flite.cach3.annotations.*;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Arrays;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * Copyright (c) 2006-2013 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class ReadThroughAssignCacheAnnotationTest {

    private static final String NS = "RTAC";
    private static final String KEY = "Shnikeys";
    private static final int JITTER = 19;
    private static final int EXPIRATION = 119;

    @Test
    public void testNull() {
        try {
            ReadThroughAssignCacheAdvice.getAnnotationInfo(null, "bubba", 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }

        try {
            L2ReadThroughAssignCacheAdvice.getAnnotationInfo(null, "bubba");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }
    }

    @Test
    public void testNS() throws Exception {
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            ReadThroughAssignCacheAdvice.getAnnotationInfo(m01.getAnnotation(ReadThroughAssignCache.class), m01.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughAssignCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            ReadThroughAssignCacheAdvice.getAnnotationInfo(m02.getAnnotation(ReadThroughAssignCache.class), m02.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughAssignCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            L2ReadThroughAssignCacheAdvice.getAnnotationInfo(m01.getAnnotation(L2ReadThroughAssignCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2ReadThroughAssignCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            L2ReadThroughAssignCacheAdvice.getAnnotationInfo(m02.getAnnotation(L2ReadThroughAssignCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2ReadThroughAssignCache.class.getName()));
        }
    }

    @Test
    public void testKey() {
        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            ReadThroughAssignCacheAdvice.getAnnotationInfo(m03.getAnnotation(ReadThroughAssignCache.class), m03.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(ReadThroughAssignCache.class.getName()));
        }

        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            ReadThroughAssignCacheAdvice.getAnnotationInfo(m04.getAnnotation(ReadThroughAssignCache.class), m04.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(ReadThroughAssignCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            L2ReadThroughAssignCacheAdvice.getAnnotationInfo(m03.getAnnotation(L2ReadThroughAssignCache.class), m03.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(L2ReadThroughAssignCache.class.getName()));
        }

        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            L2ReadThroughAssignCacheAdvice.getAnnotationInfo(m04.getAnnotation(L2ReadThroughAssignCache.class), m04.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(L2ReadThroughAssignCache.class.getName()));
        }
    }

    @Test
    public void testExpirationOrWindow() {
        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            ReadThroughAssignCacheAdvice.getAnnotationInfo(m05.getAnnotation(ReadThroughAssignCache.class), m05.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Expiration for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughAssignCache.class.getName()));
        }

        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            L2ReadThroughAssignCacheAdvice.getAnnotationInfo(m05.getAnnotation(L2ReadThroughAssignCache.class), m05.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Window for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2ReadThroughAssignCache.class.getName()));
        }
    }

    @Test
    public void testJitter() {
        try {
            final Method m06 = new Mirror().on(this.getClass()).reflect().method("m06").withAnyArgs();
            ReadThroughAssignCacheAdvice.getAnnotationInfo(m06.getAnnotation(ReadThroughAssignCache.class), m06.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Jitter for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughAssignCache.class.getName()));
        }

        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            ReadThroughAssignCacheAdvice.getAnnotationInfo(m07.getAnnotation(ReadThroughAssignCache.class), m07.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Jitter for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughAssignCache.class.getName()));
        }

        // Jitter Default!
        final int jitterDefault = RandomUtils.nextInt(50) + 10;
        final Method m08 = new Mirror().on(this.getClass()).reflect().method("m08").withAnyArgs();
        final AnnotationInfo r1 = ReadThroughAssignCacheAdvice.getAnnotationInfo(m08.getAnnotation(ReadThroughAssignCache.class), m08.getName(), jitterDefault);
        assertEquals(jitterDefault, r1.getAsInteger(AType.JITTER).intValue());

    }

    @Test
    public void testSuccess() {
        final int jitterDefault = RandomUtils.nextInt(30) + 30;
        final Method m09 = new Mirror().on(this.getClass()).reflect().method("m09").withAnyArgs();

        final AnnotationInfo r1 = ReadThroughAssignCacheAdvice.getAnnotationInfo(m09.getAnnotation(ReadThroughAssignCache.class), m09.getName(), jitterDefault);
        assertEquals(NS, r1.getAsString(AType.NAMESPACE));
        assertEquals(KEY, r1.getAsString(AType.ASSIGN_KEY));
        assertEquals(EXPIRATION, r1.getAsInteger(AType.EXPIRATION).intValue());
        assertEquals(JITTER, r1.getAsInteger(AType.JITTER).intValue());
        AnnotationInfoTest.ensureValuesNotSet(r1,
                Arrays.asList(AType.NAMESPACE, AType.ASSIGN_KEY, AType.EXPIRATION, AType.JITTER));

        final AnnotationInfo r2 = L2ReadThroughAssignCacheAdvice.getAnnotationInfo(m09.getAnnotation(L2ReadThroughAssignCache.class), m09.getName());
        assertEquals(NS, r2.getAsString(AType.NAMESPACE));
        assertEquals(KEY, r2.getAsString(AType.ASSIGN_KEY));
        assertEquals(Duration.ONE_MINUTE, r2.getAsType(AType.WINDOW, Duration.class));
        AnnotationInfoTest.ensureValuesNotSet(r2,
                Arrays.asList(AType.NAMESPACE, AType.ASSIGN_KEY, AType.WINDOW));

    }

    /* * * *  Namespace Tests  * * * */
    @L2ReadThroughAssignCache(namespace = AnnotationConstants.DEFAULT_STRING, window = Duration.ONE_MINUTE)
    @ReadThroughAssignCache(namespace = AnnotationConstants.DEFAULT_STRING)
    public String m01() { return null; }

    @L2ReadThroughAssignCache(namespace = "", window = Duration.ONE_MINUTE)
    @ReadThroughAssignCache(namespace = "")
    public String m02() { return null; }

    /* * * *  AssignedKey Tests  * * * */
    @L2ReadThroughAssignCache(namespace = NS, assignedKey = AnnotationConstants.DEFAULT_STRING, window = Duration.ONE_MINUTE)
    @ReadThroughAssignCache(namespace = NS, assignedKey = AnnotationConstants.DEFAULT_STRING)
    public String m03() { return null; }

    @L2ReadThroughAssignCache(namespace = NS, assignedKey = "", window = Duration.ONE_MINUTE)
    @ReadThroughAssignCache(namespace = NS, assignedKey = "")
    public String m04() { return null; }

    /* * * *  Expiration/Window Tests  * * * */
    @L2ReadThroughAssignCache(namespace = NS, assignedKey = KEY, window = Duration.UNDEFINED)
    @ReadThroughAssignCache(namespace = NS, assignedKey = KEY, expiration = -1)
    public String m05() { return null; }

    /* * * *  Jitter Tests  * * * */
    @ReadThroughAssignCache(namespace = NS, assignedKey = KEY, jitter = -2)
    public String m06() { return null; }

    @ReadThroughAssignCache(namespace = NS, assignedKey = KEY, jitter = 100)
    public String m07() { return null; }

    @ReadThroughAssignCache(namespace = NS, assignedKey = KEY, jitter = -1)
    public String m08() { return null; }

    /* * * *  Success Tests  * * * */
    @L2ReadThroughAssignCache(namespace = NS, assignedKey = KEY, window = Duration.ONE_MINUTE)
    @ReadThroughAssignCache(namespace = NS, assignedKey = KEY, expiration = EXPIRATION, jitter = JITTER)
    public String m09() { return null; }

}
