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

package org.flite.cach3.test.l2;

import org.apache.commons.lang.*;
import org.flite.cach3.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.AssertJUnit.assertTrue;

public class L2UpdateMultiTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {

        final TestSvc test = (TestSvc) context.getBean("testSvc");

        final String g1 = RandomStringUtils.randomAlphabetic(4) + "-";

        final List<Long> ids = new ArrayList<Long>();
        final long base = System.currentTimeMillis() - 200000;
        for (int ix = 0; ix < 10; ix++) {
            ids.add(base + ix);
        }

        final List<Long> addls = new ArrayList<Long>(ids);
        for (int ix = 0; ix < 10; ix++) {
            addls.add(1000L + ix);
        }
        Collections.shuffle(addls);

        // Set the base expectations
        final List<String> first = test.getL2MultiAlpha(addls, g1);
        for (final String out : first) {
            assertTrue(out.startsWith(g1));
        }

        // Now call the update
        final String g2 = RandomStringUtils.randomAlphabetic(6) + "-";
        final List<String> second = test.getL2MultiBeta(ids, g2);
        for (final String out : second) {
            assertTrue(out.startsWith(g2));
        }

        // Only the updated ones should be different.
        Collections.shuffle(addls);
        final String g3 = RandomStringUtils.randomAlphabetic(8) + "-";
        System.out.println("G1: " + g1);
        System.out.println("G2: " + g2);
        System.out.println("G3: " + g3);
        final List<String> results = test.getL2MultiAlpha(addls, g3);
        for (int ix = 0; ix < addls.size(); ix++) {
            final Long key = addls.get(ix);
            final String result = results.get(ix);
            System.out.println(result);
            assertTrue(StringUtils.contains(result, key.toString()));
            assertTrue("Key: " + key, result.startsWith(ids.contains(key) ? g2 : g1));
        }

    }
}
