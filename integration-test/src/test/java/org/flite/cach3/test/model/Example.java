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

package org.flite.cach3.test.model;

import org.flite.cach3.api.*;

import java.io.*;

public class Example implements CacheConditionally, Externalizable {

    private Long pk;
    private String body;

    public Long getPk() {
        return pk;
    }

    public Example setPk(Long pk) {
        this.pk = pk;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Example setBody(String body) {
        this.body = body;
        return this;
    }

    // TODO: Possibly accept a boolean that says whether or not we want it to be conditional.
    public boolean isCacheable() {
        return pk != null && pk % 5 != 0;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(pk);
        out.writeObject(body);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        pk = (Long) in.readObject();
        body = (String) in.readObject();
    }
}
