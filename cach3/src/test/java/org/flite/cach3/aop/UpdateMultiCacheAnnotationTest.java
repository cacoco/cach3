package org.flite.cach3.aop;

import net.vidageek.mirror.dsl.Mirror;
import org.apache.commons.lang.math.RandomUtils;
import org.flite.cach3.annotations.*;
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
public class UpdateMultiCacheAnnotationTest {

    private static final String NS = "UMC";
    private static final String STR = "BarberaHeros";
    private static final int JITTER = 11;
    private static final int INDEX = 13;
    private static final int EXPIRATION = 97;

    @Test
    public void testNull() {
        try {
            UpdateMultiCacheAdvice.getAnnotationInfo(null, "bubba", 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }

        try {
            L2UpdateMultiCacheAdvice.getAnnotationInfo(null, "bubba");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }
    }

    @Test
    public void testNS() throws Exception {
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m01.getAnnotation(UpdateMultiCache.class), m01.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m02.getAnnotation(UpdateMultiCache.class), m02.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            L2UpdateMultiCacheAdvice.getAnnotationInfo(m01.getAnnotation(L2UpdateMultiCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2UpdateMultiCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            L2UpdateMultiCacheAdvice.getAnnotationInfo(m02.getAnnotation(L2UpdateMultiCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2UpdateMultiCache.class.getName()));
        }
    }

    @Test
    public void testExpirationOrWindow() {
        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m03.getAnnotation(UpdateMultiCache.class), m03.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Expiration for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            L2UpdateMultiCacheAdvice.getAnnotationInfo(m03.getAnnotation(L2UpdateMultiCache.class), m03.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Window for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2UpdateMultiCache.class.getName()));
        }
    }

    @Test
    public void testJitter() {
        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m04.getAnnotation(UpdateMultiCache.class), m04.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Jitter for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m05.getAnnotation(UpdateMultiCache.class), m05.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Jitter for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        // Jitter Default!
        final int jitterDefault = RandomUtils.nextInt(50) + 10;
        final Method m06 = new Mirror().on(this.getClass()).reflect().method("m06").withAnyArgs();
        final AnnotationInfo r1 = UpdateMultiCacheAdvice.getAnnotationInfo(m06.getAnnotation(UpdateMultiCache.class), m06.getName(), jitterDefault);
        assertEquals(jitterDefault, r1.getAsInteger(AType.JITTER).intValue());
    }

    @Test
    public void testDataIndex() {
        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m07.getAnnotation(UpdateMultiCache.class), m07.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("DataIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        try {
            final Method m08 = new Mirror().on(this.getClass()).reflect().method("m08").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m08.getAnnotation(UpdateMultiCache.class), m08.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("DataIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            L2UpdateMultiCacheAdvice.getAnnotationInfo(m07.getAnnotation(L2UpdateMultiCache.class), m07.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("DataIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2UpdateMultiCache.class.getName()));
        }

        try {
            final Method m08 = new Mirror().on(this.getClass()).reflect().method("m08").withAnyArgs();
            L2UpdateMultiCacheAdvice.getAnnotationInfo(m08.getAnnotation(L2UpdateMultiCache.class), m08.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("DataIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2UpdateMultiCache.class.getName()));
        }
    }

    @Test
    public void testKeyPrefix() {
        try {
            final Method m09 = new Mirror().on(this.getClass()).reflect().method("m09").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m09.getAnnotation(UpdateMultiCache.class), m09.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyPrefix for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m09 = new Mirror().on(this.getClass()).reflect().method("m09").withAnyArgs();
            L2UpdateMultiCacheAdvice.getAnnotationInfo(m09.getAnnotation(L2UpdateMultiCache.class), m09.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyPrefix for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2UpdateMultiCache.class.getName()));
        }
    }

