package org.flite.cach3.config;

import org.flite.cach3.api.*;

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

public class Cach3State {

    private boolean cacheDisabled = false;

    private List<InvalidateAssignCacheListener> iaListeners = new ArrayList<InvalidateAssignCacheListener>();
    private List<InvalidateSingleCacheListener> isListeners = new ArrayList<InvalidateSingleCacheListener>();
    private List<InvalidateMultiCacheListener> imListeners = new ArrayList<InvalidateMultiCacheListener>();
    private List<ReadThroughAssignCacheListener> rtaListeners = new ArrayList<ReadThroughAssignCacheListener>();
    private List<ReadThroughSingleCacheListener> rtsListeners = new ArrayList<ReadThroughSingleCacheListener>();
    private List<ReadThroughMultiCacheListener> rtmListeners = new ArrayList<ReadThroughMultiCacheListener>();
    private List<UpdateAssignCacheListener> uaListeners = new ArrayList<UpdateAssignCacheListener>();
    private List<UpdateSingleCacheListener> usListeners = new ArrayList<UpdateSingleCacheListener>();
    private List<UpdateMultiCacheListener> umListeners = new ArrayList<UpdateMultiCacheListener>();

    public boolean isCacheDisabled() {
        return cacheDisabled;
    }

    public void setCacheDisabled(boolean cacheDisabled) {
        this.cacheDisabled = cacheDisabled;
    }

    // InvalidateAssign
    public void registerIAListener(final InvalidateAssignCacheListener listener) {
        iaListeners.add(listener);
    }

    public List<InvalidateAssignCacheListener> getIAListeners() {
        return iaListeners;
    }

    // InvalidateSingle
    public void registerISListener(final InvalidateSingleCacheListener listener) {
        isListeners.add(listener);
    }

    public List<InvalidateSingleCacheListener> getISListeners() {
        return isListeners;
    }

    // InvalidateMulti
    public void registerIMListener(final InvalidateMultiCacheListener listener) {
        imListeners.add(listener);
    }

    public List<InvalidateMultiCacheListener> getIMListeners() {
        return imListeners;
    }

    // ReadThroughAssign
    public void registerRTAListener(final ReadThroughAssignCacheListener listener) {
        rtaListeners.add(listener);
    }

    public List<ReadThroughAssignCacheListener> getRTAListeners() {
        return rtaListeners;
    }

    // ReadThroughSingle
    public void registerRTSListener(final ReadThroughSingleCacheListener listener) {
        rtsListeners.add(listener);
    }

    public List<ReadThroughSingleCacheListener> getRTSListeners() {
        return rtsListeners;
    }

    // ReadThroughMulti
    public void registerRTMListener(final ReadThroughMultiCacheListener listener) {
        rtmListeners.add(listener);
    }

    public List<ReadThroughMultiCacheListener> getRTMListeners() {
        return rtmListeners;
    }

    // UpdateAssign
    public void registerUAListener(final UpdateAssignCacheListener listener) {
        uaListeners.add(listener);
    }

    public List<UpdateAssignCacheListener> getUAListeners() {
        return uaListeners;
    }

    // UpdateSingle
    public void registerUSListener(final UpdateSingleCacheListener listener) {
        usListeners.add(listener);
    }

    public List<UpdateSingleCacheListener> getUSListeners() {
        return usListeners;
    }

    // UpdateMulti
    public void registerUMListener(final UpdateMultiCacheListener listener) {
        umListeners.add(listener);
    }

    public List<UpdateMultiCacheListener> getUMListeners() {
        return umListeners;
    }

}
