package org.flite.cach3.test;

import org.flite.cach3.test.dao.TestDAOImpl;
import org.flite.cach3.test.listeners.StubReadThroughAssignCacheListenerImpl;
import org.flite.cach3.test.listeners.StubUpdateAssignCacheListenerImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;
import org.flite.cach3.test.svc.TestSvc;

import java.util.List;
import java.util.ArrayList;

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
public class UpdateAssignCacheTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {
        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final StubUpdateAssignCacheListenerImpl listener =
                (StubUpdateAssignCacheListenerImpl) context.getBean("stubUA");

        final List<String> result1 = test.getAssignStrings();

        final List<String> altData = new ArrayList<String>();
        for (int ix = 0; ix < result1.size(); ix++) {
            if (ix % 2 == 0) {
                altData.add(result1.get(ix));
            }
        }

        final int previous = listener.getTriggers().size();
        test.updateAssignStrings(altData);

        // Testing that the listener got invoked as required.
        assertTrue("Doesn't look like the listener got called.", listener.getTriggers().size() == previous+1);
        final String expected = StubUpdateAssignCacheListenerImpl.formatTriggers(TestDAOImpl.ASSIGN_NAMESPACE, TestDAOImpl.ASSIGN_KEY, altData, null, new Object[] {25, altData});
        assertEquals(expected, listener.getTriggers().get(listener.getTriggers().size() - 1));

        final List<String> result2 = test.getAssignStrings();

        assertNotSame(result1.size(), result2.size());
        assertEquals(altData.size(), result2.size());
        for (int ix = 0; ix < result2.size(); ix++) {
            assertEquals(altData.get(ix), result2.get(ix));
        }

    }
}