    @Test
    public void testKeyIndex() {
        try {
            final Method m10 = new Mirror().on(this.getClass()).reflect().method("m10").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m10.getAnnotation(UpdateMultiCache.class), m10.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m10 = new Mirror().on(this.getClass()).reflect().method("m10").withAnyArgs();
            L2UpdateMultiCacheAdvice.getAnnotationInfo(m10.getAnnotation(L2UpdateMultiCache.class), m10.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2UpdateMultiCache.class.getName()));
        }
    }

    @Test
    public void testKeyTemplate() {
        try {
            final Method m11 = new Mirror().on(this.getClass()).reflect().method("m11").withAnyArgs();
            UpdateMultiCacheAdvice.getAnnotationInfo(m11.getAnnotation(UpdateMultiCache.class), m11.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyTemplate for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(UpdateMultiCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m11 = new Mirror().on(this.getClass()).reflect().method("m11").withAnyArgs();
            L2UpdateMultiCacheAdvice.getAnnotationInfo(m11.getAnnotation(L2UpdateMultiCache.class), m11.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyTemplate for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2UpdateMultiCache.class.getName()));
        }
    }

    @Test
    public void testSuccess() {
        final int jitterDefault = RandomUtils.nextInt(10) + 10;
        final Method m12 = new Mirror().on(this.getClass()).reflect().method("m12").withAnyArgs();

        final AnnotationInfo r1 = UpdateMultiCacheAdvice.getAnnotationInfo(m12.getAnnotation(UpdateMultiCache.class), m12.getName(), jitterDefault);
        assertEquals(NS, r1.getAsString(AType.NAMESPACE));
        assertEquals(STR, r1.getAsString(AType.KEY_TEMPLATE));
        assertEquals(INDEX, r1.getAsInteger(AType.KEY_INDEX).intValue());
        assertEquals(INDEX, r1.getAsInteger(AType.DATA_INDEX).intValue());
        assertEquals(EXPIRATION, r1.getAsInteger(AType.EXPIRATION).intValue());
        assertEquals(JITTER, r1.getAsInteger(AType.JITTER).intValue());
        AnnotationInfoTest.ensureValuesNotSet(r1,
                Arrays.asList(AType.NAMESPACE, AType.KEY_TEMPLATE, AType.KEY_INDEX, AType.DATA_INDEX, AType.EXPIRATION, AType.JITTER));

        final AnnotationInfo r2 = L2UpdateMultiCacheAdvice.getAnnotationInfo(m12.getAnnotation(L2UpdateMultiCache.class), m12.getName());
        assertEquals(NS, r2.getAsString(AType.NAMESPACE));
        assertEquals(STR, r2.getAsString(AType.KEY_TEMPLATE));
        assertEquals(INDEX, r1.getAsInteger(AType.KEY_INDEX).intValue());
        assertEquals(INDEX, r2.getAsInteger(AType.DATA_INDEX).intValue());
        assertEquals(Duration.ONE_MINUTE, r2.getAsType(AType.WINDOW, Duration.class));
        AnnotationInfoTest.ensureValuesNotSet(r2,
                Arrays.asList(AType.NAMESPACE, AType.KEY_TEMPLATE, AType.KEY_INDEX, AType.DATA_INDEX, AType.WINDOW));

    }

    /* * * *  Namespace Tests  * * * */
    @L2UpdateMultiCache(namespace = AnnotationConstants.DEFAULT_STRING, dataIndex = 0, window = Duration.ONE_MINUTE)
    @UpdateMultiCache(namespace = AnnotationConstants.DEFAULT_STRING, dataIndex = 0)
    public String m01() { return null; }

    @L2UpdateMultiCache(namespace = "", dataIndex = 0, window = Duration.ONE_MINUTE)
    @UpdateMultiCache(namespace = "", dataIndex = 0)
    public String m02() { return null; }

    /* * * *  Expiration/Window Tests  * * * */
    @L2UpdateMultiCache(namespace = NS, dataIndex = 0, keyIndex = 0, window = Duration.UNDEFINED)
    @UpdateMultiCache(namespace = NS, dataIndex = 0, keyIndex = 0, expiration = -1)
    public String m03() { return null; }

    /* * * *  Jitter Tests  * * * */
    @UpdateMultiCache(namespace = NS, dataIndex = 0, keyIndex = 0, jitter = -2)
    public String m04() { return null; }

    @UpdateMultiCache(namespace = NS, dataIndex = 0, keyIndex = 0, jitter = 100)
    public String m05() { return null; }

    @UpdateMultiCache(namespace = NS, dataIndex = 0, keyIndex = 0, jitter = -1)
    public String m06() { return null; }

    /* * * *  DataIndex Tests  * * * */
    @L2UpdateMultiCache(namespace = NS, keyIndex = 0, dataIndex = AnnotationConstants.DEFAULT_KEY_INDEX, window = Duration.ONE_MINUTE)
    @UpdateMultiCache(namespace = NS, keyIndex = 0, dataIndex = AnnotationConstants.DEFAULT_KEY_INDEX)
    public String m07() { return null; }

    @L2UpdateMultiCache(namespace = NS, keyIndex = 0, dataIndex = -2, window = Duration.ONE_MINUTE)
    @UpdateMultiCache(namespace = NS, keyIndex = 0, dataIndex = -2)
    public String m08() { return null; }

    /* * * *  KeyPrefix Tests  * * * */
    @L2UpdateMultiCache(namespace = NS, keyPrefix = "", keyIndex = INDEX, dataIndex = INDEX, window = Duration.ONE_MINUTE)
    @UpdateMultiCache(namespace = NS, keyPrefix = "", keyIndex = INDEX, dataIndex = INDEX)
    public String m09() { return null; }

    /* * * *  KeyIndex Tests  * * * */
    @L2UpdateMultiCache(namespace = NS, keyIndex = -2, dataIndex = 0, window = Duration.ONE_MINUTE)
    @UpdateMultiCache(namespace = NS, keyIndex = -2, dataIndex = 0)
    public String m10() { return null; }

    /* * * *  KeyTemplate Tests  * * * */
    @L2UpdateMultiCache(namespace = NS, keyTemplate = "", keyIndex = INDEX, dataIndex = 0, window = Duration.ONE_MINUTE)
    @UpdateMultiCache(namespace = NS, keyTemplate = "", keyIndex = INDEX, dataIndex = 0)
    public String m11() { return null; }

    /* * * *  Success Tests  * * * */
    @L2UpdateMultiCache(namespace = NS, keyTemplate = STR, keyIndex = INDEX, dataIndex = INDEX, window = Duration.ONE_MINUTE)
    @UpdateMultiCache(namespace = NS, keyTemplate = STR, keyIndex = INDEX, dataIndex = INDEX, expiration = EXPIRATION, jitter = JITTER)
    public String m12() { return null; }


}
