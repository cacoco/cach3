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

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UpdateSingleCache {

	/**
	 * A namespace that is added to the key as it is stored in the distributed cache.
	 * This allows differing object that may have the same ID to coexist.
	 * This value must be assigned.
	 * @return the namespace for the objects cached in the given method.
	 */
	String namespace() default AnnotationConstants.DEFAULT_STRING;

    /**
     * An optional String prefix that will be pre-pended to the id returned by the object
     * addressed by the keyIndex. If supplied, must not contain whitespace characters.
     * @return the defined prefix
     */
    String keyPrefix() default AnnotationConstants.DEFAULT_STRING;

    /**
	 * Of the arguments passed into the cached method, this identifies which
	 * argument provides the id by which the object will be cached. This is a
	 * 0-based array index. This annotation also takes a special value of -1 to signify
	 * that the object being returned is the object responsible for providing the cache key.
     * This is mutually exclusive with keyTemplate(), i.e. one and only one of
     * keyIndex() and keyTemplate() must be defined.
	 * @return the index into the arguments array for the item that will provide the id
	 */
	int keyIndex() default AnnotationConstants.DEFAULT_KEY_INDEX;

    /**
     * This is a value for the user to provide a Velocity template
     * to create the cache id.
     * For the Velocity template Context, there will be two defined variables:
     *  1 - args: An array of the objects passed into the method as parameters
     *  2 - index: the integer pointer of where in the list of multi-values that we are creating an id for.
     * This is mutually exclusive with keyIndex(), i.e. one and only one of
     * keyIndex() and keyTemplate() must be defined.
     */
    String keyTemplate() default AnnotationConstants.DEFAULT_STRING;

    /**
     * Since keys and the actual data to be cached may be different, we also need to know which
     * parameter (or output) holds the data that we should update the cache with. This is a
	 * 0-based array index. This annotation also takes a special value of -1 to signify
	 * that the object being returned is the data that should be cached.
     * @return the index into the argument array that holds the actual data to be cached
     */
    int dataIndex() default AnnotationConstants.DEFAULT_DATA_INDEX;

    /**
     * This is an <em>optional</em> value for the user to provide a Velocity template
     * to store the data. In any of the *MultiCache annotations, dataIndex() is still
     * required, so that the underlying logic has an indication of the dimensionality
     * of the multi-values.
     * For the Velocity template Context, there will be two defined variables:
     *  1 - args: An array of the objects passed into the method as parameters
     *  2 - retVal: pointer to the object that is returned
     */
    String dataTemplate() default AnnotationConstants.DEFAULT_STRING;

    /**
     * This is an <em>optional</em> value which takes in class. If dataTemplate is
     * provided by default data is stored as string. You can pass in the class you
     * want the string to be transcoded to. Currently we support only Long and Integer
     */
    Class dataTemplateType() default String.class;

    /**
     * The exp value is passed along to memcached, and will be
     * processed per the memcached protocol specification:
     *
     * The actual value sent may either be Unix time (number of seconds since January 1, 1970,
     * as a 32-bit value), or a number of seconds starting from current time. In the latter case,
     * this number of seconds may not exceed 60*60*24*30 (number of seconds in 30 days); if the
     * number sent by a client is larger than that, the server will consider it to be real Unix
     * time value rather than an offset from current time.
     *
     * (Also note: a value of 0 means the given value should never expire. The value is still
     * susceptible to purging by memcached for space and LRU (least recently used) considerations.)
     *
     * @return
     */
    int expiration() default 0;

    /**
     * The jitter value is used as a bounded randomizer for the expiration time. Jittering your
     * expiration times can help to alleviate stampeding if many objects all expire at the same
     * time.
     *
     * Jitter will not be applied to expirations that exceed 60*60*24*30.
     *
     * The default value of -1 means the calculations will rely whatever jitter value is set
     * as a system default in the <code>Cach3State</code>.
     *
     * Other accepted values are integers between 0 and 99. These numbers represent a bounded
     * percentage of possible reductions to the expiration time.
     *
     * For example, if you have an <code>expiration</code> value of 1000, and you apply a
     * jitter value of 20, a modification to the cache would set the actual expiration
     * to a random value between 800 and 1000.
     */
    int jitter() default -1;

}
