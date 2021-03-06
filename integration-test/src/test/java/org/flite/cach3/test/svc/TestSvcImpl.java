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

package org.flite.cach3.test.svc;

import org.flite.cach3.test.dao.*;
import org.flite.cach3.test.model.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service("testSvc")
public class TestSvcImpl implements TestSvc {

	private TestDAO dao;

	public void setDao(TestDAO dao) {
		this.dao = dao;
	}

	public String getDateString(final String key) {
		return this.dao.getDateString(key);
	}

    public void overrideDateString(final int trash, final String key, final String overrideData) {
        dao.overrideDateString(trash, key, overrideData);
    }

	public List<String> getTimestampValues(final List<Long> keys) {
		return this.dao.getTimestampValues(keys);
	}

	public String updateTimestampValue(final Long key) {
		return this.dao.updateTimestampValue(key);
	}

	public List<String> updateTimestamValues(final List<Long> keys) {
		return this.dao.updateTimestamValues(keys);
	}

    public void overrideTimestampValues(final int trash, final List<Long> keys,
                                        final String nuthin, final List<String> overrideData) {
        dao.overrideTimestampValues(trash, keys, nuthin, overrideData);
    }

    public String getRandomString(final Long key) {
        return this.dao.getRandomString(key);
    }

    public void updateRandomString(final Long key) {
        this.dao.updateRandomString(key);
    }

    public Long updateRandomStringAgain(final Long key) {
        return this.dao.updateRandomStringAgain(key);
    }

    public List<String> getRandomStrings(List<Long> keys) {
        return this.dao.getRandomStrings(keys);
    }

    public void updateRandomStrings(List<Long> keys) {
        this.dao.updateRandomStrings(keys);
    }

    public List<Long> updateRandomStringsAgain(List<Long> keys) {
        return this.dao.updateRandomStringsAgain(keys);
    }

    public List<String> getAssignStrings() {
        return this.dao.getAssignStrings();
    }

    public void invalidateAssignStrings() {
        this.dao.invalidateAssignStrings();
    }

    public void updateAssignStrings(final List<String> newData) {
        this.dao.updateAssignStrings(25, newData);
    }

    public String getDwarf(Long id) {
        return this.dao.getDwarf(id);
    }

    public List<String> getDwarves(List<Long> ids) {
        return this.dao.getDwarves(ids);
    }

    public void invalidateDwarf(Long id) {
        this.dao.invalidateDwarf(id);
    }

    public void invalidateDwarves(List<Long> ids) {
        this.dao.invalidateDwarves(ids);
    }

    public String updateDwarf(Long id) {
        return this.dao.updateDwarf(id);
    }

    public List<String> updateDwarves(List<Long> ids) {
        return this.dao.updateDwarves(ids);
    }

    public String getCompoundString(final Long first, final String toReturn, final Long second) {
        return this.dao.getCompoundString(first, toReturn, second);
    }

    public List<String> getCompoundStrings(final List<Long> first, final String toReturn, final Long second) {
        return this.dao.getCompoundStrings(first, toReturn, second);
    }

    public String updateCompoundString(final Long second, final String toReturn, final Long first) {
        return this.dao.updateCompoundString(second, toReturn, first);
    }

    public List<String> updateCompoundStrings(final Long second, final String toReturn, final List<Long> first) {
        return this.dao.updateCompoundStrings(second, toReturn, first);
    }

    public void invalidateCompoundString(final Long second, final Long first) {
        this.dao.invalidateCompoundString(second, first);
    }

    public void invalidateCompoundStrings(final Long second, final List<Long> first) {
        this.dao.invalidateCompoundStrings(second, first);
    }


    public void setFunkFactor(long number, long funkFactor) {
        this.dao.setFunkFactor(number, funkFactor);
    }

    public void undercoverSetFunkFactor(long number, long funkFactor) {
        this.dao.undercoverSetFunkFactor(number, funkFactor);
    }

    public long funkySquare(long number) {
        return this.dao.funkySquare(number);
    }

    public long funkyCube(long number) {
        return this.dao.funkyCube(number);
    }

    public List<String> getL2MultiAlpha(List<Long> ids, String generation) {
        return this.dao.getL2MultiAlpha(ids, generation);
    }

    public List<String> getL2MultiBeta(List<Long> ids, String generation) {
        return this.dao.getL2MultiBeta(ids, generation);
    }

    public List<Long> invalidateL2MultiCharlie(List<Long> ids) {
        return this.dao.invalidateL2MultiCharlie(ids);
    }

    public String getL2SingleDelta(Long id, String generation) {
        return this.dao.getL2SingleDelta(id, generation);
    }

    public String getL2SingleEcho(Long id, String generation) {
        return this.dao.getL2SingleEcho(id, generation);
    }

    public Long invalidateL2SingleFoxtrot(Long id) {
        return this.dao.invalidateL2SingleFoxtrot(id);
    }

    public String getL2AssignGolf(Long id, String generation) {
        return this.dao.getL2AssignGolf(id, generation);
    }

    public String getL2AssignHotel(Long id, String generation) {
        return this.dao.getL2AssignHotel(id, generation);
    }

    public Long invalidateL2AssignIndia(Long id) {
        return this.dao.invalidateL2AssignIndia(id);
    }

    public Long getCombinedData(String key) {
        return this.dao.getCombinedData(key);
    }

    public Long getL1Data(String key) {
        return this.dao.getL1Data(key);
    }

    public Long updateL1Data(String key) {
        return this.dao.updateL1Data(key);
    }

    public List<Example> getExampleObjects(List<Long> ids, String gen) {
        return this.dao.getExampleObjects(ids, gen);
    }
}
