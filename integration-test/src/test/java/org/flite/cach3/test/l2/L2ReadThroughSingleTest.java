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

import static org.testng.AssertJUnit.*;

public class L2ReadThroughSingleTest {
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
            final long id = base + ix;
            ids.add(id);
            assertTrue(test.getL2SingleDelta(id, g1).startsWith(g1));
        }

        final String g2 = RandomStringUtils.randomAlphabetic(6) + "-";
        final List<Long> addls = new ArrayList<Long>();
        addls.addAll(ids);
        for (int ix = 0; ix < 10; ix++) {
            addls.add(1000L + ix);
        }

        Collections.shuffle(addls);
        final List<String> results = test.getL2MultiAlpha(addls, g2);
        for (int ix = 0; ix < addls.size(); ix++) {
            final Long key = addls.get(ix);
            final String result = results.get(ix);
            System.out.println(result);
            assertTrue(StringUtils.contains(result, key.toString()));
            assertTrue(result.startsWith(ids.contains(key) ? g1 : g2));
        }

    }
}
