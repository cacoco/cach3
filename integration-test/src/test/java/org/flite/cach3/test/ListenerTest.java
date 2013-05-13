package org.flite.cach3.test;

import junit.framework.Assert;
import org.flite.cach3.test.dao.ListenerTestDAO;
import org.flite.cach3.test.model.TestObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Copyright 2013 Flite, Inc.
 * All rights reserved.
 * <p/>
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class ListenerTest {

    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }


    @Test
    public void test() {

        final ListenerTestDAO dao = (ListenerTestDAO) context.getBean("ListenerTestDAO");

        TestObject testObj;

        dao.delete(new TestObject(1l, "abracadabra", "some content"));

        testObj = dao.getById(1l);
        Assert.assertNull(testObj);

        testObj = dao.getByGuid("abracadabra");
        Assert.assertNull(testObj);

        testObj = dao.getByIdFromListener(1l);
        Assert.assertNull(testObj);

        testObj = dao.getByGuidFromListener("abracadabra");
        Assert.assertNull(testObj);


        dao.save(new TestObject(1l, "abracadabra", "some content"));


        testObj = dao.getById(1l);
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some content", testObj.getContent());

        testObj = dao.getByGuid("abracadabra");
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some content", testObj.getContent());


        testObj = dao.getByIdFromListener(1l);
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some content", testObj.getContent());


        testObj = dao.getByGuidFromListener("abracadabra");
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some content", testObj.getContent());


        dao.undercoverSave(new TestObject(1l, "abracadabra", "some other content"));

        testObj = dao.getById(1l);
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some content", testObj.getContent());

        testObj = dao.getByGuid("abracadabra");
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some content", testObj.getContent());

        testObj = dao.getByIdFromListener(1l);
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some content", testObj.getContent());


        testObj = dao.getByGuidFromListener("abracadabra");
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some content", testObj.getContent());


        dao.save(new TestObject(1l, "abracadabra", "some awesome content"));

        testObj = dao.getById(1l);
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some awesome content", testObj.getContent());

        testObj = dao.getByGuid("abracadabra");
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some awesome content", testObj.getContent());

        testObj = dao.getByIdFromListener(1l);
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some awesome content", testObj.getContent());


        testObj = dao.getByGuidFromListener("abracadabra");
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some awesome content", testObj.getContent());



        dao.undercoverDelete(new TestObject(1l, "abracadabra", "some content"));

        testObj = dao.getById(1l);
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some awesome content", testObj.getContent());

        testObj = dao.getByGuid("abracadabra");
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some awesome content", testObj.getContent());

        testObj = dao.getByIdFromListener(1l);
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some awesome content", testObj.getContent());


        testObj = dao.getByGuidFromListener("abracadabra");
        Assert.assertEquals(1l, testObj.getId().longValue());
        Assert.assertEquals("abracadabra", testObj.getGuid());
        Assert.assertEquals("some awesome content", testObj.getContent());


        dao.delete(new TestObject(1l, "abracadabra", "some content"));

        testObj = dao.getById(1l);
        Assert.assertNull(testObj);

        testObj = dao.getByGuid("abracadabra");
        Assert.assertNull(testObj);

        testObj = dao.getByIdFromListener(1l);
        Assert.assertNull(testObj);

        testObj = dao.getByGuidFromListener("abracadabra");
        Assert.assertNull(testObj);

    }
}
