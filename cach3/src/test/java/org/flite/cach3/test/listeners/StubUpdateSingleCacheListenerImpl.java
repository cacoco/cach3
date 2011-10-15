package org.flite.cach3.test.listeners;

import org.flite.cach3.api.UpdateSingleCacheListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
Copyright (c) 2011 Flite, Inc

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
public class StubUpdateSingleCacheListenerImpl implements UpdateSingleCacheListener {
    private Set<String> interests = null;
    private List<String> triggers = new ArrayList<String>();

    public List<String> getTriggers() {
        return triggers;
    }

    public Set<String> getInterests() {
        return interests;
    }

    public void setInterests(Set<String> interests) {
        this.interests = interests;
    }

    public Set<String> getNamespacesOfInterest() {
        return interests;
    }

    public void triggeredUpdateSingleCache(String namespace, Object keyObject, Object submission) {
        triggers.add(formatTriggers(namespace, keyObject, submission));
    }

    public static String formatTriggers(final String namespace, final Object keyObject, final Object submission) {
        return String.format("%s [-] %s [-] %s", namespace, keyObject, submission);
    }
}
