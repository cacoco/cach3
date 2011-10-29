package org.flite.cach3.test.listeners;

import org.flite.cach3.api.*;

import java.security.*;
import java.util.*;

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
public class StubUpdateMultiCacheListenerImpl implements UpdateMultiCacheListener {
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

    public void triggeredUpdateMultiCache(final String namespace, final String prefix, final List<Object> keyObjects, final List<Object> submissions) {
        triggers.add(formatTriggers(namespace, prefix, keyObjects, submissions));
    }

    public static String formatTriggers(final String namespace, final String prefix, final List<Object> keyObjects, final List<Object> submissions) {
        if (keyObjects == null && submissions == null) {
            return String.format("%s [-] %s [-] null [-] null", namespace, prefix, keyObjects, submissions);
        }
        if (keyObjects == null
                || submissions == null
                || keyObjects.size() != submissions.size()) { throw new IllegalStateException("keys and submissions don't match."); }

        final Map<Object, Object> map = new HashMap<Object, Object>();
        for (int ix = 0; ix < keyObjects.size(); ix++) {
            if (map.put(keyObjects.get(ix), submissions.get(ix)) != null) {
                throw new InvalidParameterException("There seems to be duplicate keys. This may not be an actual problem, but this formatter is not equipped to handle that case.");
            }
        }

        final List<Comparable> sorted = new ArrayList<Comparable>((List<Comparable>)(List)keyObjects);
        Collections.sort(sorted);

        final StringBuilder sb = new StringBuilder(namespace).append(" ").append(prefix).append("");
        for (int ix = 0; ix < sorted.size(); ix++) {
            final Object key = sorted.get(ix);
            final Object value = map.get(key);
            sb.append(key).append("=").append(value).append(";");
        }

        return sb.toString();
    }
}
