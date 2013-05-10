package org.flite.cach3.aop;

import net.vidageek.mirror.dsl.Mirror;
import org.apache.commons.lang.math.RandomUtils;
import org.flite.cach3.annotations.AnnotationConstants;
import org.flite.cach3.annotations.Duration;
import org.flite.cach3.annotations.L2UpdateAssignCache;
import org.flite.cach3.annotations.UpdateAssignCache;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Arrays;

import static org.testng.AssertJUnit.*;

/**
 * Copyright (c) 2006-2013 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class UpdateAssignCacheAnnotationTest {
    private static final String NS = "UAC";
    private static final String KEY = "CaptainCaveMaaaan";
    private static final int INDEX = 7;
    private static final int EXPIRATION = 1337;
    private static final int JITTER = 47;

    @Test
    public void testNull() {
        try {
            UpdateAssignCacheAdvice.getAnnotationInfo(null, "bubba", 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }

        try {
            L2UpdateAssignCacheAdvice.getAnnotationInfo(null, "bubba");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }
    }

    @Test
    public void testNS() throws Exception {
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            UpdateAssignCacheAdvice.getAnnotationInfo(m01.getAnnotation(UpdateAssignCache.class), m01.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(UpdateAssignCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            UpdateAssignCacheAdvice.getAnnotationInfo(m02.getAnnotation(UpdateAssignCache.class), m02.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(UpdateAssignCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            L2UpdateAssignCacheAdvice.getAnnotationInfo(m01.getAnnotation(L2UpdateAssignCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2UpdateAssignCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            L2UpdateAssignCacheAdvice.getAnnotationInfo(m02.getAnnotation(L2UpdateAssignCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2UpdateAssignCache.class.getName()));
        }
    }

    @Test
    public void testKey() {
        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            UpdateAssignCacheAdvice.getAnnotationInfo(m03.getAnnotation(UpdateAssignCache.class), m03.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(UpdateAssignCache.class.getName()));
        }

        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            UpdateAssignCacheAdvice.getAnnotationInfo(m04.getAnnotation(UpdateAssignCache.class), m04.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(UpdateAssignCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            L2UpdateAssignCacheAdvice.getAnnotationInfo(m03.getAnnotation(L2UpdateAssignCache.class), m03.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(L2UpdateAssignCache.class.getName()));
        }

        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            L2UpdateAssignCacheAdvice.getAnnotationInfo(m04.getAnnotation(L2UpdateAssignCache.class), m04.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("AssignedKey for annotation"));
            assertTrue(ex.getMessage().contains(L2UpdateAssignCache.class.getName()));
        }
    }

    @Test
    public void testDataIndex() {
        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            UpdateAssignCacheAdvice.getAnnotationInfo(m05.getAnnotation(UpdateAssignCache.class), m05.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("DataIndex for annotation"));
            assertTrue(ex.getMessage().contains(UpdateAssignCache.class.getName()));
        }

        try {
            final Method m06 = new Mirror().on(this.getClass()).reflect().method("m06").withAnyArgs();
            UpdateAssignCacheAdvice.getAnnotationInfo(m06.getAnnotation(UpdateAssignCache.class), m06.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("DataIndex for annotation"));
            assertTrue(ex.getMessage().contains(UpdateAssignCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            L2UpdateAssignCacheAdvice.getAnnotationInfo(m05.getAnnotation(L2UpdateAssignCache.class), m05.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("DataIndex for annotation"));
            assertTrue(ex.getMessage().contains(L2UpdateAssignCache.class.getName()));
        }

        try {
            final Method m06 = new Mirror().on(this.getClass()).reflect().method("m06").withAnyArgs();
            L2UpdateAssignCacheAdvice.getAnnotationInfo(m06.getAnnotation(L2UpdateAssignCache.class), m06.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("DataIndex for annotation"));
            assertTrue(ex.getMessage().contains(L2UpdateAssignCache.class.getName()));
        }
    }

    @Test
    public void testExpirationOrWindow() {
        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            UpdateAssignCacheAdvice.getAnnotationInfo(m07.getAnnotation(UpdateAssignCache.class), m07.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Expiration for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateAssignCache.class.getName()));
        }

        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            L2UpdateAssignCacheAdvice.getAnnotationInfo(m07.getAnnotation(L2UpdateAssignCache.class), m07.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Window for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2UpdateAssignCache.class.getName()));
        }
    }

    @Test
    public void testJitter() {
        try {
            final Method m08 = new Mirror().on(this.getClass()).reflect().method("m08").withAnyArgs();
            UpdateAssignCacheAdvice.getAnnotationInfo(m08.getAnnotation(UpdateAssignCache.class), m08.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Jitter for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateAssignCache.class.getName()));
        }

        try {
            final Method m09 = new Mirror().on(this.getClass()).reflect().method("m09").withAnyArgs();
            UpdateAssignCacheAdvice.getAnnotationInfo(m09.getAnnotation(UpdateAssignCache.class), m09.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Jitter for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateAssignCache.class.getName()));
        }

        // Jitter Default!
        final int jitterDefault = RandomUtils.nextInt(50) + 10;
        final Method m10 = new Mirror().on(this.getClass()).reflect().method("m10").withAnyArgs();
        final AnnotationInfo r1 = UpdateAssignCacheAdvice.getAnnotationInfo(m10.getAnnotation(UpdateAssignCache.class), m10.getName(), jitterDefault);
        assertEquals(jitterDefault, r1.getAsInteger(AType.JITTER).intValue());

    }
    
    @Test
    public void testSuccess() {
        final int jitterDefault = RandomUtils.nextInt(10) + 10;
        final Method m11 = new Mirror().on(this.getClass()).reflect().method("m11").withAnyArgs();

        final AnnotationInfo r1 = UpdateAssignCacheAdvice.getAnnotationInfo(m11.getAnnotation(UpdateAssignCache.class), m11.getName(), jitterDefault);
        assertEquals(NS, r1.getAsString(AType.NAMESPACE));
        assertEquals(KEY, r1.getAsString(AType.ASSIGN_KEY));
        assertEquals(INDEX, r1.getAsInteger(AType.DATA_INDEX).intValue());
        assertEquals(EXPIRATION, r1.getAsInteger(AType.EXPIRATION).intValue());
        assertEquals(JITTER, r1.getAsInteger(AType.JITTER).intValue());
        AnnotationInfoTest.ensureValuesNotSet(r1,
                Arrays.asList(AType.NAMESPACE, AType.ASSIGN_KEY, AType.DATA_INDEX, AType.EXPIRATION, AType.JITTER));

        final AnnotationInfo r2 = L2UpdateAssignCacheAdvice.getAnnotationInfo(m11.getAnnotation(L2UpdateAssignCache.class), m11.getName());
        assertEquals(NS, r2.getAsString(AType.NAMESPACE));
        assertEquals(KEY, r2.getAsString(AType.ASSIGN_KEY));
        assertEquals(INDEX, r2.getAsInteger(AType.DATA_INDEX).intValue());
        assertEquals(Duration.ONE_MINUTE, r2.getAsType(AType.WINDOW, Duration.class));
        AnnotationInfoTest.ensureValuesNotSet(r2,
                Arrays.asList(AType.NAMESPACE, AType.ASSIGN_KEY, AType.DATA_INDEX, AType.WINDOW));

    }
        
    /* * * *  Namespace Tests  * * * */
    @L2UpdateAssignCache(namespace = AnnotationConstants.DEFAULT_STRING, assignedKey = KEY, dataIndex = 0, window = Duration.ONE_MINUTE)
    @UpdateAssignCache(namespace = AnnotationConstants.DEFAULT_STRING, assignedKey = KEY, dataIndex = 0)
    public String m01() { return null; }

    @L2UpdateAssignCache(namespace = "", assignedKey = KEY, dataIndex = 0, window = Duration.ONE_MINUTE)
    @UpdateAssignCache(namespace = "", assignedKey = KEY, dataIndex = 0)
    public String m02() { return null; }

    /* * * *  AssignedKey Tests  * * * */
    @L2UpdateAssignCache(namespace = NS, assignedKey = AnnotationConstants.DEFAULT_STRING, dataIndex = 0, window = Duration.ONE_MINUTE)
    @UpdateAssignCache(namespace = NS, assignedKey = AnnotationConstants.DEFAULT_STRING, dataIndex = 0)
    public String m03() { return null; }

    @L2UpdateAssignCache(namespace = NS, assignedKey = "", dataIndex = 0, window = Duration.ONE_MINUTE)
    @UpdateAssignCache(namespace = NS, assignedKey = "", dataIndex = 0)
    public String m04() { return null; }

    /* * * *  DataIndex Tests  * * * */
    @L2UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = AnnotationConstants.DEFAULT_KEY_INDEX, window = Duration.ONE_MINUTE)
    @UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = AnnotationConstants.DEFAULT_KEY_INDEX)
    public String m05() { return null; }

    @L2UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = -2, window = Duration.ONE_MINUTE)
    @UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = -2)
    public String m06() { return null; }

    /* * * *  Expiration/Window Tests  * * * */
    @L2UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = 0, window = Duration.UNDEFINED)
    @UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = 0, expiration = -1)
    public String m07() { return null; }

    /* * * *  Jitter Tests  * * * */
    @UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = 0, jitter = -2)
    public String m08() { return null; }

    @UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = 0, jitter = 100)
    public String m09() { return null; }

    @UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = 0, jitter = -1)
    public String m10() { return null; }

    /* * * *  Success Tests  * * * */
    @L2UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = INDEX, window = Duration.ONE_MINUTE)
    @UpdateAssignCache(namespace = NS, assignedKey = KEY, dataIndex = INDEX, expiration = EXPIRATION, jitter = JITTER)
    public String m11() { return null; }

}
