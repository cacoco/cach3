package org.flite.cach3.test.l2;

import org.apache.commons.lang.*;
import org.flite.cach3.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import org.testng.annotations.*;

import java.util.*;

import static org.testng.AssertJUnit.*;

public class L2ReadThroughMultiTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {
        final TestSvc test = (TestSvc) context.getBean("testSvc");

        final String g1 = RandomStringUtils.randomAlphabetic(4) + "-";

        final List<Long> ids = new ArrayList<Long>();
        final long base = System.currentTimeMillis() - 200000;
        for (int ix = 0; ix < 10; ix++) {
            ids.add(base + ix);
        }

        final List<String> first = test.getL2MultiAlpha(ids, g1);
        for (final String out : first) {
            assertTrue(out.startsWith(g1));
        }

        final String g2 = RandomStringUtils.randomAlphabetic(6) + "-";
        final List<Long> addls = new ArrayList<Long>(ids);
        for (int ix = 0; ix < 10; ix++) {
            addls.add(System.currentTimeMillis() + ix);
        }

        Collections.shuffle(addls);
        final List<String> results = test.getL2MultiAlpha(addls, g2);
        for (int ix = 0; ix < addls.size(); ix++) {
            final Long key = addls.get(ix);
            final String result = results.get(ix);
            System.out.println(result);
            assertTrue(StringUtils.contains(result, key.toString()));
            assertTrue(result.startsWith(ids.contains(key) ? g1 : g2));
        }


    }

}
