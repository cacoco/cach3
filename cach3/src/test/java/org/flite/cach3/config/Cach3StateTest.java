package org.flite.cach3.config;

import org.testng.annotations.*;

import java.security.*;

import static org.testng.AssertJUnit.*;

/**
 * Copyright (c) 2012 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

public class Cach3StateTest {

    @Test
    public void testJitterDefault() {
        final Cach3State state = new Cach3State();

        assertEquals(0, state.getJitterDefault());

        state.setJitterDefault(99);
        assertEquals(99, state.getJitterDefault());

        state.setJitterDefault(10);
        try {
            state.setJitterDefault(-1);
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertEquals(10, state.getJitterDefault());
        }

        try {
            state.setJitterDefault(100);
            fail("Expected Exception");
        } catch (InvalidParameterException ex) {
            assertEquals(10, state.getJitterDefault());
        }
    }
}
