package org.flite.cach3.test;

import net.spy.memcached.*;
import org.apache.commons.lang.*;
import org.apache.commons.lang.math.*;
import org.flite.cach3.config.*;
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
public class ReadThroughMultiCacheTest {
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
        final List<Long> complement = new ArrayList<Long>();
		final List<Long> superset = new ArrayList<Long>();
		final List<Long> jumbleset = new ArrayList<Long>();
        final List<String> complementResult = new ArrayList<String>(complement.size());

		for (Long ix = 1 + now; ix < 35 + now; ix++) {
			if (ix % 3 == 0) {
				subset.add(ix);
			} else {
                complement.add(ix);
                complementResult.add(null);
            }
			superset.add(ix);
			jumbleset.add(ix);
		}
		Collections.shuffle(jumbleset);

		final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubReadThroughMultiCacheListenerImpl listener =
                (StubReadThroughMultiCacheListenerImpl) context.getBean("stubRTM");

		// Get all the results for the subset ids.
		// Ensure the ids line up with the results, and have the same timestamp.
		final List<String> subsetResult = test.getTimestampValues(subset);
		assertEquals(subset.size(), subsetResult.size());
		String subsetTime = null;
		for (int ix = 0; ix < subset.size(); ix++) {
			final Long key = subset.get(ix);
			final String value = subsetResult.get(ix);
			System.out.println("Subset: " + value);
			final String[] parts = value.split("-X-");
			if (subsetTime == null) {
				subsetTime = parts[0];
			} else {
				assertEquals(subsetTime, parts[0]);
			}
			assertEquals(key.toString(), parts[1]);
		}

		// Now call the full list.
		// Ensure id's line up, and that results from ids that got passed in the subset
		// have the older time stamp.
        final int previous = listener.getTriggers().size();
		final List<String> supersetResult = test.getTimestampValues(superset);

		assertEquals(superset.size(), supersetResult.size());
		String supersetTime = null;
		for (int ix = 0; ix < superset.size(); ix++) {
			final Long key = superset.get(ix);
			final String value = supersetResult.get(ix);
			System.out.println("Superset: " + value);
			final String[] parts = value.split("-X-");
			final boolean inSubset = subset.contains(key);
			if (!inSubset && supersetTime == null) {
				supersetTime = parts[0];
			} else if (inSubset) {
				assertEquals(subsetTime, parts[0]);
			} else {
				assertEquals(supersetTime, parts[0]);
			}
			assertEquals(key.toString(), parts[1]);

            if (!inSubset) {
                int index = complement.indexOf(key);
                complementResult.set(index, value);
            }
		}

        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);

		// Now call for the results again, but with a randomized
		// set of keys.  This is to ensure the proper values line up with
		// the given keys.
		final List<String> jumblesetResult = test.getTimestampValues(jumbleset);
		assertEquals(jumbleset.size(), jumblesetResult.size());
		for (int ix = 0; ix < jumbleset.size(); ix++) {
			final Long key = jumbleset.get(ix);
			final String value = jumblesetResult.get(ix);
			System.out.println("Jumbleset: " + value);
			final String[] parts = value.split("-X-");
			final boolean inSubset = subset.contains(key);
			if (inSubset) {
				assertEquals(subsetTime, parts[0]);
			} else {
				assertEquals(supersetTime, parts[0]);
			}
			assertEquals(key.toString(), parts[1]);
		}

	}

	@Test
	public void testMemcached() {
		final MemcachedClientIF cache = ((Cach3State) context.getBean("cach3-state")).getMemcachedClient();

		final List<String> keys = new ArrayList<String>();
		final Map<String, String> answerMap = new HashMap<String, String>();
		final Long now = System.currentTimeMillis();
		final String alphabet = "abcdefghijklmnopqrstuvwxyz";
		for (int ix = 0; ix < 5; ix++) {
			final String key = alphabet.charAt(ix) + now.toString();
			final String value = alphabet.toUpperCase().charAt(ix) + "00000";
			cache.set(key, 30, value);
			keys.add(key);
			answerMap.put(key, value);
		}

		final Map<String, Object> memcachedSez = cache.getBulk(keys);

		assertTrue(memcachedSez.equals(answerMap));
	}

    @Test
    public void testVelocity() {
        final Long constant = RandomUtils.nextLong();

        final List<Long> firsts = new ArrayList<Long>();
        final List<Long> thrus = new ArrayList<Long>();
        final Long f1 = System.currentTimeMillis();
        firsts.add(f1);

        final Long base = f1 + RandomUtils.nextInt(1000) + 500;
        for (int ix = 0; ix < 4; ix++) {
            firsts.add(base+ix);
            thrus.add(base+ix);
        }
        Collections.shuffle(firsts);

        final String early = RandomStringUtils.randomAlphanumeric(8);
        final String late = RandomStringUtils.randomAlphanumeric(12);

        final List<String> bodies = new ArrayList<String>();
        final List<String> keys = new ArrayList<String>();
        final List<String> subs = new ArrayList<String>();
        for (int ix = 0; ix < firsts.size(); ix++) {
            if (firsts.get(ix).equals(f1)) {
                bodies.add(early);
            } else {
                bodies.add(late);
                subs.add(late);
                keys.add(firsts.get(ix).toString() + "&&" + constant);
            }
        }

        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubReadThroughMultiCacheListenerImpl listener =
                (StubReadThroughMultiCacheListenerImpl) context.getBean("stubRTM");

        // This just primes the cache so we can ensure the sub-set feature works in the multi-cache example.
        test.getCompoundString(f1, early, constant);

        // Get the before count of objects that have been triggered, so we can isolate if the call triggers.
        final int previous = listener.getTriggers().size();

        final List<String> results = test.getCompoundStrings(firsts, late, constant);
        for (int ix = 0; ix < results.size(); ix++) {
            assertEquals(bodies.get(ix), results.get(ix));
        }

        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);

        final String expected = StubReadThroughMultiCacheListenerImpl.formatTriggers(
                TestDAOImpl.COMPOUND_NAMESPACE,
                TestDAOImpl.COMPOUND_PREFIX,
                keys,
                (List<Object>) (List) subs, // Using Erasure to satisfy the compiler. YUCK!
                null,
                new Object[]{thrus, late, constant});
        System.out.println("Expected = " + expected);
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));
    }
}
