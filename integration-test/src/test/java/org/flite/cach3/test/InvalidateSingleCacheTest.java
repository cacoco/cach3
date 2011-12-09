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
public class InvalidateSingleCacheTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {
        final Long key1 = System.currentTimeMillis();
        final Long key2 = System.currentTimeMillis() + 10000;

        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubInvalidateSingleCacheListenerImpl listener =
                (StubInvalidateSingleCacheListenerImpl) context.getBean("stubIS");

        final String f1 = test.getRandomString(key1);
        final String f2 = test.getRandomString(key2);
        assertEquals(f1, test.getRandomString(key1));
        assertEquals(f2, test.getRandomString(key2));
        assertEquals(f1, test.getRandomString(key1));
        assertEquals(f2, test.getRandomString(key2));
        assertEquals(f1, test.getRandomString(key1));
        assertEquals(f2, test.getRandomString(key2));

        final int previous = listener.getTriggers().size();
        test.updateRandomString(key1);

        // Make sure the listener is getting triggered.
        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubInvalidateSingleCacheListenerImpl.formatTriggers(TestDAOImpl.SINGLE_NAMESPACE, null, key1.toString(), null,new Object[] {key1});
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

        test.updateRandomString(key2);

        final String s1 = test.getRandomString(key1);
        final String s2 = test.getRandomString(key2);

        assertNotSame(f1, s1);
        assertNotSame(f2, s2);

        assertEquals(s1, test.getRandomString(key1));
        assertEquals(s2, test.getRandomString(key2));
        assertEquals(s1, test.getRandomString(key1));
        assertEquals(s2, test.getRandomString(key2));
        assertEquals(s1, test.getRandomString(key1));
        assertEquals(s2, test.getRandomString(key2));

        test.updateRandomStringAgain(key1);
        test.updateRandomStringAgain(key2);

        final String t1 = test.getRandomString(key1);
        final String t2 = test.getRandomString(key2);

        assertNotSame(s1, t1);
        assertNotSame(s2, t2);

        assertEquals(t1, test.getRandomString(key1));
        assertEquals(t2, test.getRandomString(key2));
    }

    @Test
    public void testVelocity() {
        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubInvalidateSingleCacheListenerImpl listener =
                (StubInvalidateSingleCacheListenerImpl) context.getBean("stubIS");

        final String original = RandomStringUtils.randomAlphanumeric(4);
        final String replace = RandomStringUtils.randomAlphanumeric(6);
        final String finish = RandomStringUtils.randomAlphanumeric(8);

        final Long first = System.currentTimeMillis() + 3600000;
        final Long second = first + 1337;
        final String baseKey = first.toString() + "&&" + second.toString();

        final String r1 = test.getCompoundString(first, original, second);
        assertEquals(r1, original);

        final String r2 = test.getCompoundString(first, replace, second);
        assertEquals(r2, original);

        final int previous = listener.getTriggers().size();
        test.invalidateCompoundString(second, first);

        // Make sure the listener is getting triggered.
        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubInvalidateSingleCacheListenerImpl.formatTriggers(TestDAOImpl.COMPOUND_NAMESPACE, TestDAOImpl.COMPOUND_PREFIX, baseKey, null, new Object[] {second, first});
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

        // Now, by retrieving again we ensure the invalidate actually took place.
        final String r3 = test.getCompoundString(first, finish, second);
        assertEquals(r3, finish);
    }
}
