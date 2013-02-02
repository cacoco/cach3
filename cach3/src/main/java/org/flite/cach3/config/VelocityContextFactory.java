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

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VelocityContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(VelocityContextFactory.class);

    /*default*/ static final String ARGS = "args";
    /*default*/ static final String RETVAL = "retVal";
    /*default*/ static final String INDEX = "index";
    /*default*/ static final String INDEXOBJECT = "indexObject";
    private static final List<String> SYSTEM_ITEMS = Arrays.asList(ARGS.toLowerCase(),
            RETVAL.toLowerCase(),
            INDEX.toLowerCase(),
            INDEXOBJECT.toLowerCase());

    private VelocityContext baseContext;

    public VelocityContextFactory() {
        baseContext = new VelocityContext();

        baseContext.put("StringUtils", StringUtils.class);
        baseContext.put("Slugifier", Slugifier.class);
    }

    public Object addVelocityContextItems(final Map<String, Object> items) {
        final Object result = null;
        if (items == null || items.size() == 0) { return result; }

        for (final Map.Entry<String, Object> entry : items.entrySet()) {
            if (entry == null) { continue; }
            final String key = entry.getKey();
            final Object value = entry.getValue();
            try {
                if (StringUtils.isBlank(key)) { throw new InvalidParameterException(String.format("Invalid VelocityContext item name: [%s]", key)); }
                if (SYSTEM_ITEMS.contains(key.toLowerCase())) { throw new InvalidParameterException(String.format("VelocityContext item name reserved for Cach3 system usage: [%s]", key)); }
                if (value == null) { throw new InvalidParameterException("Null items disallowed from being added to the VelocityContext"); }

                baseContext.put(key, value);
            } catch (Exception ex) {
                LOG.warn("Problem adding an item to the VelocityContext", ex);
            }
        }
        return result;
    }

    public VelocityContext getNewExtendedContext() {
        return new VelocityContext(baseContext);
    }

}
