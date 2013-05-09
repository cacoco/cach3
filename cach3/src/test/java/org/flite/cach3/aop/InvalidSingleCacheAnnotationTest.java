/*
 * Copyright (c) 2011-2013 Flite, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.flite.cach3.aop;

import net.vidageek.mirror.dsl.Mirror;
import org.flite.cach3.annotations.AnnotationConstants;
import org.flite.cach3.annotations.InvalidateSingleCache;
import org.flite.cach3.annotations.L2InvalidateSingleCache;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Arrays;

import static org.testng.AssertJUnit.*;

public class InvalidSingleCacheAnnotationTest {

    private static final String NS = "NS";
    private static final String TEMPLATE = "GObbleDYggooK";

    @Test
    public void testNull() {
        try {
            InvalidateSingleCacheAdvice.getAnnotationInfo(null, "bubba");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("No annotation of type"));
        }
    }

    @Test
    public void testNS() throws Exception {
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            InvalidateSingleCacheAdvice.getAnnotationInfo(m01.getAnnotation(InvalidateSingleCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(InvalidateSingleCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            InvalidateSingleCacheAdvice.getAnnotationInfo(m02.getAnnotation(InvalidateSingleCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(InvalidateSingleCache.class.getName()));
        }

        // Testing the L2Cache version of things.
        try {
            final Method m01 = new Mirror().on(this.getClass()).reflect().method("m01").withAnyArgs();
            L2InvalidateSingleCacheAdvice.getAnnotationInfo(m01.getAnnotation(L2InvalidateSingleCache.class), m01.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateSingleCache.class.getName()));
        }

        try {
            final Method m02 = new Mirror().on(this.getClass()).reflect().method("m02").withAnyArgs();
            L2InvalidateSingleCacheAdvice.getAnnotationInfo(m02.getAnnotation(L2InvalidateSingleCache.class), m02.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Namespace for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateSingleCache.class.getName()));
        }
    }

    // Exactly one of [keyIndex,keyTemplate] must be defined
    @Test
    public void testExactlyOne() throws Exception {
        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            InvalidateSingleCacheAdvice.getAnnotationInfo(m03.getAnnotation(InvalidateSingleCache.class), m03.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Exactly one of [keyIndex,keyTemplate]"));
            assertTrue(ex.getMessage().contains(InvalidateSingleCache.class.getName()));
        }
        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            InvalidateSingleCacheAdvice.getAnnotationInfo(m04.getAnnotation(InvalidateSingleCache.class), m04.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Exactly one of [keyIndex,keyTemplate]"));
            assertTrue(ex.getMessage().contains(InvalidateSingleCache.class.getName()));
        }

        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            L2InvalidateSingleCacheAdvice.getAnnotationInfo(m03.getAnnotation(L2InvalidateSingleCache.class), m03.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Exactly one of [keyIndex,keyTemplate]"));
            assertTrue(ex.getMessage().contains(L2InvalidateSingleCache.class.getName()));
        }

        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            L2InvalidateSingleCacheAdvice.getAnnotationInfo(m04.getAnnotation(L2InvalidateSingleCache.class), m04.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("Exactly one of [keyIndex,keyTemplate]"));
            assertTrue(ex.getMessage().contains(L2InvalidateSingleCache.class.getName()));
        }
    }

    @Test
    public void testIndex() {
        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            InvalidateSingleCacheAdvice.getAnnotationInfo(m05.getAnnotation(InvalidateSingleCache.class), m05.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(InvalidateSingleCache.class.getName()));
        }

        try {
            final Method m05 = new Mirror().on(this.getClass()).reflect().method("m05").withAnyArgs();
            L2InvalidateSingleCacheAdvice.getAnnotationInfo(m05.getAnnotation(L2InvalidateSingleCache.class), m05.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateSingleCache.class.getName()));
        }

        // Successes
        final Method m06 = new Mirror().on(this.getClass()).reflect().method("m06").withAnyArgs();

        final AnnotationInfo r1 = InvalidateSingleCacheAdvice.getAnnotationInfo(m06.getAnnotation(InvalidateSingleCache.class), m06.getName());
        assertEquals(9, r1.getAsInteger(AType.KEY_INDEX).intValue());
        AnnotationInfoTest.ensureValuesNotSet(r1, Arrays.asList(AType.NAMESPACE, AType.KEY_INDEX));

        final AnnotationInfo r2 = L2InvalidateSingleCacheAdvice.getAnnotationInfo(m06.getAnnotation(L2InvalidateSingleCache.class), m06.getName());
        assertEquals(9, r2.getAsInteger(AType.KEY_INDEX).intValue());
        AnnotationInfoTest.ensureValuesNotSet(r2, Arrays.asList(AType.NAMESPACE, AType.KEY_INDEX));
    }

    @Test
    public void testTemplate() {
        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            InvalidateSingleCacheAdvice.getAnnotationInfo(m07.getAnnotation(InvalidateSingleCache.class), m07.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyTemplate for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(InvalidateSingleCache.class.getName()));
        }

        try {
            final Method m07 = new Mirror().on(this.getClass()).reflect().method("m07").withAnyArgs();
            L2InvalidateSingleCacheAdvice.getAnnotationInfo(m07.getAnnotation(L2InvalidateSingleCache.class), m07.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("KeyTemplate for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateSingleCache.class.getName()));
        }

        // Successes
        final Method m08 = new Mirror().on(this.getClass()).reflect().method("m08").withAnyArgs();

        final AnnotationInfo r1 = InvalidateSingleCacheAdvice.getAnnotationInfo(m08.getAnnotation(InvalidateSingleCache.class), m08.getName());
        assertEquals(TEMPLATE, r1.getAsString(AType.KEY_TEMPLATE));
        AnnotationInfoTest.ensureValuesNotSet(r1, Arrays.asList(AType.NAMESPACE, AType.KEY_TEMPLATE));

        final AnnotationInfo r2 = L2InvalidateSingleCacheAdvice.getAnnotationInfo(m08.getAnnotation(L2InvalidateSingleCache.class), m08.getName());
        assertEquals(TEMPLATE, r2.getAsString(AType.KEY_TEMPLATE));
        AnnotationInfoTest.ensureValuesNotSet(r2, Arrays.asList(AType.NAMESPACE, AType.KEY_TEMPLATE));
    }

    @Test
    public void testPrefix() {
        try {
            final Method m09 = new Mirror().on(this.getClass()).reflect().method("m09").withAnyArgs();
            InvalidateSingleCacheAdvice.getAnnotationInfo(m09.getAnnotation(InvalidateSingleCache.class), m09.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyPrefix for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(InvalidateSingleCache.class.getName()));
        }

        try {
            final Method m09 = new Mirror().on(this.getClass()).reflect().method("m09").withAnyArgs();
            L2InvalidateSingleCacheAdvice.getAnnotationInfo(m09.getAnnotation(L2InvalidateSingleCache.class), m09.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("KeyPrefix for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateSingleCache.class.getName()));
        }

        // Successes
        final Method m10 = new Mirror().on(this.getClass()).reflect().method("m10").withAnyArgs();

        final AnnotationInfo r1 = InvalidateSingleCacheAdvice.getAnnotationInfo(m10.getAnnotation(InvalidateSingleCache.class), m10.getName());
        assertEquals(TEMPLATE, r1.getAsString(AType.KEY_PREFIX));
        AnnotationInfoTest.ensureValuesNotSet(r1, Arrays.asList(AType.NAMESPACE, AType.KEY_PREFIX, AType.KEY_INDEX));

        final AnnotationInfo r2 = L2InvalidateSingleCacheAdvice.getAnnotationInfo(m10.getAnnotation(L2InvalidateSingleCache.class), m10.getName());
        assertEquals(TEMPLATE, r2.getAsString(AType.KEY_PREFIX));
        AnnotationInfoTest.ensureValuesNotSet(r2, Arrays.asList(AType.NAMESPACE, AType.KEY_PREFIX, AType.KEY_INDEX));
    }

    /* * * *  Namespace Tests  * * * */
    @L2InvalidateSingleCache(namespace = AnnotationConstants.DEFAULT_STRING, keyIndex = 1)
    @InvalidateSingleCache(namespace = AnnotationConstants.DEFAULT_STRING, keyIndex = 1)
    public String m01() { return null; }

    @L2InvalidateSingleCache(namespace = "", keyIndex = 1)
    @InvalidateSingleCache(namespace = "", keyIndex = 1)
    public String m02() { return null; }

    /* * * *  Exactly-One Tests  * * * */
    @L2InvalidateSingleCache(namespace = NS, keyIndex = -1, keyTemplate = "bubba")
    @InvalidateSingleCache(namespace = NS, keyIndex = -1, keyTemplate = "bubba")
    public String m03() { return null; }

    @L2InvalidateSingleCache(namespace = NS)
    @InvalidateSingleCache(namespace = NS)
    public String m04() { return null; }

    /* * * *  KeyIndex Tests  * * * */
    @L2InvalidateSingleCache(namespace = NS, keyIndex = -2)
    @InvalidateSingleCache(namespace = NS, keyIndex = -2)
    public String m05() { return null; }

    @L2InvalidateSingleCache(namespace = NS, keyIndex = 9)
    @InvalidateSingleCache(namespace = NS, keyIndex = 9)
    public String m06() { return null; }

    /* * * *  KeyTemplate Tests  * * * */
    @L2InvalidateSingleCache(namespace = NS, keyTemplate = "")
    @InvalidateSingleCache(namespace = NS, keyTemplate = "")
    public String m07() { return null; }

    @L2InvalidateSingleCache(namespace = NS, keyTemplate = TEMPLATE)
    @InvalidateSingleCache(namespace = NS, keyTemplate = TEMPLATE)
    public String m08() { return null; }

    /* * * *  KeyPrefix Tests  * * * */
    @L2InvalidateSingleCache(namespace = NS, keyIndex = 9, keyPrefix = "")
    @InvalidateSingleCache(namespace = NS, keyIndex = 9, keyPrefix = "")
    public String m09() { return null; }

    @L2InvalidateSingleCache(namespace = NS, keyIndex = 9, keyPrefix = TEMPLATE)
    @InvalidateSingleCache(namespace = NS, keyIndex = 9, keyPrefix = TEMPLATE)
    public String m10() { return null; }

}
