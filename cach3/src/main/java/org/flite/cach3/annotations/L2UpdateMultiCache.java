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
public @interface L2UpdateMultiCache {
    /**
   	 * A namespace that is added to the key as it is stored in the distributed cache.
   	 * This allows differing object that may have the same ID to coexist.
   	 * This value must be assigned.
   	 * @return the namespace for the objects cached in the given method.
   	 */
   	String namespace() default AnnotationConstants.DEFAULT_STRING;

   	/**
   	 * Of the arguments passed into the cached method, this identifies which
   	 * argument provides the id by which the object will be cached. This is a
   	 * 0-based array index. This annotation also takes a special value of -1 to signify
   	 * that the object being returned is the object responsible for providing the cache key.
   	 * @return the index into the arguments array for the item that will provide the id
   	 */
   	int keyIndex() default AnnotationConstants.DEFAULT_KEY_INDEX;

    /**
     * This is an <em>optional</em> value for the user to provide a Velocity template
     * to create the cache id(s). In any of the *MultiCache annotations, keyIndex() is still
     * required, so that the underlying logic has an indication of the dimensionality
     * of the multi-values.
     * For the Velocity template Context, there will be two defined variables:
     *  1 - args: An array of the objects passed into the method as parameters
     *  2 - index: the integer pointer of where in the list of multi-values that we are creating an id for.
     */
    String keyTemplate() default AnnotationConstants.DEFAULT_STRING;

    /**
     * Since keys and the actual data to be cached may be different, we also need to know which
     * parameter (or output) holds the data that we should update the cache with. This is a
     * 0-based array index. This annotation also takes a special value of -1 to signify
     * that the object being returned is the data that should be cached.
     * @return the index into the argument array that holds the actual data to be cached
     */
    int dataIndex() default Integer.MIN_VALUE;

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

    /**
     * An optional String prefix that will be pre-pended to the id returned by the object
     * addressed by the keyIndex. If supplied, must not contain whitespace characters.
     * @return the defined prefix
     */
    String keyPrefix() default AnnotationConstants.DEFAULT_STRING;

}
