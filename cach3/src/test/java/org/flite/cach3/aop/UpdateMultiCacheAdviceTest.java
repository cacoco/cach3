package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.easymock.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.flite.cach3.config.*;
import org.flite.cach3.exceptions.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.util.*;

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
public class UpdateMultiCacheAdviceTest {

	private UpdateMultiCacheAdvice cut;
    private Cach3State state;

	@BeforeClass
	public void beforeClass() {
		cut = new UpdateMultiCacheAdvice();
		cut.setMethodStore(new CacheKeyMethodStoreImpl());
        cut.updateMulti();

        state = new Cach3State();
        cut.setState(state);
    }

	@Test
	public void testGetKeyObjects() throws Exception {
		final Method method1 = AnnotationTest.class.getMethod("cacheMe02", null);
		try {
			cut.getKeyObjects(-1, "bubba", null, method1);
		} catch (InvalidAnnotationException ex) {
			assertTrue(true);
		}

		final Method method2 = AnnotationTest.class.getMethod("cacheMe03", null);
		final ArrayList<String> results = new ArrayList<String>();
		results.add("gump");
		cut.getKeyObjects(-1, results, null, method2);
	}

    @Test
    public void testUpdateCache() throws Exception {
        final Method method = AnnotationTest.class.getMethod("cacheMe01", null);
        final UpdateMultiCache annotation = method.getAnnotation(UpdateMultiCache.class);
        final AnnotationInfo info = UpdateMultiCacheAdvice.getAnnotationInfo(annotation, "cacheMe01", 0);
        final MemcachedClientIF cache = EasyMock.createMock(MemcachedClientIF.class);
        state.setProvider(new MemcachedClientProvider() {
            public MemcachedClientIF getMemcachedClient() {
                return cache;
            }

            public void refreshConnection() { }
        });

        final List<String> keys = new ArrayList<String>();
        final List<Object> objs = new ArrayList<Object>();
        keys.add("Key1-" + System.currentTimeMillis());
        keys.add("Key2-" + System.currentTimeMillis());

        try {
            cut.updateCache(keys, objs, method, 0, 0, cache, String.class);
            fail("Expected Exception.");
        } catch (InvalidAnnotationException ex) {
            assertTrue(ex.getMessage().contains("do not match in size"));
        }


        for (final String key : keys) {
            final String value = "ValueFor-" + key;
            objs.add(value);
            EasyMock.expect(cache.set(key, info.getAsInteger(AType.EXPIRATION), value)).andReturn(null);
        }
        keys.add("BigFatNull");
        objs.add(null);
        EasyMock.expect(cache.set(keys.get(2), info.getAsInteger(AType.EXPIRATION), new PertinentNegativeNull())).andReturn(null);

        EasyMock.replay(cache);

        cut.updateCache(keys, objs, method, info.getAsInteger(AType.JITTER), info.getAsInteger(AType.EXPIRATION), cache, String.class);

        EasyMock.verify(cache);
    }

    static class AnnotationTest {
		@UpdateMultiCache(namespace = "Bubba", expiration = (60*60*24*30 + 1000), keyIndex = 0, dataIndex = 0)
		public void cacheMe01() {}

		public String cacheMe02() { return null; }

		public ArrayList<String> cacheMe03() { return null; }
	}
}
