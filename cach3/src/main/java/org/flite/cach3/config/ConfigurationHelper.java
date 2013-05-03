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

package org.flite.cach3.config;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.*;

import java.security.*;

public class ConfigurationHelper {
    public static Boolean setCacheDisabled(final Cach3State state, final boolean disabled) {
        if (state == null) { throw new InvalidParameterException("Cach3State must be defined."); }
        state.setCacheDisabled(disabled);
        return disabled;
    }

    public static Integer setJitterDefault(final Cach3State state, final int jitterDefault) {
        if (state == null) { throw new InvalidParameterException("Cach3State must be defined."); }
        state.setJitterDefault(jitterDefault);
        return jitterDefault;
    }

    public static String addVelocityContextItems(final VelocityContextFactory factory, final String key, final Object value) {
        if (factory == null) { throw new InvalidParameterException("VelocityContextFactory must be defined."); }
        factory.addVelocityContextItems(ImmutableMap.of(key, value));
        return key;
    }

    public static Boolean setL2CacheDisabled(final L2Cach3State state, final boolean disabled) {
        if (state == null) { throw new InvalidParameterException("L2Cach3State must be defined."); }
        state.setCacheDisabled(disabled);
        return disabled;
    }

}
