package org.flite.cach3.test;

import org.flite.cach3.test.svc.TestSvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Copyright 2012 Flite, Inc.
 * All rights reserved.
 * <p/>
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
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
