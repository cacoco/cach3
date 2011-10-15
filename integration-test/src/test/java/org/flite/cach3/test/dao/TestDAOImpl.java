package org.flite.cach3.test.dao;

import org.apache.commons.lang.*;
import org.apache.commons.lang.math.*;
import org.flite.cach3.annotations.*;

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
public class TestDAOImpl implements TestDAO {

    public static final String DATE_NAMESPACE = "Alpha";
    public static final String TIME_NAMESPACE = "Bravo";
    public static final String SINGLE_NAMESPACE = "Charlie";
    public static final String MULTI_NAMESPACE = "Delta";
    public static final String ASSIGN_NAMESPACE = "Echo";
    public static final String ASSIGN_KEY = "SomePhatKey";

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
		final Long now = new Date().getTime();
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
		final Long now = new Date().getTime();
		final String result = now.toString() + "-U-" + key.toString();
		return result;
	}

	@UpdateMultiCache(namespace = TIME_NAMESPACE, keyIndex = 0, expiration = 300, dataIndex = -1)
	public List<String> updateTimestamValues(final List<Long> keys) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {}
		final Long now = new Date().getTime();
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
}
