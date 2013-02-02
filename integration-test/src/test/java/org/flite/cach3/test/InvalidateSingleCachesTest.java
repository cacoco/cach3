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

import org.flite.cach3.test.svc.TestSvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InvalidateSingleCachesTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {

        final TestSvc test = (TestSvc) context.getBean("testSvc");

        //baseline
        test.setFunkFactor(5l, 0l);
        test.setFunkFactor(6l, 0l);
        Assert.assertEquals(test.funkySquare(5l), 25l);
        Assert.assertEquals(test.funkyCube(5l), 125l);
        Assert.assertEquals(test.funkySquare(6l), 36l);

        test.setFunkFactor(5l, 1l);
        test.setFunkFactor(6l, 1l);
        Assert.assertEquals(test.funkySquare(5l), 26l);
        Assert.assertEquals(test.funkyCube(5l), 126l);

        //set funk factor without clearing the cache
        test.undercoverSetFunkFactor(5l, 2l);
        test.undercoverSetFunkFactor(6l, 2l);

        //this should still be pulling from the cache, so it should return the old values
        Assert.assertEquals(test.funkySquare(5l), 26l);
        Assert.assertEquals(test.funkyCube(5l), 126l);

        //but uncached values should come in off by 2
        Assert.assertEquals(test.funkySquare(6l), 38l);

    }
}
