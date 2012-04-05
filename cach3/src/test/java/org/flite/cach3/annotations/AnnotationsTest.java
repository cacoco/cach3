package org.flite.cach3.annotations;

import org.apache.velocity.*;
import org.apache.velocity.app.*;
import org.flite.cach3.config.*;
import org.testng.annotations.*;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;

import static org.testng.AssertJUnit.*;

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
public class AnnotationsTest {

    @Test
    public void testVelocity() {
        final String result = "This Is A Big Fat Result.";
        final Object[] args = new Object[] {null, "key 2", Long.valueOf(1337)};
        final VelocityContext context = new VelocityContextFactory().getNewExtendedContext();
        context.put("result", result);
        context.put("args", args);

        final StringWriter writer = new StringWriter(250);

        Velocity.evaluate(context, writer, this.getClass().getSimpleName(), "$result&$StringUtils.defaultString($args[0])&$args[2]");

        assertEquals("This Is A Big Fat Result.&&1337", writer.toString());
    }

	@Test
	public void testIndividual() throws Exception {
		final Method method = RandomClass.class.getMethod("getName", null);
		final Annotation[] annotations = method.getDeclaredAnnotations();
		assertEquals(ReadThroughSingleCache.class, annotations[0].annotationType());
		final ReadThroughSingleCache ind = (ReadThroughSingleCache) annotations[0];
		assertEquals("polk", ind.namespace());
		assertEquals(5, ind.keyIndex());
	}

	private static class RandomClass {
		private String name = "RandomClass";

		@ReadThroughSingleCache(namespace="polk", keyIndex = 5)
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
