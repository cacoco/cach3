package org.flite.cach3.aop;

import net.vidageek.mirror.dsl.Mirror;
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
public class InvalidateMultiCacheAnnotationTest {

    private static final String NS = "NS";
    private static final String TEMPLATE = "shimshamalakaBLAM";

    @Test
    public void testNull() {
        try {
            InvalidateMultiCacheAdvice.getAnnotationInfo(null, "bubba");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }

        try {
            L2InvalidateMultiCacheAdvice.getAnnotationInfo(null, "bubba");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }
    }

    @Test
    public void testNS() throws Exception {
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            InvalidateMultiCacheAdvice.getAnnotationInfo(m01.getAnnotation(InvalidateMultiCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(InvalidateMultiCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            InvalidateMultiCacheAdvice.getAnnotationInfo(m02.getAnnotation(InvalidateMultiCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(InvalidateMultiCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            L2InvalidateMultiCacheAdvice.getAnnotationInfo(m01.getAnnotation(L2InvalidateMultiCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateMultiCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            L2InvalidateMultiCacheAdvice.getAnnotationInfo(m02.getAnnotation(L2InvalidateMultiCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateMultiCache.class.getName()));
        }
    }

    @Test
    public void testIndex() {
        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            InvalidateMultiCacheAdvice.getAnnotationInfo(m03.getAnnotation(InvalidateMultiCache.class), m03.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(InvalidateMultiCache.class.getName()));
        }

        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            L2InvalidateMultiCacheAdvice.getAnnotationInfo(m03.getAnnotation(L2InvalidateMultiCache.class), m03.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateMultiCache.class.getName()));
        }

        // Successes
        final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();

        final AnnotationInfo r1 = InvalidateMultiCacheAdvice.getAnnotationInfo(m04.getAnnotation(InvalidateMultiCache.class), m04.getName());
        assertEquals(9, r1.getAsInteger(AType.KEY_INDEX).intValue());
        AnnotationInfoTest.ensureValuesNotSet(r1, Arrays.asList(AType.NAMESPACE, AType.KEY_INDEX));

        final AnnotationInfo r2 = L2InvalidateMultiCacheAdvice.getAnnotationInfo(m04.getAnnotation(L2InvalidateMultiCache.class), m04.getName());
        assertEquals(9, r2.getAsInteger(AType.KEY_INDEX).intValue());
        AnnotationInfoTest.ensureValuesNotSet(r2, Arrays.asList(AType.NAMESPACE, AType.KEY_INDEX));
    }

    @Test
    public void testTemplate() {
        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            InvalidateMultiCacheAdvice.getAnnotationInfo(m05.getAnnotation(InvalidateMultiCache.class), m05.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyTemplate for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(InvalidateMultiCache.class.getName()));
        }

        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            L2InvalidateMultiCacheAdvice.getAnnotationInfo(m05.getAnnotation(L2InvalidateMultiCache.class), m05.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("KeyTemplate for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateMultiCache.class.getName()));
        }

        // Successes
        final Method m06 = new Mirror().on(this.getClass()).reflect().method("m06").withAnyArgs();

        final AnnotationInfo r1 = InvalidateMultiCacheAdvice.getAnnotationInfo(m06.getAnnotation(InvalidateMultiCache.class), m06.getName());
        assertEquals(TEMPLATE, r1.getAsString(AType.KEY_TEMPLATE));
        AnnotationInfoTest.ensureValuesNotSet(r1, Arrays.asList(AType.NAMESPACE, AType.KEY_INDEX, AType.KEY_TEMPLATE));

        final AnnotationInfo r2 = L2InvalidateMultiCacheAdvice.getAnnotationInfo(m06.getAnnotation(L2InvalidateMultiCache.class), m06.getName());
        assertEquals(TEMPLATE, r2.getAsString(AType.KEY_TEMPLATE));
        AnnotationInfoTest.ensureValuesNotSet(r2, Arrays.asList(AType.NAMESPACE, AType.KEY_INDEX, AType.KEY_TEMPLATE));
    }

    @Test
    public void testPrefix() {
        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            InvalidateMultiCacheAdvice.getAnnotationInfo(m07.getAnnotation(InvalidateMultiCache.class), m07.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyPrefix for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(InvalidateMultiCache.class.getName()));
        }

        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            L2InvalidateMultiCacheAdvice.getAnnotationInfo(m07.getAnnotation(L2InvalidateMultiCache.class), m07.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("KeyPrefix for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateMultiCache.class.getName()));
        }

        // Successes
        final Method m08 = new Mirror().on(this.getClass()).reflect().method("m08").withAnyArgs();

        final AnnotationInfo r1 = InvalidateMultiCacheAdvice.getAnnotationInfo(m08.getAnnotation(InvalidateMultiCache.class), m08.getName());
        assertEquals(TEMPLATE, r1.getAsString(AType.KEY_PREFIX));
        AnnotationInfoTest.ensureValuesNotSet(r1, Arrays.asList(AType.NAMESPACE, AType.KEY_PREFIX, AType.KEY_INDEX));

        final AnnotationInfo r2 = L2InvalidateMultiCacheAdvice.getAnnotationInfo(m08.getAnnotation(L2InvalidateMultiCache.class), m08.getName());
        assertEquals(TEMPLATE, r2.getAsString(AType.KEY_PREFIX));
        AnnotationInfoTest.ensureValuesNotSet(r2, Arrays.asList(AType.NAMESPACE, AType.KEY_PREFIX, AType.KEY_INDEX));
    }


    /* * * *  Namespace Tests  * * * */
    @L2InvalidateMultiCache(namespace = AnnotationConstants.DEFAULT_STRING, keyIndex = 1)
    @InvalidateMultiCache(namespace = AnnotationConstants.DEFAULT_STRING, keyIndex = 1)
    public String m01() { return null; }

    @L2InvalidateMultiCache(namespace = "", keyIndex = 1)
    @InvalidateMultiCache(namespace = "", keyIndex = 1)
    public String m02() { return null; }

    /* * * *  KeyIndex Tests  * * * */
    @L2InvalidateMultiCache(namespace = NS, keyIndex = -2)
    @InvalidateMultiCache(namespace = NS, keyIndex = -2)
    public String m03() { return null; }

    @L2InvalidateMultiCache(namespace = NS, keyIndex = 9)
    @InvalidateMultiCache(namespace = NS, keyIndex = 9)
    public String m04() { return null; }

    /* * * *  KeyTemplate Tests  * * * */
    @L2InvalidateMultiCache(namespace = NS, keyIndex = 0, keyTemplate = "")
    @InvalidateMultiCache(namespace = NS, keyIndex = 0, keyTemplate = "")
    public String m05() { return null; }

    @L2InvalidateMultiCache(namespace = NS, keyIndex = 0, keyTemplate = TEMPLATE)
    @InvalidateMultiCache(namespace = NS, keyIndex = 0, keyTemplate = TEMPLATE)
    public String m06() { return null; }

    /* * * *  KeyPrefix Tests  * * * */
    @L2InvalidateMultiCache(namespace = NS, keyIndex = 9, keyPrefix = "")
    @InvalidateMultiCache(namespace = NS, keyIndex = 9, keyPrefix = "")
    public String m07() { return null; }

    @L2InvalidateMultiCache(namespace = NS, keyIndex = 9, keyPrefix = TEMPLATE)
    @InvalidateMultiCache(namespace = NS, keyIndex = 9, keyPrefix = TEMPLATE)
    public String m08() { return null; }

}
