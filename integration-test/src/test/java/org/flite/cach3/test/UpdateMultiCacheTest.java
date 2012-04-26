package org.flite.cach3.test;

import org.apache.commons.lang.*;
import org.apache.commons.lang.math.*;
import org.flite.cach3.test.dao.*;
import org.flite.cach3.test.listeners.*;
import org.flite.cach3.test.svc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.testng.annotations.*;

import java.util.*;

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
public class UpdateMultiCacheTest {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMultiCacheTest.class);

	private ApplicationContext context;

	@BeforeClass
	public void beforeClass() {
		context = new ClassPathXmlApplicationContext("/test-context.xml");
	}

	@Test
	public void test() {
		final Long rawNow = System.currentTimeMillis();
		final Long now = (rawNow / 1000) * 10000;
		final List<Long> subset = new ArrayList<Long>();
        final List<String> subsetIds = new ArrayList<String>();
		final List<Long> superset = new ArrayList<Long>();

		for (Long ix = 1 + now; ix < 35 + now; ix++) {
			if (ix % 3 == 0) {
				subset.add(ix);
                subsetIds.add(ix.toString());
			}
			superset.add(ix);
		}

		final Map<Long, String> originalResults = new HashMap<Long, String>();
		final Map<Long, String> expectedResults = new HashMap<Long, String>();

		final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubUpdateMultiCacheListenerImpl listener =
                (StubUpdateMultiCacheListenerImpl) context.getBean("stubUM");

		final List<String> r1List = test.getTimestampValues(superset);
		for (int ix = 0; ix < r1List.size(); ix++) {
			final Long key = superset.get(ix);
			final String value = r1List.get(ix);

			originalResults.put(key, value);
			if (!subset.contains(key)) {
				expectedResults.put(key, value);
			}
		}

        final int previous = listener.getTriggers().size();
		final List<String> subsetUpdateResult = test.updateTimestamValues(subset);

        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubUpdateMultiCacheListenerImpl.formatTriggers(TestDAOImpl.TIME_NAMESPACE,
                null,
                subsetIds,
                (List<Object>) (List) subsetUpdateResult, // Using Erasure to satisfy the compiler. YUCK!
                (List<Object>) (List) subsetUpdateResult,
                new Object[] {subset});
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

		for (int ix = 0; ix < subset.size(); ix++) {
			final Long key = subset.get(ix);
			final String value = subsetUpdateResult.get(ix);
			assertFalse(originalResults.get(key).equals(value));
			assertTrue(value.indexOf("-M-") != -1);
			expectedResults.put(key, value);
		}

		final List<String> r2List = test.getTimestampValues(superset);
		for (int ix = 0; ix < r2List.size(); ix++) {
			final Long key = superset.get(ix);
			final String value = r2List.get(ix);
			LOG.info(value);
			assertEquals(expectedResults.get(key), value);
		}
	}

    @Test
    public void testDataIndex() {
        final Map<Long, String> expectedResults = new HashMap<Long, String>();
        final Long rawNow = System.currentTimeMillis();
        final Long now = (rawNow / 1000) * 10000;
        final List<Long> subset = new ArrayList<Long>();
        final List<String> overrideValues = new ArrayList<String>();
        final List<Long> superset = new ArrayList<Long>();

        for (Long ix = 1 + now; ix < 35 + now; ix++) {
            if (ix % 3 == 0) {
                subset.add(ix);
                final String overrideValue = "big-fat-override-value-" + ix;
                expectedResults.put(ix, overrideValue);
                overrideValues.add(overrideValue);
            }
            superset.add(ix);
        }

        final Map<Long, String> originalResults = new HashMap<Long, String>();

        final TestSvc test = (TestSvc) context.getBean("testSvc");

        final List<String> r1List = test.getTimestampValues(superset);
        for (int ix = 0; ix < r1List.size(); ix++) {
            final Long key = superset.get(ix);
            final String value = r1List.get(ix);

            originalResults.put(key, value);
            if (!subset.contains(key)) {
                expectedResults.put(key, value);
            }
        }

        test.overrideTimestampValues(42, subset, "Nada", overrideValues);

        final List<String> r2List = test.getTimestampValues(superset);
        for (int ix = 0; ix < r2List.size(); ix++) {
            final Long key = superset.get(ix);
            final String value = r2List.get(ix);
            LOG.info(value);
            assertEquals(expectedResults.get(key), value);
        }
    }

    @Test
    public void testVelocity() {

        final String original = RandomStringUtils.randomAlphanumeric(7);
        final Long second = Long.valueOf("1337" + RandomStringUtils.randomNumeric(5));
        final List<Long> firsts = new ArrayList<Long>();
        final List<String> baseIds = new ArrayList<String>();
        final long base = RandomUtils.nextInt(2000) + 1000;
        for (int ix = 0; ix < 3; ix++) {
            final Long val = base + ix;
            firsts.add(val);
            baseIds.add(val + "&&" + second);
        }
        final Long extra = base + 10;
        final String extraString = original + extra.toString();

        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubUpdateMultiCacheListenerImpl listener =
                (StubUpdateMultiCacheListenerImpl) context.getBean("stubUM");

        final int previous = listener.getTriggers().size();
		final List<String> results = test.updateCompoundStrings(second, original, firsts);

        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubUpdateMultiCacheListenerImpl.formatTriggers(TestDAOImpl.COMPOUND_NAMESPACE,
                TestDAOImpl.COMPOUND_PREFIX,
                baseIds,
                (List<Object>) (List) results, // Using Erasure to satisfy the compiler. YUCK!
                results,
                new Object[] {second, original, firsts});
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

        // This part just double-checks the sublist aspect of the ReadThroughMultiCache
        firsts.add(extra);
        Collections.shuffle(firsts);
        final List<String> r2 = test.getCompoundStrings(firsts, extraString, second);
        for (int ix = 0; ix < firsts.size(); ix++) {
            final Long value = firsts.get(ix);
            assertEquals(value.equals(extra) ? extraString : original, r2.get(ix));
        }
    }
}
