package org.flite.cach3.aop;

import org.apache.commons.lang.*;
import org.aspectj.lang.*;
import org.easymock.*;
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
public class DisabledCacheTest extends EasyMockSupport {
    @Test
    public void testDisabled() throws Throwable {
        final Cach3State state = new Cach3State();
        state.setCacheDisabled(true);
        int length = 5;

        // Invalidates
        final InvalidateAssignCacheAdvice a1 = new InvalidateAssignCacheAdvice();
        a1.setState(state);
        final ProceedingJoinPoint pjp1 = createMock(ProceedingJoinPoint.class);
        final String r1 = RandomStringUtils.randomAlphanumeric(length++);

        final InvalidateMultiCacheAdvice a2 = new InvalidateMultiCacheAdvice();
        a2.setState(state);
        final JoinPoint pjp2 = createMock(ProceedingJoinPoint.class);
        final String r2 = RandomStringUtils.randomAlphanumeric(length++);

        final InvalidateSingleCacheAdvice a3 = new InvalidateSingleCacheAdvice();
        a3.setState(state);
        final ProceedingJoinPoint pjp3 = createMock(ProceedingJoinPoint.class);
        final String r3 = RandomStringUtils.randomAlphanumeric(length++);

        // ReadThroughs
        final ReadThroughAssignCacheAdvice a4 = new ReadThroughAssignCacheAdvice();
        a4.setState(state);
        final ProceedingJoinPoint pjp4 = createMock(ProceedingJoinPoint.class);
        final String r4 = RandomStringUtils.randomAlphanumeric(length++);
        expect(pjp4.proceed()).andReturn(r4);

        final ReadThroughMultiCacheAdvice a5 = new ReadThroughMultiCacheAdvice();
        a5.setState(state);
        final ProceedingJoinPoint pjp5 = createMock(ProceedingJoinPoint.class);
        final String r5 = RandomStringUtils.randomAlphanumeric(length++);
        expect(pjp5.proceed()).andReturn(r5);

        final ReadThroughSingleCacheAdvice a6 = new ReadThroughSingleCacheAdvice();
        a6.setState(state);
        final ProceedingJoinPoint pjp6 = createMock(ProceedingJoinPoint.class);
        final String r6 = RandomStringUtils.randomAlphanumeric(length++);
        expect(pjp6.proceed()).andReturn(r6);

        // Updates
        final UpdateAssignCacheAdvice a7 = new UpdateAssignCacheAdvice();
        a7.setState(state);
        final JoinPoint pjp7 = createMock(JoinPoint.class);
        final String r7 = RandomStringUtils.randomAlphanumeric(length++);

        final UpdateMultiCacheAdvice a8 = new UpdateMultiCacheAdvice();
        a8.setState(state);
        final JoinPoint pjp8 = createMock(JoinPoint.class);
        final String r8 = RandomStringUtils.randomAlphanumeric(length++);

        final UpdateSingleCacheAdvice a9 = new UpdateSingleCacheAdvice();
        a9.setState(state);
        final JoinPoint pjp9 = createMock(JoinPoint.class);
        final String r9 = RandomStringUtils.randomAlphanumeric(length++);

        replayAll();

        assertEquals(r1, a1.cacheInvalidateAssign(pjp1, r1));
        assertEquals(r2, a2.cacheInvalidateMulti(pjp2, r2));
        assertEquals(r3, a3.cacheInvalidateSingle(pjp3, r3));

        assertEquals(r4, a4.cacheAssign(pjp4));
        assertEquals(r5, a5.cacheMulti(pjp5));
        assertEquals(r6, a6.cacheSingle(pjp6));

        assertEquals(r7, a7.cacheUpdateAssign(pjp7, r7));
        assertEquals(r8, a8.cacheUpdateMulti(pjp8, r8));
        assertEquals(r9, a9.cacheUpdateSingle(pjp9, r9));

        verifyAll();

    }



}
