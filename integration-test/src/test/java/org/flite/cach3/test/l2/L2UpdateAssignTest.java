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

import static org.testng.AssertJUnit.*;

public class L2UpdateAssignTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {
        final TestSvc test = (TestSvc) context.getBean("testSvc");

        // Set the first cached datum
        final String orig = test.getL2AssignGolf(999L, RandomStringUtils.randomAlphabetic(4) + "-");

        // Make sure the value is definitely in there
        for (int ix = 0; ix < 3; ix++) {
            assertEquals(orig, test.getL2AssignGolf(1000L+ix, RandomStringUtils.randomAlphanumeric(4+ix) + "-"));
        }

        // Force the update to happen.
        final String renew = test.getL2AssignHotel(System.currentTimeMillis(), RandomStringUtils.randomAlphanumeric(8) + "-");
        assertFalse(orig.equals(renew));

        // Make sure the NEW value is definitely in there
        for (int ix = 0; ix < 3; ix++) {
            assertEquals(renew, test.getL2AssignGolf(1000L+ix, RandomStringUtils.randomAlphanumeric(8+ix) + "-"));
        }

    }

}
