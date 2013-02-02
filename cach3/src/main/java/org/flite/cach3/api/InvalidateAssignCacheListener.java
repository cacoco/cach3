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

package org.flite.cach3.api;

public interface InvalidateAssignCacheListener extends CacheListener {

    /**
     * This method gets triggered only after a modification to the cache is made.
     * Actual cache ids are built the following way:
     *  (namespace) + ":" + (assignKey)
     *
     * @param namespace  The string supplied to the associated annotation
     * @param assignKey  The specific cache id assigned to the associated annotation
     * @param retVal  Object returned by the underlying method (null if of void return type)
     * @param args  Object[] that are the passed in parameters to the underlying method (empty/null if no-arg)
     */
    void triggeredInvalidateAssignCache(String namespace, String assignKey, Object retVal, Object[] args);
}
