package org.flite.cach3.test.model;

import java.io.*;

/**
 * Copyright 2013 Flite, Inc.
 * All rights reserved.
 * <p/>
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class TestObject implements Externalizable {

    private Long id;
    private String guid;
    private String content;

    public TestObject() {}

    public TestObject(Long id, String guid, String content) {
        this.id = id;
        this.guid = guid;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeObject(guid);
        out.writeObject(content);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (Long) in.readObject();
        guid = (String) in.readObject();
        content = (String) in.readObject();
    }
}
