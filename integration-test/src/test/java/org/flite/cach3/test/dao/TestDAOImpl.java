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

package org.flite.cach3.test.dao;

import org.apache.commons.lang.*;
import org.apache.commons.lang.math.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.annotations.groups.InvalidateSingleCaches;
import org.flite.cach3.level2.annotations.*;

import java.util.*;

public class TestDAOImpl implements TestDAO {

    public static final String DATE_NAMESPACE = "Alpha";
    public static final String TIME_NAMESPACE = "Bravo";
    public static final String SINGLE_NAMESPACE = "Charlie";
    public static final String MULTI_NAMESPACE = "Delta";
    public static final String ASSIGN_NAMESPACE = "Echo";
    public static final String ASSIGN_KEY = "SomePhatKey";
    public static final String PREFIX_NAMESPACE = "Foxtrot";
    public static final String PREFIX_STRING = "p-p-p-prefix-";

    public static final String COMPOUND_NAMESPACE = "Cmpnd";
    public static final String COMPOUND_PREFIX = "c2-";

	@ReadThroughSingleCache(namespace = DATE_NAMESPACE, keyIndex = 0, expiration = 30)
	public String getDateString(final String key) {
		final Date now = new Date();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException ex) {}
		return now.toString() + ":" + now.getTime();
	}

    @UpdateSingleCache(namespace = DATE_NAMESPACE, keyIndex = 1, dataIndex = 2, expiration = 30)
    public void overrideDateString(final int trash, final String key, final String overrideData) {}

    @ReadThroughMultiCache(namespace = TIME_NAMESPACE, keyIndex = 0, expiration = 300)
	public List<String> getTimestampValues(final List<Long> keys) {
		final List<String> results = new ArrayList<String>();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException ex) {}
		final Long now = System.currentTimeMillis();
		for (final Long key : keys) {
			results.add(now.toString() + "-X-" + key.toString());
		}
		return results;
	}

	@UpdateSingleCache(namespace = TIME_NAMESPACE, keyIndex = 0, expiration = 300, dataIndex = -1)
	public String updateTimestampValue(final Long key) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {}
		final Long now = System.currentTimeMillis();
		final String result = now.toString() + "-U-" + key.toString();
		return result;
	}

	@UpdateMultiCache(namespace = TIME_NAMESPACE, keyIndex = 0, expiration = 300, dataIndex = -1)
	public List<String> updateTimestamValues(final List<Long> keys) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {}
		final Long now = System.currentTimeMillis();
		final List<String> results = new ArrayList<String>();
		for (final Long key : keys) {
			results.add(now.toString() + "-M-" + key.toString());
		}
		return results;
	}

    @UpdateMultiCache(namespace = TIME_NAMESPACE, keyIndex = 1, dataIndex = 3, expiration = 300)
    public void overrideTimestampValues(final int trash,
                                        final List<Long> keys,
                                        final String nuthin,
                                        final List<String> overrideData) {}

    @ReadThroughSingleCache(namespace = SINGLE_NAMESPACE, keyIndex = 0, expiration = 1000)
    public String getRandomString(final Long key) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {}
        return RandomStringUtils.randomAlphanumeric(25 + RandomUtils.nextInt(30));
    }

    @InvalidateSingleCache(namespace = SINGLE_NAMESPACE, keyIndex = 0)
    public void updateRandomString(final Long key) {
        // Nothing really to do here.
    }

    @InvalidateSingleCache(namespace = SINGLE_NAMESPACE, keyIndex = -1)
    public Long updateRandomStringAgain(final Long key) {
        return key;
    }

    @ReadThroughMultiCache(namespace = MULTI_NAMESPACE, keyIndex = 0, expiration = 1000)
    public List<String> getRandomStrings(final List<Long> keys) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {}
        final String series = RandomStringUtils.randomAlphabetic(6);
        final List<String> results = new ArrayList<String>(keys.size());
        for (final Long key : keys) {
            results.add(series + "-" + key.toString() + "-" + RandomStringUtils.randomAlphanumeric(25 + RandomUtils.nextInt(30)));
        }
        return results;
    }

    @InvalidateMultiCache(namespace = MULTI_NAMESPACE, keyIndex = 0)
    public void updateRandomStrings(final List<Long> keys) {
        // Nothing to do.
    }

    @InvalidateMultiCache(namespace = MULTI_NAMESPACE, keyIndex = 0)
    public List<Long> updateRandomStringsAgain(final List<Long> keys) {
        return keys;
    }

    @ReadThroughAssignCache(assignedKey = ASSIGN_KEY, namespace = ASSIGN_NAMESPACE, expiration = 3000)
    public List<String> getAssignStrings() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {}
        final List<String> results = new ArrayList<String>();
        final long extra = System.currentTimeMillis() % 20;
        final String base = System.currentTimeMillis() + "";
        for (int ix = 0; ix < 20 + extra; ix++) {
            results.add(ix + "-" + base);
        }
        return results;
    }

    @InvalidateAssignCache(assignedKey = ASSIGN_KEY, namespace = ASSIGN_NAMESPACE)
    public void invalidateAssignStrings() { }

    @UpdateAssignCache(assignedKey = ASSIGN_KEY, namespace = ASSIGN_NAMESPACE, expiration = 3000, dataIndex = 1)
    public void updateAssignStrings(int bubpkus, final List<String> newData) { }

    @ReadThroughSingleCache(namespace = PREFIX_NAMESPACE, keyPrefix = PREFIX_STRING, keyIndex = 0, expiration = 3000)
    public String getDwarf(final Long id) {
        final List<Long> ids = new ArrayList<Long>();
        ids.add(id);
        return disneyBasedStrings(ids).get(0);
    }

    @ReadThroughMultiCache(namespace = PREFIX_NAMESPACE, keyPrefix = PREFIX_STRING, keyIndex = 0, expiration = 3000)
    public List<String> getDwarves(final List<Long> ids) {
        return disneyBasedStrings(ids);
    }

    @InvalidateSingleCache(namespace = PREFIX_NAMESPACE, keyPrefix = PREFIX_STRING, keyIndex = 0)
    public void invalidateDwarf(final Long id) { }

    @InvalidateMultiCache(namespace = PREFIX_NAMESPACE, keyPrefix = PREFIX_STRING, keyIndex = 0)
    public void invalidateDwarves(final List<Long> ids) { }

    @UpdateSingleCache(namespace = PREFIX_NAMESPACE, keyPrefix = PREFIX_STRING, keyIndex = 0, expiration = 3000, dataIndex = -1)
    public String updateDwarf(final Long id) {
        return "Snow Whte - " + id;
    }

    @UpdateMultiCache(namespace = PREFIX_NAMESPACE, keyPrefix = PREFIX_STRING, keyIndex = 0, expiration = 3000, dataIndex = -1)
    public List<String> updateDwarves(final List<Long> ids) {
        final List<String> results = new ArrayList<String>();
        for (final Long id : ids) {
            results.add(updateDwarf(id));
        }
        return results;
    }

    private static String[] dwarves = {"Sneezy", "Sleepy", "Dopey", "Doc", "Happy", "Bashful", "Grumpy"};
    private static List<String> disneyBasedStrings(final List<Long> ids) {
        final long current = System.currentTimeMillis();
        final String dwarf1 = dwarves[(int)(current % dwarves.length)];
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {}

        final List<String> results = new ArrayList<String>(ids.size());
        for (final Long id : ids) {
            final String dwarf2 = dwarves[(int)(id % dwarves.length)];
            results.add(dwarf1 + " - " + dwarf2 + " - " + current + " - " + id);
        }
        return results;
    }

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/
    /** *                  Methods using the velocity templating option.                * **/
    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/

    @ReadThroughSingleCache(
            namespace=COMPOUND_NAMESPACE,
            keyPrefix = COMPOUND_PREFIX,
            keyTemplate = "$args[0]&&$args[2]",
            expiration = 30)
    public String getCompoundString(final Long first, final String toReturn, final Long second) {
        return toReturn;
    }

    @ReadThroughMultiCache(
            namespace = COMPOUND_NAMESPACE,
            keyPrefix = COMPOUND_PREFIX,
            keyIndex = 0,
            keyTemplate = "$indexObject&&$args[2]",
            expiration = 30
    )
    public List<String> getCompoundStrings(final List<Long> first, final String toReturn, final Long second) {
        final List<String> results = new ArrayList<String>();
        for (final Long f : first) {
            results.add(toReturn);
        }
        return results;
    }

    @UpdateSingleCache(
            namespace = COMPOUND_NAMESPACE,
            keyPrefix = COMPOUND_PREFIX,
            dataIndex = 1,
            keyTemplate = "$args[2]&&$args[0]",
            expiration = 30
    )
    public String updateCompoundString(final Long second, final String toReturn, final Long first) {
        return toReturn;
    }

    @UpdateMultiCache(
            namespace = COMPOUND_NAMESPACE,
            keyPrefix = COMPOUND_PREFIX,
            keyIndex = 2,
            dataIndex = -1,
            keyTemplate = "$args[2][$index]&&$args[0]",
            expiration = 30
    )
    public List<String> updateCompoundStrings(final Long second, final String toReturn, final List<Long> first) {
        final List<String> results = new ArrayList<String>();
        for (final Long f : first) {
            results.add(toReturn);
        }
        return results;
    }

    @InvalidateSingleCache(
            namespace = COMPOUND_NAMESPACE,
            keyPrefix = COMPOUND_PREFIX,
            keyTemplate = "$args[1]&&$args[0]"
    )
    public void invalidateCompoundString(final Long second, final Long first) { }

    @InvalidateMultiCache(
            namespace = COMPOUND_NAMESPACE,
            keyPrefix = COMPOUND_PREFIX,
            keyIndex = 1,
            keyTemplate = "$indexObject&&$args[0]"
    )
    public void invalidateCompoundStrings(final Long second, final List<Long> first) { }



    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/
    /** *                  Mulitple cache methods.                                      * **/
    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/


    private Map<Long, Long> funkFactor = new HashMap<Long, Long>();

    @InvalidateSingleCaches(
            {@InvalidateSingleCache(namespace = PREFIX_NAMESPACE, keyPrefix = "square", keyIndex = 0),
            @InvalidateSingleCache(namespace = PREFIX_NAMESPACE, keyPrefix = "cube", keyIndex = 0)}
    )
    public void setFunkFactor(Long number, Long funkFactor) {
        this.funkFactor.put(number, funkFactor);
    }

    //this changes the funk factor without notifying the caching mechanism
    public void undercoverSetFunkFactor(Long number, Long funkFactor) {
        this.funkFactor.put(number, funkFactor);
    }

    private Long getFunkFactor(Long number) {
        if (funkFactor.containsKey(number)) {
            return funkFactor.get(number);
        }
        return 0l;
    }

    @ReadThroughSingleCache(namespace = PREFIX_NAMESPACE, keyPrefix = "square", keyIndex = 0, expiration = 3000)
    public Long funkySquare(Long number) {
        return number * number + getFunkFactor(number);
    }

    @ReadThroughSingleCache(namespace = PREFIX_NAMESPACE, keyPrefix = "cube", keyIndex = 0, expiration = 3000)
    public Long funkyCube(Long number) {
        return number * number * number + getFunkFactor(number);
    }


    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/
    /** *                  L2 cache methods.                                            * **/
    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/

    private static final String L2_CACHE = "L2Cach3";
    private static final String L2_PREFIX = "prfx-";
    private static final String TMPL_START = "shenanigans-";
    private static final String L2_ASSIGN = "dewey-cheatem-howe";

    @L2ReadThroughMultiCache(namespace = L2_CACHE,
            keyIndex = 0,
            keyPrefix = L2_PREFIX,
            keyTemplate = TMPL_START + "$args[0][$index]-$indexObject",
            window = Duration.FIVE_MINUTES)
    public List<String> getL2MultiAlpha(final List<Long> ids, final String generation) {
        final List<String> results = new ArrayList<String>(ids.size());
        for (final Long id : ids) {
            results.add(generation + id);
        }
        return results;
    }

    @L2UpdateMultiCache(namespace = L2_CACHE,
            keyIndex = 0,
            keyPrefix = L2_PREFIX,
            keyTemplate = TMPL_START + "$args[0][$index]-$args[0][$index]",
            dataIndex = -1,
            window = Duration.FIVE_MINUTES)
    public List<String> getL2MultiBeta(final List<Long> ids, final String generation) {
        final List<String> results = new ArrayList<String>(ids.size());
        for (final Long id : ids) {
            results.add(generation + id);
        }
        return results;
    }

    @L2InvalidateMultiCache(namespace = L2_CACHE,
            keyIndex = -1,
            keyPrefix = L2_PREFIX,
            keyTemplate = TMPL_START + "$retVal[$index]-$indexObject"
    )
    public List<Long> invalidateL2MultiCharlie(final List<Long> ids) {
        return ids;
    }

    @L2ReadThroughSingleCache(namespace = L2_CACHE,
            keyPrefix = L2_PREFIX,
            keyTemplate = TMPL_START + "$args[0]-$args[0]",
            window = Duration.FIVE_MINUTES
    )
    public String getL2SingleDelta(final Long id, final String generation) {
        return generation + id;
    }

    @L2UpdateSingleCache(namespace = L2_CACHE,
            keyPrefix = L2_PREFIX,
            keyTemplate = TMPL_START + "$args[0]-$args[0]",
            dataIndex = -1,
            window = Duration.FIVE_MINUTES
    )
    public String getL2SingleEcho(final Long id, final String generation) {
        return generation + id;
    }

    @L2InvalidateSingleCache(namespace = L2_CACHE,
            keyPrefix = L2_PREFIX,
            keyTemplate = TMPL_START + "$!{retVal}-$args[0]" // Had to use special delimiting because LONG-XXX looks like math...
    )
    public Long invalidateL2SingleFoxtrot(final Long id) {
        return id;
    }

    @L2ReadThroughAssignCache(namespace = L2_CACHE,
            assignedKey = L2_ASSIGN,
            window = Duration.FIVE_MINUTES
    )
    public String getL2AssignGolf(final Long id, final String generation) {
        return generation + id;
    }

    @L2UpdateAssignCache(namespace = L2_CACHE,
            assignedKey = L2_ASSIGN,
            dataIndex = -1,
            window = Duration.FIVE_MINUTES
    )
    public String getL2AssignHotel(final Long id, final String generation) {
        return generation + id;
    }

    @L2InvalidateAssignCache(namespace = L2_CACHE,
            assignedKey = L2_ASSIGN
    )
    public Long invalidateL2AssignIndia(final Long id) {
        return id;
    }

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/
    /** *                  Combined L1 & L2 cache methods.                              * **/
    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/

    private static final String COMBINE_CACHE = "CombiL1L2";
    private static final String COMBINE_PREFIX = "both-";

    @L2ReadThroughSingleCache(namespace = COMBINE_CACHE, keyPrefix = COMBINE_PREFIX, keyIndex = 0, window = Duration.FIVE_SECONDS)
    @ReadThroughSingleCache(namespace = COMBINE_CACHE, keyPrefix = COMBINE_PREFIX, keyIndex = 0, expiration = 30)
    public Long getCombinedData(String key) {
        return 1000L + RandomUtils.nextInt(8999);
    }

    @ReadThroughSingleCache(namespace = COMBINE_CACHE, keyPrefix = COMBINE_PREFIX, keyIndex = 0, expiration = 30)
    public Long getL1Data(String key) {
        return 10000L + RandomUtils.nextInt(89999);
    }

    @UpdateSingleCache(namespace = COMBINE_CACHE, keyPrefix = COMBINE_PREFIX, keyIndex = 0, dataIndex = -1, expiration = 30)
    public Long updateL1Data(String key) {
        return 100000L + RandomUtils.nextInt(899999);
    }
}
