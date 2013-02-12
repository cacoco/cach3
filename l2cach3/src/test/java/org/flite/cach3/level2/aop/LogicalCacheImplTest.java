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

package org.flite.cach3.level2.aop;

import org.apache.commons.lang.*;
import org.apache.commons.lang.math.*;
import org.easymock.*;
import org.flite.cach3.level2.annotations.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.AssertJUnit.*;
import static org.testng.AssertJUnit.assertEquals;

public class LogicalCacheImplTest extends EasyMockSupport {

    private LogicalCacheImpl impl;

    @BeforeClass
    public void beforeClass() throws Exception {
        impl = new LogicalCacheImpl();
        impl.afterPropertiesSet();
    }

    @BeforeMethod
    private void beforeMethod() {
        resetAll();
    }

    @Test
    public void testWarnings() {
        final String prefix = "testWarnings-";

        final Set<String> putIds = new HashSet<String>();
        final Map<String, Object> data = new HashMap<String, Object>();
        final Set<String> missIds = new HashSet<String>();

        final List<Duration> allDurations = new ArrayList<Duration>(LogicalCacheImpl.DURATION_SET);
        Collections.shuffle(allDurations);
        final Duration target = allDurations.get(0);

        for (int ix = 0; ix < 10; ix++) {
            final String key = prefix + RandomStringUtils.randomAlphanumeric(10 + ix);
            putIds.add(key);
            data.put(key, RandomStringUtils.randomAlphabetic(12+ix));
            missIds.add(prefix + RandomStringUtils.randomAlphanumeric(10 + ix));
        }

        // To start with, NONE should be in the list, so they should all miss.
        for (final Duration duration : LogicalCacheImpl.DURATION_SET) {
            assertEquals(0, impl.checkIdsForDuplication(putIds, duration).size());
            assertEquals(0, impl.checkIdsForDuplication(missIds, duration).size());

            assertNull(impl.warnOfDuplication(putIds, duration));
            assertNull(impl.warnOfDuplication(missIds, duration));
        }

        // Put the data in the list for the first time
        impl.setBulk(data, target);

        // Check for misses.
        for (final Duration duration : LogicalCacheImpl.DURATION_SET) {
            if (duration == target) { continue; }
            assertEquals(0, impl.checkIdsForDuplication(missIds, duration).size());
            assertNull(impl.warnOfDuplication(missIds, duration));
        }

        // Check for expected duplication warnings.
        for (final Duration duration : LogicalCacheImpl.DURATION_SET) {
            if (duration == target) { continue; }
            final Set<String> notifyIds = impl.checkIdsForDuplication(putIds, duration);
            assertNotNull(notifyIds);
//            System.out.println(notifyIds);
            assertEquals(putIds.size(), notifyIds.size());
            final String warningText = impl.warnOfDuplication(putIds, duration);
//            System.out.println(warningText);
            assertNotNull(warningText);
            for (final String id : putIds) {
                assertTrue(warningText.contains(id));
            }
        }

        // And test the last little pre-expectations
        assertEquals(0, impl.checkIdsForDuplication(null, Duration.ONE_MINUTE).size());
        assertEquals(0, impl.checkIdsForDuplication(Collections.EMPTY_SET, Duration.ONE_MINUTE).size());
        assertNull(impl.warnOfDuplication(null, Duration.FIVE_MINUTES));
        assertNull(impl.warnOfDuplication(Collections.EMPTY_LIST, Duration.FIVE_MINUTES));
    }

    @Test
    public void testRoundTrip() {
        final String prefix = "roundTrip-";

        final Map<String, Object> submission = new HashMap<String, Object>();
        final List<String> keys = new ArrayList<String>();
        for (int ix = 0; ix < 25; ix++) {
            final String key = prefix + RandomStringUtils.randomAlphanumeric(5 + ix%5);
            keys.add(key);
            submission.put(key, RandomUtils.nextBoolean() ? null : RandomStringUtils.randomAlphanumeric(2 + ix%7));
        }

        final Duration target = Duration.NINETY_SECONDS;
        impl.setBulk(submission, target);

        final Set<String> requests = new HashSet<String>();
        Collections.shuffle(keys);
        final int goodSize = 10, badSize = 5;
        for (int ix = 0; ix < goodSize; ix++) { requests.add(keys.get(ix)); }
        for (int ix = 0; ix < badSize; ix++) { requests.add(RandomStringUtils.randomAlphanumeric(9)); }

        // Make sure there are NO hits for any other duration.
        for (final Duration duration : LogicalCacheImpl.DURATION_SET) {
            if (duration == target) { continue; }
            final Map<String, Object> attempt = impl.getBulk(requests, duration);
            assertNotNull(attempt);
            assertEquals(0, attempt.size());
        }

        // Now get the subset of the data that is referenced.
        final Map<String, Object> attempt = impl.getBulk(requests, target);
        assertNotNull(attempt);
        assertEquals(goodSize, attempt.size());
        for (final String request : requests) {
            if (!keys.contains(request)) {
                assertFalse(attempt.containsKey(request));
            } else {
                assertTrue(attempt.containsKey(request));
                assertEquals(submission.get(request), attempt.get(request));
            }
        }
    }

    @Test
    public void testInvalidation() throws Exception {
        final String prefix = "roundTrip-";

        final String keyAlpha = prefix + "alpha";
        final String bodyAlpha = prefix + RandomStringUtils.randomAlphabetic(8);
        final String keyOmega = prefix + "omega";
        final String bodyOmega = prefix + RandomStringUtils.randomAlphabetic(9);

        final Map<String, Object> submission = new HashMap<String, Object>();
        submission.put(keyAlpha, bodyAlpha);
        submission.put(keyOmega, bodyOmega);

        final List<String> inquiryKeys = Arrays.asList(keyAlpha, prefix+"bubba", keyOmega, prefix+"gump");

        for (final Duration duration : LogicalCacheImpl.DURATION_SET) {
            final Map<String, Object> attempt = impl.getBulk(inquiryKeys, duration);
            assertNotNull(attempt);
            assertEquals(0, attempt.size());
        }

        // This will blow a bunch of DUPLICATION errors, but we're not testing that right here.
        for (final Duration duration : LogicalCacheImpl.DURATION_SET) {
            impl.setBulk(submission, duration);

            // Verify the data got into the caches as we expect.
            final Map<String, Object> results = impl.getBulk(inquiryKeys, duration);
            assertEquals(bodyAlpha, results.get(keyAlpha));
            assertEquals(bodyOmega, results.get(keyOmega));
        }

        // Now, blow the values away.
        impl.invalidateBulk(inquiryKeys);

        // No data should be found.
        for (final Duration duration : LogicalCacheImpl.DURATION_SET) {
            final Map<String, Object> attempt = impl.getBulk(inquiryKeys, duration);
            assertNotNull(attempt);
            assertEquals(0, attempt.size());
        }
    }

}
