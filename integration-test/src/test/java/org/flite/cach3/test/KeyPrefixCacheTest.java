package org.flite.cach3.test;

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
public class KeyPrefixCacheTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {
        final List<Long> allIds = new ArrayList<Long>();
        final long base = System.currentTimeMillis();
        for (int ix = 0; ix < 30; ix++) {
            allIds.add(base + (ix*5));
        }
        final Long singleId = allIds.get(0);
        final List<Long> multiIds = new ArrayList<Long>();
        multiIds.add(allIds.get(1));
        multiIds.add(allIds.get(2));
        multiIds.add(allIds.get(3));
        Collections.shuffle(allIds);

        final Map<Long, Integer> positions = new HashMap<Long, Integer>();
        positions.put(singleId, allIds.indexOf(singleId));
        for (final Long id : allIds) {
            positions.put(id, allIds.indexOf(id));
        }

        final TestSvc test = (TestSvc) context.getBean("testSvc");

        // Test the ReadThrough's
        final String sResult1 = test.getDwarf(singleId);
        final List<String> mResult1 = test.getDwarves(allIds);

        final int singleLoc = allIds.indexOf(singleId);
        assertEquals(sResult1, mResult1.get(singleLoc));

        // Testing the invalidations.
        test.invalidateDwarf(singleId);
        final List<String> mResult2 = test.getDwarves(allIds);
        final int pos1 = positions.get(singleId);
        assertFalse(sResult1.equals(mResult2.get(pos1)));

        test.invalidateDwarves(multiIds);
        final List<String> mResult3 = test.getDwarves(allIds);
        for (final Long id : multiIds) {
            final int pos = positions.get(id);
            assertFalse(mResult2.get(pos).equals(mResult3.get(pos)));
        }

        // Now, test the updates.
        final String sResult2 = test.updateDwarf(singleId);
        final List<String> mResult4 = test.getDwarves(allIds);
        assertTrue(sResult2.equals(mResult4.get(pos1)));

        final List<String> uResult1 = test.updateDwarves(multiIds);
        final List<String> mResult5 = test.getDwarves(allIds);
        for (int ix = 0; ix < multiIds.size(); ix++) {
            final Long id = multiIds.get(ix);
            final int pos = positions.get(id);
            assertEquals(uResult1.get(ix), mResult5.get(pos));
        }
    }
}
