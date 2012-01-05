package org.flite.cach3.aop;

import net.spy.memcached.*;
import org.apache.commons.lang.*;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.flite.cach3.config.*;
import org.testng.annotations.*;

import static org.easymock.EasyMock.*;
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
public class ReadThroughSingleCacheMockTest {

	private ReadThroughSingleCacheAdvice cut;
    private Cach3State state;
	private ProceedingJoinPoint pjp;
	private MemcachedClientIF cache;
	private MethodSignature sig;

	@BeforeClass
	public void beforeClass() {
		cut = new ReadThroughSingleCacheAdvice();

		pjp = createMock(ProceedingJoinPoint.class);
		cache = createMock(MemcachedClientIF.class);
		sig = createMock(MethodSignature.class);

        state = new Cach3State();
        cut.setState(state);
		cut.setMethodStore(new CacheKeyMethodStoreImpl());

        cut.setFactory(new VelocityContextFactory());
        
        state.setProvider(new MemcachedClientProvider() {
            public MemcachedClientIF getMemcachedClient() {
                return cache;
            }
        });
	}

	@BeforeMethod
	public void beforeMethod() {
		reset(pjp);
		reset(cache);
		reset(sig);
	}

	public void replayAll() {
		replay(pjp);
		replay(cache);
		replay(sig);
	}

	public void verifyAll() {
		verify(pjp);
		verify(cache);
		verify(sig);
	}

	@Test
	public void testTopLevelCacheIndividualCacheHit() throws Throwable {
		final String methodName = "cacheThis";
		expect(pjp.getSignature()).andReturn(sig);
		expect(sig.getName()).andReturn(methodName);
		expect(sig.getParameterTypes()).andReturn(new Class[] {AOPKeyClass.class});
		expect(pjp.getTarget()).andReturn(new AOPTargetClass2());
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
		final String cachedResult = "A VALUE FROM THE CACHE";
		expect(cache.get("BUBBA:" + AOPKeyClass.result)).andReturn(cachedResult);

		replayAll();

		final String result = (String) cut.cacheSingle(pjp);

		verifyAll();
		assertEquals(cachedResult, result);
	}

	@Test
	public void testTopLevelCacheIndividualCacheHitNull() throws Throwable {
		final String methodName = "cacheThis";
		expect(pjp.getSignature()).andReturn(sig);
		expect(sig.getName()).andReturn(methodName);
		expect(sig.getParameterTypes()).andReturn(new Class[] {AOPKeyClass.class});
		expect(pjp.getTarget()).andReturn(new AOPTargetClass2());
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
		expect(cache.get("BUBBA:" + AOPKeyClass.result)).andReturn(new PertinentNegativeNull());

		replayAll();

		final String result = (String) cut.cacheSingle(pjp);

		verifyAll();
		assertNull(result);
	}

	@Test
	public void testTopLevelCacheIndividualCachePreException() throws Throwable {
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
        expect(pjp.getArgs()).andReturn(new Object[]{}).once();
		expect(pjp.getSignature()).andThrow(new RuntimeException("FORCE FOR TEST"));
		final String targetResult = "A VALUE FROM THE TARGET OBJECT";
		expect(pjp.proceed()).andReturn(targetResult);

		replayAll();

		final String result = (String) cut.cacheSingle(pjp);

		verifyAll();
		assertEquals(targetResult, result);
	}

	@Test
	public void testTopLevelCacheIndividualCacheMissWithData() throws Throwable {
		final String methodName = "cacheThis";
		expect(pjp.getSignature()).andReturn(sig);
		expect(sig.getName()).andReturn(methodName);
		expect(sig.getParameterTypes()).andReturn(new Class[] {AOPKeyClass.class});
		expect(pjp.getTarget()).andReturn(new AOPTargetClass2());
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
		final String cacheKey = "BUBBA:" + AOPKeyClass.result;
		final String targetResult = "A VALUE FROM THE CACHE";
		expect(cache.get(cacheKey)).andReturn(null);
		expect(pjp.proceed()).andReturn(targetResult);
		expect(cache.set(cacheKey, 3600, targetResult)).andReturn(null);

		replayAll();

		final String result = (String) cut.cacheSingle(pjp);

		verifyAll();
		assertEquals(targetResult, result);
	}

	@Test
	public void testTopLevelCacheIndividualCacheMissWithNull() throws Throwable {
		final String methodName = "cacheThis";
		expect(pjp.getSignature()).andReturn(sig);
		expect(sig.getName()).andReturn(methodName);
		expect(sig.getParameterTypes()).andReturn(new Class[] {AOPKeyClass.class});
		expect(pjp.getTarget()).andReturn(new AOPTargetClass2());
		expect(pjp.getArgs()).andReturn(new Object[] {new AOPKeyClass()});
		expect(pjp.toShortString()).andReturn("SHORTSTRING").anyTimes();
		final String cacheKey = "BUBBA:" + AOPKeyClass.result;
		expect(cache.get(cacheKey)).andReturn(null);
		expect(pjp.proceed()).andReturn(null);
		expect(cache.set(cacheKey, 3600, new PertinentNegativeNull())).andReturn(null);

		replayAll();

		final String result = (String) cut.cacheSingle(pjp);

		verifyAll();
		assertNull(result);
	}

    @Test
    public void testNonVelocityBaseKey() throws Exception {
        final AnnotationData data = new AnnotationData();
        data.setKeyIndex(3);
        final String key = RandomStringUtils.randomAlphanumeric(8);

        final String result = cut.generateBaseKeySingle(new Object[]{"alpha", "beta", "gamma", key}, data, "fakeMethodName()");

        assertEquals(key, result);
    }

    @Test
    public void testVelocityBaseKey() throws Exception {
        final String arbitrary = RandomStringUtils.randomAlphanumeric(10);
        final String template = "$args[0]-" + arbitrary + "-$args[3]";
        final String alpha = RandomStringUtils.randomAlphabetic(7);
        final String delta = RandomStringUtils.randomAlphanumeric(11);
        final String expected = alpha + "-" + arbitrary + "-" + delta;

        final AnnotationData data = new AnnotationData();
        data.setKeyTemplate(template);

        final String result = cut.generateBaseKeySingle(new Object[]{alpha, "beta", "gamma", delta}, data, "fakeMethodName()");

        assertEquals(expected, result);
    }

	private static class AOPTargetClass1 {
		public String doIt(final String s1, final String s2, final String s3) { return null; }
	}

	private static class AOPTargetClass2 {
		@ReadThroughSingleCache(namespace = "BUBBA", keyIndex = 0, expiration = 3600)
		public String cacheThis(final AOPKeyClass p1) {
			throw new RuntimeException("Forced.");
		}
	}

	private static class AOPKeyClass {
		public static final String result = "CACHE_KEY";
		@CacheKeyMethod
		public String getKey() {
			return result;
		}
	}
}
