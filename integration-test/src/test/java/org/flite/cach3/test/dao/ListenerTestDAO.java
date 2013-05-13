package org.flite.cach3.test.dao;

import org.flite.cach3.test.model.TestObject;

/**
 * Copyright 2013 Flite, Inc.
 * All rights reserved.
 * <p/>
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public interface ListenerTestDAO {

    TestObject save(TestObject testObject);

    TestObject undercoverSave(TestObject testObject);

    void delete(TestObject testObject);

    void undercoverDelete(TestObject testObject);


    TestObject getById(Long id);

    TestObject getByGuid(String guid);


    TestObject getByIdFromListener(Long id);

    TestObject getByGuidFromListener(String guid);


}
