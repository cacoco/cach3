package org.flite.cach3.aop;

import org.apache.commons.lang.*;
import org.flite.cach3.annotations.*;
import org.flite.cach3.api.*;
import org.flite.cach3.config.*;
import org.flite.cach3.exceptions.*;
import org.flite.cach3.test.listeners.StubInvalidateAssignCacheListenerImpl;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

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
public class CacheBaseTest {
	private CacheBase cut;

	@BeforeClass
	public void beforeClass() {
		cut = new CacheBase();

		cut.setMethodStore(new CacheKeyMethodStoreImpl());
	}

	@Test
	public void testKeyMethodArgs() throws Exception {
		try {
			cut.getKeyMethod(new KeyObject01());
			fail("Expected exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("0 arguments") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.getKeyMethod(new KeyObject02());
			fail("Expected exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("String") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.getKeyMethod(new KeyObject03());
			fail("Expected exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("String") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.getKeyMethod(new KeyObject04());
			fail("Expected exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("only one method") != -1);
			System.out.println(ex.getMessage());
		}

		assertEquals("doIt", cut.getKeyMethod(new KeyObject05()).getName());
		assertEquals("toString", cut.getKeyMethod(new KeyObject06(null)).getName());
	}

	@Test
	public void testBuildCacheKey() {
		try {
			cut.buildCacheKey(null, (AnnotationData) null);
			fail("Expected exception.");
		} catch (InvalidParameterException ex) {
			assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
			System.out.println(ex.getMessage());
		}

		try {
			cut.buildCacheKey("", (AnnotationData) null);
			fail("Expected exception.");
		} catch (InvalidParameterException ex) {
			assertTrue(ex.getMessage().indexOf("at least 1 character") != -1);
			System.out.println(ex.getMessage());
		}

		final String objectId = RandomStringUtils.randomAlphanumeric(20);
        final AnnotationData annotationData = new AnnotationData();
        final String namespace = RandomStringUtils.randomAlphanumeric(12);
        annotationData.setNamespace(namespace);

		final String result = cut.buildCacheKey(objectId, annotationData);

		assertTrue(result.indexOf(objectId) != -1);
		assertTrue(result.indexOf(namespace) != -1);
	}

	@Test
	public void testGenerateCacheKey() throws Exception {
		final Method method = KeyObject.class.getMethod("toString", null);

		try {
			cut.generateObjectId(method, new KeyObject(null));
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("empty key value") != -1);
		}

		try {
			cut.generateObjectId(method, new KeyObject(""));
			fail("Expected Exception.");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("empty key value") != -1);
		}

		final String result = "momma";
		assertEquals(result, cut.generateObjectId(method, new KeyObject(result)));
	}

	@Test
	public void testReturnTypeChecking() throws Exception {
		Method method = null;

		method = ReturnTypeCheck.class.getMethod("checkA", null);
		cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);

		method = ReturnTypeCheck.class.getMethod("checkB", null);
		cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);

		method = ReturnTypeCheck.class.getMethod("checkC", null);
		cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);

		method = ReturnTypeCheck.class.getMethod("checkD", null);
		cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);

		try {
			method = ReturnTypeCheck.class.getMethod("checkE", null);
			cut.verifyReturnTypeIsList(method, CacheKeyMethod.class);
			fail("Expected Exception.");
		} catch (InvalidAnnotationException ex) {
			assertTrue(ex.getMessage().indexOf("requirement") != -1);
		}
	}

    @Test
    public void testGetPertinentListeners() throws Exception {
        final Cach3State state = new Cach3State();
        cut.setState(state);

        final StubInvalidateAssignCacheListenerImpl bubba = new StubInvalidateAssignCacheListenerImpl();
        bubba.setInterests(new HashSet<String>());
        bubba.getInterests().add("buford");
        bubba.getInterests().add("blue");
        bubba.getInterests().add("shrimp");
        state.addListener(bubba);

        final StubInvalidateAssignCacheListenerImpl forest = new StubInvalidateAssignCacheListenerImpl();
        forest.setInterests(new HashSet<String>());
        forest.getInterests().add("gump");
        forest.getInterests().add("shrimp");
        state.addListener(forest);

        final StubInvalidateAssignCacheListenerImpl nosey1 = new StubInvalidateAssignCacheListenerImpl();
        nosey1.setInterests(new HashSet<String>());
        state.addListener(nosey1);

        final StubInvalidateAssignCacheListenerImpl nosey2 = new StubInvalidateAssignCacheListenerImpl();
        state.addListener(nosey2);

        // Check for valid parameters
        try {
            cut.getPertinentListeners(null, "bubba");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }

        try {
            cut.getPertinentListeners(CacheListener.class, null);
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }

        try {
            cut.getPertinentListeners(CacheListener.class, "");
            fail("Expected Exception.");
        } catch (InvalidParameterException ex) { }

        // Now check actual execution.
        final List<InvalidateAssignCacheListener> r1 = cut.getPertinentListeners(InvalidateAssignCacheListener.class, "yankee");
        assertFalse(r1.contains(bubba));
        assertFalse(r1.contains(forest));
        assertTrue(r1.contains(nosey1));
        assertTrue(r1.contains(nosey2));

        final List<InvalidateAssignCacheListener> r2 = cut.getPertinentListeners(InvalidateAssignCacheListener.class, "buford");
        assertTrue(r2.contains(bubba));
        assertFalse(r2.contains(forest));
        assertTrue(r2.contains(nosey1));
        assertTrue(r2.contains(nosey2));

        final List<InvalidateAssignCacheListener> r3 = cut.getPertinentListeners(InvalidateAssignCacheListener.class, "shrimp");
        assertTrue(r3.contains(bubba));
        assertTrue(r3.contains(forest));
        assertTrue(r3.contains(nosey1));
        assertTrue(r3.contains(nosey2));

        final List<UpdateMultiCacheListener> r4 = cut.getPertinentListeners(UpdateMultiCacheListener.class, "shrimp");
        assertTrue(r4.isEmpty());
    }

	private static class ReturnTypeCheck {
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public List checkA() {return null;}
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public List<String> checkB() {return null;}
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public ArrayList checkC() {return null;}
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public ArrayList<String> checkD() {return null;}
		@ReadThroughMultiCache(keyIndex = 0, namespace = "bubba", expiration = 10)
		public String checkE() {return null;}
	}

	private static class KeyObject {
		private String result;
		private KeyObject(String result) { this.result = result;}
		public String toString() { return result; }
	}

	private static class KeyObject01 {
		@CacheKeyMethod
		public void doIt(final String nonsense) { }
	}

	private static class KeyObject02 {
		@CacheKeyMethod
		public void doIt() { }
	}

	private static class KeyObject03 {
		@CacheKeyMethod
		public Long doIt() { return null; }
	}

	private static class KeyObject04 {
		@CacheKeyMethod
		public String doIt() { return null; }
		@CacheKeyMethod
		public String doItAgain() { return null; }
	}

	private static class KeyObject05 {
		public static final String result = "shrimp";
		@CacheKeyMethod
		public String doIt() { return result; }
	}

	private static class KeyObject06 {
		private String result;
		private KeyObject06(String result) { this.result = result;}
		public String toString() { return result; }
	}

}
