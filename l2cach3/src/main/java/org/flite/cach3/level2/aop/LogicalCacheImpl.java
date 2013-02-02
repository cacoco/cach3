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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.flite.cach3.level2.annotations.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LogicalCacheImpl implements LogicalCacheIF {
    private static final Logger LOG = LoggerFactory.getLogger(LogicalCacheImpl.class);

    final Cache<String, Duration> nanny;

    public LogicalCacheImpl() {
        this.nanny = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(5, TimeUnit.MINUTES).build();
    }

    public Map<String, Object> getBulk(Collection<String> ids, Duration duration) {
        if (Duration.UNDEFINED == duration) { throw new InvalidParameterException("UNDEFINED is not an allowed value"); }
        warnOfDuplication(ids, duration);

        throw new RuntimeException("Not Yet Implemented!");
//        return null;
    }

    /*default*/ static final String ERROR = "WARNING! Conflicting expiration durations for a given key will result in " +
            "duplicate cache entries. Evidence of conflicts found for the given key(s):";
    /*default*/ void warnOfDuplication(final Collection<String> ids, final Duration duration) {
        if (ids == null || ids.size() < 1) { return; }
        final Set<String> results = checkIdsForDuplication(ids, duration);
        if (results.size() > 0) {
            final StringBuilder sb = new StringBuilder(ERROR);
            for (final String single : results) {
                sb.append('\n').append(single);
            }
            LOG.warn(sb.toString());
        }
    }

    /*default*/ Set<String> checkIdsForDuplication(final Collection<String> ids, final Duration duration) {
        if (ids == null || ids.size() < 1) { return Collections.EMPTY_SET; }
        final Map<String, Duration> history = nanny.getAllPresent(ids);
        if (history == null || history.size() < 1) { return Collections.EMPTY_SET; }

        final Set<String> results = new HashSet<String>(ids.size());
        for (final Map.Entry<String, Duration> entry : history.entrySet()) {
            final Duration previous = entry.getValue();
            if (duration != previous) {
                results.add(duration.name() + " vs " + previous.name() + ": " + entry.getKey());
            }
        }

        return results;
    }
}
