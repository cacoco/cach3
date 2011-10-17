package org.flite.cach3.config;

import org.apache.commons.logging.*;
import org.flite.cach3.api.*;
import org.springframework.beans.factory.*;
import org.springframework.context.*;

import java.security.*;
import java.util.*;

/**
 * Copyright (c) 2011 Flite, Inc
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

public class Cach3State implements ApplicationContextAware, InitializingBean {
    private static final Log LOG = LogFactory.getLog(Cach3State.class);

    private boolean cacheDisabled = false;
    private ApplicationContext context;

    private Map<Class<? extends CacheListener>, List<? extends CacheListener>> listeners = new HashMap<Class<? extends CacheListener>, List<? extends CacheListener>>();
    {
        listeners.put(InvalidateAssignCacheListener.class, new ArrayList<InvalidateAssignCacheListener>());
        listeners.put(InvalidateSingleCacheListener.class, new ArrayList<InvalidateSingleCacheListener>());
        listeners.put(InvalidateMultiCacheListener.class, new ArrayList<InvalidateMultiCacheListener>());
        listeners.put(ReadThroughAssignCacheListener.class, new ArrayList<ReadThroughAssignCacheListener>());
        listeners.put(ReadThroughSingleCacheListener.class, new ArrayList<ReadThroughSingleCacheListener>());
        listeners.put(ReadThroughMultiCacheListener.class, new ArrayList<ReadThroughMultiCacheListener>());
        listeners.put(UpdateAssignCacheListener.class, new ArrayList<UpdateAssignCacheListener>());
        listeners.put(UpdateSingleCacheListener.class, new ArrayList<UpdateSingleCacheListener>());
        listeners.put(UpdateMultiCacheListener.class, new ArrayList<UpdateMultiCacheListener>());
    }

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        final Map<String, CacheListener> beans = context.getBeansOfType(CacheListener.class);
        if (beans == null || beans.isEmpty()) {
            LOG.info(String.format("No beans of type [%s] found.", CacheListener.class.getName()));
            return;
        }

        for (final Map.Entry<String, CacheListener> entry : beans.entrySet()) {
            final String beanName = entry.getKey();
            final CacheListener listener = entry.getValue();
            if (listener != null) {
                addListener(listener);
                LOG.debug(String.format("Added bean: [%s] - {%s}", beanName, listener.getClass().getName()));
            }
        }
    }

    public boolean isCacheDisabled() {
        return cacheDisabled;
    }

    public void setCacheDisabled(boolean cacheDisabled) {
        this.cacheDisabled = cacheDisabled;
    }

    public void addListener(final CacheListener listener) {
        if (listener == null) { return; }

        if (listener instanceof InvalidateAssignCacheListener) {
            final List<InvalidateAssignCacheListener> list = (List<InvalidateAssignCacheListener>)listeners.get(InvalidateAssignCacheListener.class);
            list.add((InvalidateAssignCacheListener) listener);
        }

        if (listener instanceof InvalidateSingleCacheListener) {
            final List<InvalidateSingleCacheListener> list = (List<InvalidateSingleCacheListener>)listeners.get(InvalidateSingleCacheListener.class);
            list.add((InvalidateSingleCacheListener) listener);
        }

        if (listener instanceof InvalidateMultiCacheListener) {
            final List<InvalidateMultiCacheListener> list = (List<InvalidateMultiCacheListener>)listeners.get(InvalidateMultiCacheListener.class);
            list.add((InvalidateMultiCacheListener) listener);
        }

        if (listener instanceof UpdateAssignCacheListener) {
            final List<UpdateAssignCacheListener> list = (List<UpdateAssignCacheListener>)listeners.get(UpdateAssignCacheListener.class);
            list.add((UpdateAssignCacheListener) listener);
        }

        if (listener instanceof UpdateSingleCacheListener) {
            final List<UpdateSingleCacheListener> list = (List<UpdateSingleCacheListener>)listeners.get(UpdateSingleCacheListener.class);
            list.add((UpdateSingleCacheListener) listener);
        }

        if (listener instanceof UpdateMultiCacheListener) {
            final List<UpdateMultiCacheListener> list = (List<UpdateMultiCacheListener>)listeners.get(UpdateMultiCacheListener.class);
            list.add((UpdateMultiCacheListener) listener);
        }

        if (listener instanceof ReadThroughAssignCacheListener) {
            final List<ReadThroughAssignCacheListener> list = (List<ReadThroughAssignCacheListener>)listeners.get(ReadThroughAssignCacheListener.class);
            list.add((ReadThroughAssignCacheListener) listener);
        }

        if (listener instanceof ReadThroughSingleCacheListener) {
            final List<ReadThroughSingleCacheListener> list = (List<ReadThroughSingleCacheListener>)listeners.get(ReadThroughSingleCacheListener.class);
            list.add((ReadThroughSingleCacheListener) listener);
        }

        if (listener instanceof ReadThroughMultiCacheListener) {
            final List<ReadThroughMultiCacheListener> list = (List<ReadThroughMultiCacheListener>)listeners.get(ReadThroughMultiCacheListener.class);
            list.add((ReadThroughMultiCacheListener) listener);
        }
    }

    public <L extends CacheListener> List<L> getListeners(final Class<L> type) {
        if (type == null) { throw new InvalidParameterException("Type must be defined."); }

        final List<L> listenerList = (List<L>) listeners.get(type);
        if (listenerList == null) { throw new InvalidParameterException(String.format("No listeners found of type [%s]",type.getName())); }

        return listenerList;
    }

}
