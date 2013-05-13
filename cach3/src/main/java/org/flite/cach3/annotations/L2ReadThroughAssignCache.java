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

package org.flite.cach3.annotations;

import org.flite.cach3.annotations.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface L2ReadThroughAssignCache {
    /**
     * A namespace that is added to the key as it is stored in the distributed cache.
     * This allows differing object that may have the same ID to coexist.
     * This value must be assigned.
     * @return the namespace for the objects cached in the given method.
     */
    String namespace() default AnnotationConstants.DEFAULT_STRING;

    /**
     * A single key that is assigned to the value that is returned from this method. This key
     * will be combined with the <code>namespace()</code> value to be used in the distributed cache.
     * This value must be assigned.
     * @return the assigned key for the given data
     */
    String assignedKey() default AnnotationConstants.DEFAULT_STRING;

    /**
     * The maximum length of time that a value should live in the cache.
     *
     * The user must be careful to ensure that any invocation of the cache for a given
     * key have the exact same <code>Duration</code>. (In effect, the <code>Duration</code>
     * becomes a part of the unique key.) If there are multiple invocations for a given
     * key with different <code>Duration</code>s set, there will be multiple (conflicting?) copies
     * of the value in the cache.
     *
     */
    Duration window() default Duration.UNDEFINED;

}
