package org.flite.cach3.config;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.testng.annotations.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.*;

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
public class VelocityContextFactoryTest {

    private static final String VALUE = RandomStringUtils.randomAlphanumeric(10);

    @Test
    public void testAddition() {
        final VelocityContextFactory factory = new VelocityContextFactory();

        final Map<String, Object> items = new HashMap<String, Object>();
        items.put("testing", this.getClass());
        factory.addVelocityContextItems(items);

        final StringWriter sw = new StringWriter();
        final VelocityContext ctx = factory.getNewExtendedContext();
        Velocity.evaluate(ctx, sw, "", "bubba-$testing.getValue()");

        assertEquals("bubba-" + VALUE, sw.toString());
    }

    public static String getValue() {
        return VALUE;
    }

    @Test
    public void testCreation() {
        final VelocityContextFactory factory = new VelocityContextFactory();

        final String value = RandomStringUtils.randomAlphanumeric(9);

        final Map<String, Object> items = new HashMap<String, Object>();
        items.put(VelocityContextFactory.ARGS, "should not arrive");
        items.put(VelocityContextFactory.RETVAL.toUpperCase(), "ignored");
        items.put("testing", value);
        factory.addVelocityContextItems(items);

        final VelocityContext ctx = factory.getNewExtendedContext();

        assertNull(ctx.get(VelocityContextFactory.ARGS));
        assertNull(ctx.get(VelocityContextFactory.ARGS.toLowerCase()));
        assertNull(ctx.get(VelocityContextFactory.ARGS.toUpperCase()));
        assertNull(ctx.get(VelocityContextFactory.RETVAL));
        assertNull(ctx.get(VelocityContextFactory.RETVAL.toLowerCase()));
        assertNull(ctx.get(VelocityContextFactory.RETVAL.toUpperCase()));

        assertEquals(value, ctx.get("testing"));

        // Make sure we get distinct Contexts for each getNew...(...)
        assertFalse(factory.getNewExtendedContext() == ctx);
    }

}
