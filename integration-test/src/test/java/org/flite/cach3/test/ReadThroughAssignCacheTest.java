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

import org.flite.cach3.test.dao.*;
import org.flite.cach3.test.listeners.*;
import org.flite.cach3.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.AssertJUnit.*;

public class ReadThroughAssignCacheTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {
        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubReadThroughAssignCacheListenerImpl listener =
                (StubReadThroughAssignCacheListenerImpl) context.getBean("stubRTA");

        // First things first; to ensure an empty cache, invalidate any previous data in the assign strings.
        test.invalidateAssignStrings();

        final int previous = listener.getTriggers().size();
        final List<String> result1 = test.getAssignStrings();

        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubReadThroughAssignCacheListenerImpl.formatTriggers(TestDAOImpl.ASSIGN_NAMESPACE, TestDAOImpl.ASSIGN_KEY, result1, null);
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

        final List<String> result2 = test.getAssignStrings();

        assertEquals(result1.size(), result2.size());
        for (int ix = 0; ix < result1.size(); ix++) {
            assertEquals(result1.get(ix), result2.get(ix));
        }
    }
}
