package org.flite.cach3.test;

import org.apache.commons.lang.math.*;
import org.flite.cach3.test.dao.*;
import org.flite.cach3.test.listeners.*;
import org.flite.cach3.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.AssertJUnit.*;

/**
Copyright (c) 2011 Flite, Inc

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
}
