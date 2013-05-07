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
import org.flite.cach3.annotations.*;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

public class InvalidSingleCacheAnnotationTest {

    private static final String NS = "NS";

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
    public void testCollide() throws Exception {
        try {
            final Method m03 = new Mirror().on(this.getClass()).reflect().method("m03").withAnyArgs();
            InvalidateSingleCacheAdvice.getAnnotationInfo(m03.getAnnotation(InvalidateSingleCache.class), m03.getName());
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
    }

//    TODO: FIX!
//    @Test
    public void testIndex() {
        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            InvalidateSingleCacheAdvice.getAnnotationInfo(m04.getAnnotation(InvalidateSingleCache.class), m04.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage(), ex.getMessage().contains(InvalidateSingleCache.class.getName()));
        }

        try {
            final Method m04 = new Mirror().on(this.getClass()).reflect().method("m04").withAnyArgs();
            L2InvalidateSingleCacheAdvice.getAnnotationInfo(m04.getAnnotation(L2InvalidateSingleCache.class), m04.getName());
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) {
            assertTrue(ex.getMessage().contains("KeyIndex for annotation"));
            assertTrue(ex.getMessage().contains(L2InvalidateSingleCache.class.getName()));
        }        
    }
    
    @L2InvalidateSingleCache(namespace = AnnotationConstants.DEFAULT_STRING, keyIndex = 1)
    @InvalidateSingleCache(namespace = AnnotationConstants.DEFAULT_STRING, keyIndex = 1)
    public String m01() { return null; }

    @L2InvalidateSingleCache(namespace = "", keyIndex = 1)
    @InvalidateSingleCache(namespace = "", keyIndex = 1)
    public String m02() { return null; }

    @L2InvalidateSingleCache(namespace = NS, keyIndex = -1, keyTemplate = "bubba")
    @InvalidateSingleCache(namespace = NS, keyIndex = -1, keyTemplate = "bubba")
    public String m03() { return null; }

    @L2InvalidateSingleCache(namespace = NS, keyIndex = -2)
    @InvalidateSingleCache(namespace = NS, keyIndex = -2)
    public String m04() { return null; }

}
