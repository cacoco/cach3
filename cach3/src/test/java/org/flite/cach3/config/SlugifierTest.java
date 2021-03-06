package org.flite.cach3.config;

import org.testng.annotations.Test;

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
public class SlugifierTest {

    @Test
    public void testSlugify() throws Exception {
        assertEquals("foobar", Slugifier.slugify("f\u00F3\u00F2b\u00e2r")); // Looks like "fóòbâr"
        assertEquals("special-characters-sic", Slugifier.slugify("Special Characters \u0161\u00ed\u010d")); // Looks like "Special Characters šíč"
        assertEquals("it-rocks", Slugifier.slugify("It Rocks"));
    }

    @Test
    public void testHash() throws Exception {
        assertEquals("", Slugifier.hash(null));
        assertEquals("", Slugifier.hash(""));
        assertEquals(32, Slugifier.hash("Some string value that can be hashed").length());
    }
}
