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

import org.apache.commons.lang.RandomStringUtils;
import org.flite.cach3.test.svc.TestSvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

public class CombinedLevelsTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() throws Exception {
        final TestSvc test = (TestSvc) context.getBean("testSvc");
        final String key = RandomStringUtils.randomAlphanumeric(12);

        final long start = System.currentTimeMillis();

        final Long initial = test.getCombinedData(key);
        final long initialValue = initial.longValue();
        assertEquals(4, initial.toString().length());

        // For the first little bit, make sure that both levels of caching are returning the same info.
        while(System.currentTimeMillis() - start < 1500) {
//            System.out.println("1 - 2nd: " + test.getCombinedData(key).longValue());
//            System.out.println("1 - 1st: " + test.getL1Data(key).longValue());
            assertEquals(initialValue, test.getCombinedData(key).longValue());
            assertEquals(initialValue, test.getL1Data(key).longValue());

            Thread.sleep(200);
        }

        // Now force an update to the Level 1 cache, but don't have it get in the L2 cache
        final Long update = test.updateL1Data(key);
        final long updateValue = update.longValue();
        assertEquals(6, update.toString().length());

        // Make sure the values continue to diverge while the L2 cache window is still open (< 4.75sec)
        while(System.currentTimeMillis() - start < 4750) {
//            System.out.println("2 - 2nd: " + test.getCombinedData(key).longValue());
//            System.out.println("2 - 1st: " + test.getL1Data(key).longValue());
            assertEquals(initialValue, test.getCombinedData(key).longValue());
            assertEquals(updateValue, test.getL1Data(key).longValue());

            Thread.sleep(500);
        }

        // Wait until the L2 cache timeout of 5sec is good and expired, now try again.
        Thread.sleep(1000);

        // We expect the L2 cache to fall through to the V1 cache, which still has a good value.
        final long lastValue = test.getCombinedData(key).longValue();
        assertEquals(updateValue, lastValue);

//        System.out.println("3 - 2nd: " + test.getCombinedData(key).longValue());
//        System.out.println("3 - 1st: " + test.getL1Data(key).longValue());

    }

}
