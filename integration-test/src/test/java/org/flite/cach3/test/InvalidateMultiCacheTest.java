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

package org.flite.cach3.test;

import org.apache.commons.lang.*;
import org.apache.commons.lang.math.*;
import org.flite.cach3.test.dao.*;
import org.flite.cach3.test.listeners.*;
import org.flite.cach3.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.AssertJUnit.*;

public class InvalidateMultiCacheTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {
        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubInvalidateMultiCacheListenerImpl listener =
                (StubInvalidateMultiCacheListenerImpl) context.getBean("stubIM");

        final List<Long> allIds = new ArrayList<Long>();
        final List<Long> noChangeIds = new ArrayList<Long>();
        final List<Long> firstChangeIds = new ArrayList<Long>();
        final List<String> firstChangeStringIds = new ArrayList<String>();
        final List<Long> secondChangeIds = new ArrayList<Long>();

        final Long base = RandomUtils.nextLong();
        for (int ix = 0; ix < 30; ix++) {
            final Long key = base + (ix * 100);
            allIds.add(key);
            if (ix % 3 == 0) { noChangeIds.add(key); }
            if (ix % 3 == 1) {
                firstChangeIds.add(key);
                firstChangeStringIds.add(key.toString());
            }
            if (ix % 3 == 2) { secondChangeIds.add(key); }
        }

        final Map<Long, String> firstMap = createMap(allIds, test.getRandomStrings(allIds));
        assertEquals(firstMap, createMap(allIds, test.getRandomStrings(allIds)));
        assertEquals(firstMap, createMap(allIds, test.getRandomStrings(allIds)));

        final int previous = listener.getTriggers().size();
        test.updateRandomStrings(firstChangeIds);

        // Make sure the listener is getting triggered.
        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubInvalidateMultiCacheListenerImpl.formatTriggers(TestDAOImpl.MULTI_NAMESPACE, null, firstChangeStringIds, null, new Object[] {firstChangeIds});
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

        final Map<Long, String> secondMap = createMap(allIds, test.getRandomStrings(allIds));
        for (Map.Entry<Long, String> entry : secondMap.entrySet()) {
            final Long key = entry.getKey();
            if (firstChangeIds.contains(key)) {
                assertNotSame(entry.getValue(), firstMap.get(key));
            } else {
                assertEquals(entry.getValue(), firstMap.get(key));
            }
        }
        assertEquals(secondMap, createMap(allIds, test.getRandomStrings(allIds)));
        assertEquals(secondMap, createMap(allIds, test.getRandomStrings(allIds)));

        test.updateRandomStringsAgain(secondChangeIds);
        final Map<Long, String> thirdMap = createMap(allIds, test.getRandomStrings(allIds));
        for (Map.Entry<Long, String> entry : thirdMap.entrySet()) {
            final Long key = entry.getKey();
            if (noChangeIds.contains(key)) {
                assertEquals(entry.getValue(), firstMap.get(key));
            }
            if (firstChangeIds.contains(key)) {
                assertEquals(entry.getValue(), secondMap.get(key));
            }
            if (secondChangeIds.contains(key)) {
                assertNotSame(entry.getValue(), firstMap.get(key));
            }
        }
        assertEquals(thirdMap, createMap(allIds, test.getRandomStrings(allIds)));
        assertEquals(thirdMap, createMap(allIds, test.getRandomStrings(allIds)));
    }

    Map<Long, String> createMap(final List<Long> keys, final List<String> values) {
        assertEquals(keys.size(), values.size());

        final Map<Long, String> result = new HashMap<Long, String>(keys.size());
        for (int ix = 0; ix < keys.size(); ix++) {
            result.put(keys.get(ix), values.get(ix));
        }
        return result;
    }

    @Test
    public void testVelocity() {
        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubInvalidateMultiCacheListenerImpl listener =
                (StubInvalidateMultiCacheListenerImpl) context.getBean("stubIM");

        final String original = RandomStringUtils.randomAlphanumeric(10);
        final String replace = RandomStringUtils.randomAlphanumeric(8);
        final Long second = 10661337L;

        final long base = RandomUtils.nextInt(2000) + 4000;
        final List<Long> keys = new ArrayList<Long>();
        final List<Long> invalidSubset = new ArrayList<Long>();
        final List<String> baseKeys = new ArrayList<String>();
        for (int ix = 0; ix < 4; ix++) {
            final Long id = base + ix;
            keys.add(id);
            if (ix % 2  == 0) {
                invalidSubset.add(id);
                baseKeys.add(id.toString() + "&&" + second.toString());
            }
        }

        // Set up the cache via the read-thru.
        final List<String> r1 = test.getCompoundStrings(keys, original, second);
        for (final String str : r1) {
            assertEquals(original, str);
        }

        final int previous = listener.getTriggers().size();
        test.invalidateCompoundStrings(second, invalidSubset);

        // Make sure the listener is getting triggered.
        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubInvalidateMultiCacheListenerImpl.formatTriggers(TestDAOImpl.COMPOUND_NAMESPACE, TestDAOImpl.COMPOUND_PREFIX, baseKeys, null, new Object[] {second, invalidSubset});
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

        // Now we ensure that the invalidation occurred.
        final List<String> r2 = test.getCompoundStrings(keys, replace, second);
        for (int ix = 0; ix < keys.size(); ix++) {
            if (invalidSubset.contains(keys.get(ix))) {
                assertEquals(replace, r2.get(ix));
            } else {
                assertEquals(original, r2.get(ix));
            }
        }
    }
}
