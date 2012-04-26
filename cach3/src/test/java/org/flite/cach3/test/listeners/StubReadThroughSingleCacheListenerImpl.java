package org.flite.cach3.test.listeners;

import org.flite.cach3.api.*;

import java.util.*;

/**
Copyright (c) 2011-2012 Flite, Inc

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
public class StubReadThroughSingleCacheListenerImpl implements ReadThroughSingleCacheListener {
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

    public void triggeredReadThroughSingleCache(final String namespace,
                                                final String prefix,
                                                final String baseCacheId,
                                                final Object submission,
                                                final Object[] args) {
        triggers.add(formatTriggers(namespace, prefix, baseCacheId, submission, null, args));
    }

    public static final String SEP = " [-] ";
    public static String formatTriggers(final String namespace,
                                        final String prefix,
                                        final String baseCacheId,
                                        final Object submission,
                                        final Object retVal,
                                        final Object[] args) {
        final StringBuilder sb = new StringBuilder(namespace)
                .append(SEP)
                .append(prefix)
                .append(SEP)
                .append(baseCacheId)
                .append(SEP)
                .append(submission)
                .append(SEP)
                .append(retVal)
                .append(SEP);
        if (args != null && args.length > 0) {
            for (int ix = 0; ix < args.length; ix++) {
                sb.append(args[ix] == null ? "null" : args[ix].toString()).append(SEP);
            }
        }
        return sb.toString();
    }

    @Deprecated
    public static String formatTriggers(final String namespace, final String prefix, final Object keyObject, final Object submission) {
        return String.format("%s [-] %s [-] %s [-] %s", namespace, prefix, keyObject, submission);
    }
}
