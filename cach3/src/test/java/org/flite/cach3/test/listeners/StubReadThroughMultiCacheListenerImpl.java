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
public class StubReadThroughMultiCacheListenerImpl implements ReadThroughMultiCacheListener {
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

    public void triggeredReadThroughMultiCache(final String namespace,
                                               final String prefix,
                                               final List<String> baseCacheIds,
                                               final List<Object> submissions,
                                               final Object[] alteredArgs) {
        this.triggers.add(formatTriggers(namespace, prefix, baseCacheIds, submissions, alteredArgs));
    }

    public static final String SEP = " [-] ";
    public static String formatTriggers(final String namespace,
                                               final String prefix,
                                               final List<String> baseCacheIds,
                                               final List<Object> submissions,
                                               final Object[] alteredArgs) {
        final StringBuilder sb = new StringBuilder(namespace).append(SEP).append(prefix).append(SEP);

        final int idLen = baseCacheIds != null ? baseCacheIds.size() : 0;
        final int obLen = submissions != null ? submissions.size() : 0;
        if (idLen != obLen) { throw new InvalidParameterException("Unequal lengths of objects."); }

        final List<String> compounds = new ArrayList<String>(idLen);
        for (int ix = 0; ix < idLen; ix++) {
            compounds.add(baseCacheIds.get(ix).toString() + "-&&-" + submissions.get(ix).toString());
        }
        Collections.sort(compounds);
        for (final String compound : compounds) {
            sb.append(compound).append(SEP);
        }

        if (alteredArgs != null && alteredArgs.length > 0) {
            for (int ix = 0; ix < alteredArgs.length; ix++) {
                final Object arg = alteredArgs[ix];
                if (arg instanceof Collection) {
                    final Collection collection = (Collection) arg;
                    if (collection != null) {
                        if (collection instanceof List) { Collections.sort((List) collection); }
                        for (final Object item : collection) {
                            sb.append(item == null ? "" : item.toString()).append("&_&");
                        }
                    }
                    sb.append(SEP);
                } else {
                    sb.append(alteredArgs[ix].toString()).append(SEP);
                }
            }
        }
        return sb.toString();
    }
}
