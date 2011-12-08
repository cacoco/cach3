package org.flite.cach3.test;

import org.apache.commons.lang.*;
import org.flite.cach3.test.dao.*;
import org.flite.cach3.test.listeners.*;
import org.flite.cach3.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.testng.annotations.*;

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
public class ReadThroughSingleCacheTest {

	private ApplicationContext context;

	@BeforeClass
	public void beforeClass() {
		context = new ClassPathXmlApplicationContext("/test-context.xml");
	}

	@Test
	public void test() {
		final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubReadThroughSingleCacheListenerImpl listener =
                (StubReadThroughSingleCacheListenerImpl) context.getBean("stubRTS");

        final String currentKey = "TestKey-" + System.currentTimeMillis();

        final int previous = listener.getTriggers().size();
		final String s1 = test.getDateString(currentKey);

        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubReadThroughSingleCacheListenerImpl.formatTriggers(TestDAOImpl.DATE_NAMESPACE, null, currentKey, s1, null, new Object[] {currentKey});
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

		for (int ix = 0; ix < 10; ix++) {
			assertEquals(String.format("Cache didn't seem to bring back [%s] as expectd.", s1), s1, test.getDateString(currentKey));
		}
	}

    @Test
    public void testVelocity() {
        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubReadThroughSingleCacheListenerImpl listener =
                (StubReadThroughSingleCacheListenerImpl) context.getBean("stubRTS");

        final String v1 = "pass-through1" + System.currentTimeMillis();
        final Long keyB = System.currentTimeMillis();
        final Long key1a = 123L;
        final Long key2a = 1234L;
        final String expectedKey = key1a.toString() + "&&" + keyB.toString();

        final int previous = listener.getTriggers().size();
        final String r1 = test.getCompoundString(key1a, v1, keyB);
        assertEquals(v1, r1);

        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubReadThroughSingleCacheListenerImpl.formatTriggers(TestDAOImpl.COMPOUND_NAMESPACE, TestDAOImpl.COMPOUND_PREFIX, expectedKey, v1, null, new Object[] {key1a, v1, keyB});
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

        // This time through, we'd better get the CACHED value, not the one being sent in.
        final String v2 = "pt2-" + RandomStringUtils.randomAlphanumeric(4);
        final String r2 = test.getCompoundString(key1a, v2, keyB);
        assertFalse(v2.equals(r2));
        assertEquals(v1, r2);

        // Try a new set of keys, should pass through fine.
        final String v3 = "pt3--" + RandomStringUtils.randomAlphanumeric(5);
        final String r3 = test.getCompoundString(key2a, v3, keyB);
        assertEquals(v3, r3);

        // Now, even with this 'new' set of keys, we should get the last returned value.
        final String v4 = "pt4xx" + RandomStringUtils.randomAlphanumeric(6);
        final String r4 = test.getCompoundString(key2a, v4, keyB);
        assertFalse(v4.equals(r4));
        assertEquals(v3, r4);
    }
}
