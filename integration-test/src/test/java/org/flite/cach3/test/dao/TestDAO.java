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

import java.util.*;

public interface TestDAO {

	public String getDateString(final String key);

    public void overrideDateString(final int trash, final String key, final String overrideData);

    public List<String> getTimestampValues(final List<Long> keys);

	public String updateTimestampValue(final Long key);

	public List<String> updateTimestamValues(final List<Long> keys);

    public void overrideTimestampValues(final int trash, final List<Long> keys,
                                        final String nuthin, final List<String> overrideData);

    public String getRandomString(final Long key);

    public void updateRandomString(final Long key);

    public Long updateRandomStringAgain(final Long key);

    public List<String> getRandomStrings(final List<Long> keys);

    public void updateRandomStrings(final List<Long> keys);

    public List<Long> updateRandomStringsAgain(final List<Long> keys);

    public List<String> getAssignStrings();

    public void invalidateAssignStrings();

    public void updateAssignStrings(int bubpkus, final List<String> newData);

    public String getDwarf(final Long id);

    public List<String> getDwarves(final List<Long> ids);

    public void invalidateDwarf(final Long id);

    public void invalidateDwarves(final List<Long> id);

    public String updateDwarf(final Long id);

    public List<String> updateDwarves(final List<Long> ids);

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/
    /** *                  Methods using the velocity templating option.                * **/
    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/

    public String getCompoundString(final Long first, final String toReturn, final Long second);

    public List<String> getCompoundStrings(final List<Long> first, final String toReturn, final Long second);

    public String updateCompoundString(final Long second, final String toReturn, final Long first);

    public List<String> updateCompoundStrings(final Long second, final String toReturn, final List<Long> first);

    public void invalidateCompoundString(final Long second, final Long first);

    public void invalidateCompoundStrings(final Long second, final List<Long> first);


    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/
    /** *                  Mulitple cache methods.                                      * **/
    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/

    void setFunkFactor(Long number, Long funkFactor);
    void undercoverSetFunkFactor(Long number, Long funkFactor);

    Long funkySquare(Long number);
    Long funkyCube(Long number);

    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/
    /** *                  L2 Multi cache methods.                                      * **/
    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/

    public List<String> getL2MultiAlpha(List<Long> ids, String generation);
    public List<String> getL2MultiBeta(List<Long> ids, String generation);
    public List<Long> invalidateL2MultiCharlie(List<Long> ids);
    public String getL2SingleDelta(Long id, String generation);
    public String getL2SingleEcho(Long id, String generation);

}
