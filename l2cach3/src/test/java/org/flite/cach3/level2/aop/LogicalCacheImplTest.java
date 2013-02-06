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
            System.out.println(warningText);
            assertNotNull(warningText);
            for (final String id : putIds) {
                assertTrue(warningText.contains(id));
            }
        }

        // And test the last little pre-expectations
        assertNotNull(impl.checkIdsForDuplication(null, Duration.ONE_MINUTE));
        assertNotNull(impl.checkIdsForDuplication(Collections.EMPTY_SET, Duration.ONE_MINUTE));
        assertNull(impl.warnOfDuplication(null, Duration.FIVE_MINUTES));
        assertNull(impl.warnOfDuplication(Collections.EMPTY_LIST, Duration.FIVE_MINUTES));

    }

}
