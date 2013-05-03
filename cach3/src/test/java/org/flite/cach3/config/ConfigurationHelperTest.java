package org.flite.cach3.config;

import net.spy.memcached.*;
import org.apache.commons.lang.math.*;
import org.flite.cach3.api.*;
import org.testng.annotations.*;

import java.security.*;

import static org.testng.AssertJUnit.*;

/**
Copyright (c) 2011-2012 Flite, Inc

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
public class ConfigurationHelperTest {

    @Test
    public void testSetDisabled() throws Exception {
        try {
            ConfigurationHelper.setCacheDisabled(null, true);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }

        final Cach3State state = new Cach3State();
        state.setProvider(new MemcachedClientProvider() {
            public MemcachedClientIF getMemcachedClient() {
                return null;
            }

            public void refreshConnection() { }
        });

        for (int ix = 0; ix < 3; ix++) {
            final boolean result = RandomUtils.nextBoolean();
            ConfigurationHelper.setCacheDisabled(state, result);
            assertEquals(result, state.isCacheDisabled());
        }
    }

    @Test
    public void testSetJitterDefault() throws Exception {
        try {
            ConfigurationHelper.setJitterDefault(null, 10);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }

        final Cach3State state = new Cach3State();
        state.setProvider(new MemcachedClientProvider() {
            public MemcachedClientIF getMemcachedClient() {
                return null;
            }

            public void refreshConnection() { }
        });

        for (int ix = 0; ix < 3; ix++) {
            final int result = RandomUtils.nextInt(99);
            ConfigurationHelper.setJitterDefault(state, result);
            assertEquals(result, state.getJitterDefault());
        }
    }

    @Test
    public void testAddVelocityContextItems() throws Exception {
        try {
            ConfigurationHelper.addVelocityContextItems(null, "bubba", 10);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }
    }

    @Test
    public void testL2CacheEnabled() {
        try {
            ConfigurationHelper.setL2CacheDisabled(null, false);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }

        final L2Cach3State state = new L2Cach3State();
        assertTrue(ConfigurationHelper.setL2CacheDisabled(state, true));
        assertFalse(ConfigurationHelper.setL2CacheDisabled(state, false));
    }
}
