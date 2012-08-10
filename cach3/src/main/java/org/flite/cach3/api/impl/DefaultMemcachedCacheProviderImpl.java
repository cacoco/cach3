package org.flite.cach3.api.impl;

import net.spy.memcached.*;
import org.flite.cach3.api.*;
import org.slf4j.*;
import org.springframework.jmx.export.annotation.*;

/**
 * Copyright (c) 2011-2012 Flite, Inc
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
@ManagedResource
public class DefaultMemcachedCacheProviderImpl implements MemcachedClientProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMemcachedCacheProviderImpl.class);

    private boolean useConsistentHashing;
    private String nodeList = null;
    private MemcachedClientIF client;

    public DefaultMemcachedCacheProviderImpl(final boolean useConsistentHashing, final String nodeList) throws Exception {
        this.useConsistentHashing = useConsistentHashing;
        this.nodeList = nodeList;
        refreshConnection();
    }

    public MemcachedClientIF getMemcachedClient() {
        return this.client;
    }

    @ManagedOperation
    public void refreshConnection() throws Exception {
        LOG.info("Creating a new connection to the memcached server(s)");
        final ConnectionFactory cf = useConsistentHashing ? new KetamaConnectionFactory() : new DefaultConnectionFactory();
        this.client = new MemcachedClient(cf, AddrUtil.getAddresses(nodeList));
    }
}
