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

import java.util.*;

public interface ReadThroughMultiCacheListener extends CacheListener {

    /**
     * This method gets triggered only after a modification to the cache is made.
     * Actual cache ids are built the following way:
     *  (namespace) + ":" + (prefix, if exists) + (baseCacheId)
     *
     * @param namespace  The string supplied to the associated annotation
     * @param prefix  The string optionally supplied to the associated annotation
     * @param baseCacheIds The calculated ids (no prefix, no namespace) for the objects written to the cache
     * @param submissions  The objects that were just written to the cache
     * @param alteredArgs  Object[] that are the passed in parameters to the underlying method (Remember:
     *                      the object at keyIndex is a list, an it represents only the subset of items that were
     *                      read from the underlying method and written to the cache.
     */
    void triggeredReadThroughMultiCache(String namespace, String prefix, List<String> baseCacheIds, List<Object> submissions, Object[] alteredArgs);
}
