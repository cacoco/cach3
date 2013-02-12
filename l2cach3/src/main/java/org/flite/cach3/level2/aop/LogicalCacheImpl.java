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
import org.flite.cach3.aop.*;
import org.flite.cach3.level2.annotations.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.*;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LogicalCacheImpl implements LogicalCacheIF, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(LogicalCacheImpl.class);

    /*default*/ static final Set<Duration> DURATION_SET = EnumSet.complementOf(EnumSet.of(Duration.UNDEFINED));
    private Cache<String, Duration> nanny;
    private Map<Duration, Cache<String, Object>> caches = new HashMap<Duration, Cache<String, Object>>(10);

    public void afterPropertiesSet() throws Exception {
        this.nanny = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(5, TimeUnit.MINUTES).build();

        // TODO: Refactor to use a configurable set of cache sizes.
        for (final Duration duration : DURATION_SET) {
            final Cache<String, Object> single = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(1000, TimeUnit.MILLISECONDS).build();
            caches.put(duration, single);
        }
    }

    /**
     * Returns only the id & object pairs that are actually in the cache.
     * @param ids
     * @param duration
     * @return
     */
    public Map<String, Object> getBulk(Collection<String> ids, Duration duration) {
        if (duration == null || Duration.UNDEFINED == duration) { throw new InvalidParameterException("UNDEFINED is not an allowed value"); }
        warnOfDuplication(ids, duration);

        final Map<String, Object> partial = caches.get(duration).getAllPresent(ids);
        if (partial.size() == 0) { return partial; }

        final Map<String, Object> results = new HashMap<String, Object>(partial.size());
        for (Map.Entry<String, Object> entry : partial.entrySet()) {
            final Object value = entry.getValue() instanceof PertinentNegativeNull ? null : entry.getValue();
            results.put(entry.getKey(), value);
        }
        return results;
    }

    public void invalidateBulk(final Collection<String> ids) {
        if (ids == null || ids.size() == 0) { return; }

        for (final Cache<String, Object> cache : caches.values()) {
            if (cache != null) { cache.invalidateAll(ids); }
        }
    }

    public void setBulk(Map<String, Object> contents, Duration duration) {
        if (duration == null || Duration.UNDEFINED == duration) { throw new InvalidParameterException("UNDEFINED is not an allowed value"); }
        if (contents.size() == 0) { return; }

        // Only do this on SET, because we want the GET to be super-optimized.
        warnOfDuplication(contents.keySet(), duration);

        final Cache<String, Object> cache = caches.get(duration);
        for (final Map.Entry<String, Object> entry : contents.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue() == null ? new PertinentNegativeNull() : entry.getValue();
            nanny.put(key, duration);
            cache.put(key, value);
        }
    }

    /*default*/ static final String WARNING = "WARNING! Conflicting expiration durations for a given key will result in " +
            "duplicate cache entries. Evidence of conflicts found for the given key(s):";
    /*default*/ String warnOfDuplication(final Collection<String> ids, final Duration duration) {
        if (ids == null || ids.size() < 1) { return null; }
        final Set<String> results = checkIdsForDuplication(ids, duration);
        if (results.size() == 0) { return null; }
        final StringBuilder sb = new StringBuilder(WARNING);
        for (final String single : results) {
            sb.append('\n').append(single);
        }
        final String resultString = sb.toString();
        LOG.warn(resultString);
        return resultString;
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
