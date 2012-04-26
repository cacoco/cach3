package org.flite.cach3.config;

import java.io.*;
import java.net.*;
import java.text.*;

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

 Original source: http://maddemcode.com/java/seo-friendly-urls-using-slugify-in-java/
 Originally under a WTFPL: http://en.wikipedia.org/wiki/WTFPL

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>

Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.
 */
public class Slugifier {

    public static String slugify(final String input) {
        try {
            if (input == null || input.length() == 0) { return ""; }
            String toReturn = normalize(input);
            toReturn = toReturn.replace(" ", "-");
            toReturn = toReturn.toLowerCase();
            toReturn = URLEncoder.encode(toReturn, "UTF-8");
            return toReturn;
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("Problem slugifying the string: " + input, ex);
        }
    }

    private static String normalize(String input) {
        if (input == null || input.length() == 0) return "";
        return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","");
    }
}
