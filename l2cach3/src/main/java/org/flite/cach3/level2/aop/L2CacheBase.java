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

package org.flite.cach3.level2.aop;

import org.aspectj.lang.JoinPoint;
import org.flite.cach3.aop.CacheBase;
import org.flite.cach3.aop.CacheKeyMethodStore;
import org.flite.cach3.config.VelocityContextFactory;
import org.slf4j.*;

import java.lang.reflect.Method;

public class L2CacheBase {
    private static final Logger LOG = LoggerFactory.getLogger(L2CacheBase.class);

    protected VelocityContextFactory factory;
    /*default*/ CacheKeyMethodStore methodStore;
    /*default*/ LogicalCacheIF cache = new LogicalCacheImpl(); // TODO: add setter/getter
    {
        try {
            ((LogicalCacheImpl) cache).afterPropertiesSet();
        } catch (Exception ex) {}
    }
    public void setFactory(VelocityContextFactory factory) {
        this.factory = factory;
    }

    public void setMethodStore(CacheKeyMethodStore methodStore) {
        this.methodStore = methodStore;
    }

    protected boolean isCacheDisabled() {
        LOG.debug(" > > > > ATTENTION!! - Not Yet Implemented!");
        return false;
//        return state == null ? false : state.isCacheDisabled();
    }

    public static Method getMethodToCache(final JoinPoint jp) throws NoSuchMethodException {
        return CacheBase.getMethodToCache(jp);
    }
    public static void verifyReturnTypeIsList(final Method method, final Class annotationClass) {
        CacheBase.verifyReturnTypeIsList(method, annotationClass);
    }

    protected Method getKeyMethod(final Object keyObject) throws NoSuchMethodException {
        return CacheBase.getKeyMethod(keyObject, methodStore);
    }

    public static String generateObjectId(final Method keyMethod, final Object keyObject) throws Exception {
        return CacheBase.generateObjectId(keyMethod, keyObject);
    }

    public static String buildCacheKey(final String objectId, final String namespace, final String prefix) {
        return CacheBase.buildCacheKey(objectId, namespace, prefix);
    }


}
