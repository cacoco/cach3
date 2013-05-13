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
public class ReadThroughSingleCacheAnnotationTest {

    private static final String NS = "RTSC";
    private static final String STR = "GaryGnu";
    private static final int EXPIRATION = 23;
    private static final int INDEX = 2;
    private static final int JITTER = 4;

    @Test
    public void testNull() {
        try {
            ReadThroughSingleCacheAdvice.getAnnotationInfo(null, "bubba", 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }

        try {
            L2ReadThroughSingleCacheAdvice.getAnnotationInfo(null, "bubba");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }
    }

    @Test
    public void testNS() throws Exception {
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m01.getAnnotation(ReadThroughSingleCache.class), m01.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m02.getAnnotation(ReadThroughSingleCache.class), m02.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m01.getAnnotation(L2ReadThroughSingleCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2ReadThroughSingleCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m02.getAnnotation(L2ReadThroughSingleCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2ReadThroughSingleCache.class.getName()));
        }
    }


    @Test
    public void testExpirationOrWindow() {
        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m03.getAnnotation(ReadThroughSingleCache.class), m03.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Expiration for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }

        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m03.getAnnotation(L2ReadThroughSingleCache.class), m03.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Window for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2ReadThroughSingleCache.class.getName()));
        }
    }

    @Test
    public void testJitter() {
        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m04.getAnnotation(ReadThroughSingleCache.class), m04.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Jitter for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }

        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m05.getAnnotation(ReadThroughSingleCache.class), m05.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Jitter for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }

        // Jitter Default!
        final int jitterDefault = RandomUtils.nextInt(50) + 10;
        final Method m06 = new Mirror().on(this.getClass()).reflect().method("m06").withAnyArgs();
        final AnnotationInfo r1 = ReadThroughSingleCacheAdvice.getAnnotationInfo(m06.getAnnotation(ReadThroughSingleCache.class), m06.getName(), jitterDefault);
        assertEquals(jitterDefault, r1.getAsInteger(AType.JITTER).intValue());
    }

    @Test
    public void testKeyPrefix() {
        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m07.getAnnotation(ReadThroughSingleCache.class), m07.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyPrefix for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m07.getAnnotation(L2ReadThroughSingleCache.class), m07.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyPrefix for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2ReadThroughSingleCache.class.getName()));
        }
    }

    @Test
    public void testKeyIndex() {
        try {
            final Method m08 = new Mirror().on(this.getClass()).reflect().method("m08").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m08.getAnnotation(ReadThroughSingleCache.class), m08.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m08 = new Mirror().on(this.getClass()).reflect().method("m08").withAnyArgs();
            L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m08.getAnnotation(L2ReadThroughSingleCache.class), m08.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2ReadThroughSingleCache.class.getName()));
        }
    }

    @Test
    public void testKeyTemplate() {
        try {
            final Method m09 = new Mirror().on(this.getClass()).reflect().method("m09").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m09.getAnnotation(ReadThroughSingleCache.class), m09.getName(), 0);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyTemplate for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m09 = new Mirror().on(this.getClass()).reflect().method("m09").withAnyArgs();
            L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m09.getAnnotation(L2ReadThroughSingleCache.class), m09.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyTemplate for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2ReadThroughSingleCache.class.getName()));
        }
    }

    // Exactly one of [keyIndex,keyTemplate] must be defined
    @Test
    public void testExactlyOne() throws Exception {
        try {
            final Method m10 = new Mirror().on(this.getClass()).reflect().method("m10").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m10.getAnnotation(ReadThroughSingleCache.class), m10.getName(), JITTER);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Exactly one of [keyIndex,keyTemplate]"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }
        try {
            final Method m11 = new Mirror().on(this.getClass()).reflect().method("m11").withAnyArgs();
            ReadThroughSingleCacheAdvice.getAnnotationInfo(m11.getAnnotation(ReadThroughSingleCache.class), m11.getName(), JITTER);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Exactly one of [keyIndex,keyTemplate]"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(ReadThroughSingleCache.class.getName()));
        }

        try {
            final Method m10 = new Mirror().on(this.getClass()).reflect().method("m10").withAnyArgs();
            L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m10.getAnnotation(L2ReadThroughSingleCache.class), m10.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Exactly one of [keyIndex,keyTemplate]"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2ReadThroughSingleCache.class.getName()));
        }

        try {
            final Method m11 = new Mirror().on(this.getClass()).reflect().method("m11").withAnyArgs();
            L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m11.getAnnotation(L2ReadThroughSingleCache.class), m11.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Exactly one of [keyIndex,keyTemplate]"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(L2ReadThroughSingleCache.class.getName()));
        }
    }

    @Test
    public void testSuccess() {
        final int jitterDefault = RandomUtils.nextInt(10) + 40;

        // KeyTemplate version.
        final Method m12 = new Mirror().on(this.getClass()).reflect().method("m12").withAnyArgs();
        final AnnotationInfo r1 = ReadThroughSingleCacheAdvice.getAnnotationInfo(m12.getAnnotation(ReadThroughSingleCache.class), m12.getName(), jitterDefault);
        assertEquals(NS, r1.getAsString(AType.NAMESPACE));
        assertEquals(STR, r1.getAsString(AType.KEY_TEMPLATE));
        assertEquals(EXPIRATION, r1.getAsInteger(AType.EXPIRATION).intValue());
        assertEquals(JITTER, r1.getAsInteger(AType.JITTER).intValue());
        AnnotationInfoTest.ensureValuesNotSet(r1,
                Arrays.asList(AType.NAMESPACE, AType.KEY_TEMPLATE, AType.EXPIRATION, AType.JITTER));

        final AnnotationInfo r2 = L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m12.getAnnotation(L2ReadThroughSingleCache.class), m12.getName());
        assertEquals(NS, r2.getAsString(AType.NAMESPACE));
        assertEquals(STR, r2.getAsString(AType.KEY_TEMPLATE));
        assertEquals(Duration.ONE_MINUTE, r2.getAsType(AType.WINDOW, Duration.class));
        AnnotationInfoTest.ensureValuesNotSet(r2,
                Arrays.asList(AType.NAMESPACE, AType.KEY_TEMPLATE, AType.WINDOW));

        // KeyIndex version.
        final Method m13 = new Mirror().on(this.getClass()).reflect().method("m13").withAnyArgs();

        final AnnotationInfo r3 = ReadThroughSingleCacheAdvice.getAnnotationInfo(m13.getAnnotation(ReadThroughSingleCache.class), m13.getName(), jitterDefault);
        assertEquals(NS, r3.getAsString(AType.NAMESPACE));
        assertEquals(INDEX, r3.getAsInteger(AType.KEY_INDEX).intValue());
        assertEquals(EXPIRATION, r3.getAsInteger(AType.EXPIRATION).intValue());
        assertEquals(JITTER, r3.getAsInteger(AType.JITTER).intValue());
        AnnotationInfoTest.ensureValuesNotSet(r3,
                Arrays.asList(AType.NAMESPACE, AType.KEY_INDEX, AType.EXPIRATION, AType.JITTER));

        final AnnotationInfo r4 = L2ReadThroughSingleCacheAdvice.getAnnotationInfo(m13.getAnnotation(L2ReadThroughSingleCache.class), m13.getName());
        assertEquals(NS, r4.getAsString(AType.NAMESPACE));
        assertEquals(INDEX, r4.getAsInteger(AType.KEY_INDEX).intValue());
        assertEquals(Duration.ONE_MINUTE, r4.getAsType(AType.WINDOW, Duration.class));
        AnnotationInfoTest.ensureValuesNotSet(r4,
                Arrays.asList(AType.NAMESPACE, AType.KEY_INDEX, AType.WINDOW));
    }
    
    /* * * *  Namespace Tests  * * * */
    @L2ReadThroughSingleCache(namespace = AnnotationConstants.DEFAULT_STRING, window = Duration.ONE_MINUTE)
    @ReadThroughSingleCache(namespace = AnnotationConstants.DEFAULT_STRING)
    public String m01() { return null; }

    @L2ReadThroughSingleCache(namespace = "", window = Duration.ONE_MINUTE)
    @ReadThroughSingleCache(namespace = "")
    public String m02() { return null; }

    /* * * *  Expiration/Window Tests  * * * */
    @L2ReadThroughSingleCache(namespace = NS, keyIndex = 0, window = Duration.UNDEFINED)
    @ReadThroughSingleCache(namespace = NS, keyIndex = 0, expiration = -1)
    public String m03() { return null; }

    /* * * *  Jitter Tests  * * * */
    @ReadThroughSingleCache(namespace = NS, keyIndex = 0, jitter = -2)
    public String m04() { return null; }

    @ReadThroughSingleCache(namespace = NS, keyIndex = 0, jitter = 100)
    public String m05() { return null; }

    @ReadThroughSingleCache(namespace = NS, keyIndex = 0, jitter = -1)
    public String m06() { return null; }

    /* * * *  KeyPrefix Tests  * * * */
    @L2ReadThroughSingleCache(namespace = NS, keyPrefix = "", keyIndex = 0, window = Duration.ONE_MINUTE)
    @ReadThroughSingleCache(namespace = NS, keyPrefix = "", keyIndex = 0)
    public String m07() { return null; }

    /* * * *  KeyIndex Tests  * * * */
    @L2ReadThroughSingleCache(namespace = NS, keyIndex = -2, window = Duration.ONE_MINUTE)
    @ReadThroughSingleCache(namespace = NS, keyIndex = -2)
    public String m08() { return null; }

    /* * * *  KeyTemplate Tests  * * * */
    @L2ReadThroughSingleCache(namespace = NS, keyTemplate = "", window = Duration.ONE_MINUTE)
    @ReadThroughSingleCache(namespace = NS, keyTemplate = "")
    public String m09() { return null; }

    /* * * *  Exactly One Tests  * * * */
    @L2ReadThroughSingleCache(namespace = NS, keyTemplate = STR, keyIndex = 0, window = Duration.ONE_MINUTE)
    @ReadThroughSingleCache(namespace = NS, keyTemplate = STR, keyIndex = 0)
    public String m10() { return null; }

    @L2ReadThroughSingleCache(namespace = NS, window = Duration.ONE_MINUTE)
    @ReadThroughSingleCache(namespace = NS)
    public String m11() { return null; }

    /* * * *  Success Tests  * * * */
    @L2ReadThroughSingleCache(namespace = NS, keyTemplate = STR, window = Duration.ONE_MINUTE)
    @ReadThroughSingleCache(namespace = NS, keyTemplate = STR, expiration = EXPIRATION, jitter = JITTER)
    public String m12() { return null; }

    @L2ReadThroughSingleCache(namespace = NS, keyIndex = INDEX, window = Duration.ONE_MINUTE)
    @ReadThroughSingleCache(namespace = NS, keyIndex = INDEX, expiration = EXPIRATION, jitter = JITTER)
    public String m13() { return null; }

}
