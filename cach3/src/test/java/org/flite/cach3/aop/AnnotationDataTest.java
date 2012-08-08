package org.flite.cach3.aop;

import org.apache.commons.lang.math.*;
import org.testng.annotations.*;

import static org.testng.AssertJUnit.*;

/**
 * Copyright (c) 2012 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

public class AnnotationDataTest {

    @Test
    public void testJitterCalculation() {
        final AnnotationData data = new AnnotationData();

        final int base_exp = 100 + RandomUtils.nextInt(100);
        data.setExpiration(base_exp);

        // Jitter percent is not between 1 and 99
        data.setJitter(-2);
        assertEquals(base_exp, data.getJitteredExpiration());
        data.setJitter(-1);
        assertEquals(base_exp, data.getJitteredExpiration());
        data.setJitter(0);
        assertEquals(base_exp, data.getJitteredExpiration());
        data.setJitter(100);
        assertEquals(base_exp, data.getJitteredExpiration());
        data.setJitter(101);
        assertEquals(base_exp, data.getJitteredExpiration());

        // Expiration is over the boundary, so it is representing an actual date/time
        data.setJitter(20);
        data.setExpiration(AnnotationData.JITTER_BOUND);
        assertEquals(AnnotationData.JITTER_BOUND, data.getJitteredExpiration());
        data.setExpiration(AnnotationData.JITTER_BOUND + base_exp);
        assertEquals(AnnotationData.JITTER_BOUND + base_exp, data.getJitteredExpiration());

        // Now, we are working with actual jitter.
        int exp = 10000;
        int lower = 8000;
        int previous = 0;
        data.setExpiration(exp);
        data.setJitter(20);
        for (int ix = 0; ix < 25; ix++) {
            final int attempt = data.getJitteredExpiration();
            // System.out.println(attempt);
            assertTrue(previous != attempt);
            assertTrue(attempt <= exp);
            assertTrue(attempt > lower);

            previous = attempt;
        }
    }
}
