package org.flite.cach3.aop;

import com.sun.tools.javac.util.*;
import net.vidageek.mirror.dsl.*;
import org.apache.commons.lang.*;
import org.flite.cach3.annotations.*;
import org.testng.annotations.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import java.util.List;

import static org.testng.AssertJUnit.*;

/**
 * Copyright (c) 2011-2012 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

public class AnnotationDataBuilderTest {

    private static final String TMPL = "args[99]";

    /**
     * This ain't pretty, but it does the job.
     *
     * It tests different combinations of keyIndex() and keyTemplate() on the different non-*AssignCache annotations.
     *
     * The *MultiCache annotations require a keyIndex(), with an optional keyTemplate().
     *
     * The *SingleCache annotations require having only 1 of either keyIndex() or keyTemplate().
     *
     * The ReadThrough[Single,Multi]Cache annotations must have a keyIndex >= 0 if they have one at all.
     *
     */
    @Test
    public void testExample() {
        final Map<Class, Pair<String, List<String>>> map = new HashMap<Class, Pair<String, List<String>>>();
        map.put(ReadThroughSingleCache.class, new Pair<String, List<String>>("rts", Arrays.asList("Bad1", "Bad2", "Bad3", "GoodIndex", "GoodTemplate")));
        map.put(ReadThroughMultiCache.class, new Pair<String, List<String>>("rtm", Arrays.asList("Bad1", "Bad2", "Bad3", "Good", "GoodTemplate")));
        map.put(InvalidateSingleCache.class, new Pair<String, List<String>>("is", Arrays.asList("Bad1", "Good", "GoodTemplate")));
        map.put(InvalidateMultiCache.class, new Pair<String, List<String>>("im", Arrays.asList("Bad1", "Good", "GoodTemplate")));
        map.put(UpdateSingleCache.class, new Pair<String, List<String>>("us", Arrays.asList("Bad1", "Good", "GoodTemplate")));
        map.put(UpdateMultiCache.class, new Pair<String, List<String>>("um", Arrays.asList("Bad1", "Good", "GoodTemplate")));

        for (Map.Entry<Class, Pair<String, List<String>>> entry : map.entrySet()) {
            final Class clazz = entry.getKey();
            final String prefix = entry.getValue().fst;
            final List<String> suffixes = entry.getValue().snd;

            for (final String suffix : suffixes) {
                final String name = prefix + suffix;
                final String id = String.format("Working on [%s].%s()", clazz.getSimpleName(), name);
//                System.out.println(id);
                final Method method = new Mirror().on(ExampleClass.class).reflect().method(name).withAnyArgs();
                try {
                    final AnnotationData data = AnnotationDataBuilder.buildAnnotationData(method.getAnnotation(clazz), clazz, method.getName());
                    if (!StringUtils.contains(suffix, "Good")) {
                        fail("Expected exception for " + id);
                    } else {
//                        System.out.println("Success: " + id);
                        if (StringUtils.contains(suffix, "Template")) {
                            assertEquals(TMPL, data.getKeyTemplate());
                        } else {
                            assertEquals(1, data.getKeyIndex());
                        }
                    }
                } catch (InvalidParameterException ex) {
                    if (StringUtils.contains(suffix, "Good")) {
                        fail("Didn't expect exception for " + id + "\n" + ex.getMessage());
                    }
                }
            }

        }
    }

    private static class ExampleClass {

        @UpdateMultiCache(namespace="A", dataIndex=-1, keyTemplate=TMPL)
        public String umBad1(final Long id, final String alt) { return ""; }

        @UpdateMultiCache(namespace="A", dataIndex=-1, keyIndex=1)
        public String umGood(final Long id, final String alt) { return ""; }

        @UpdateMultiCache(namespace="A", dataIndex=-1, keyIndex=-1, keyTemplate=TMPL)
        public String umGoodTemplate(final Long id, final String alt) { return ""; }


        @UpdateSingleCache(namespace="A", dataIndex=-1, keyIndex=-1, keyTemplate=TMPL)
        public String usBad1(final Long id, final String alt) { return ""; }

        @UpdateSingleCache(namespace="A", dataIndex=-1, keyIndex=1)
        public String usGood(final Long id, final String alt) { return ""; }

        @UpdateSingleCache(namespace="A", dataIndex=-1, keyTemplate=TMPL)
        public String usGoodTemplate(final Long id, final String alt) { return ""; }


        @InvalidateMultiCache(namespace="A", keyTemplate=TMPL)
        public List<String> imBad1(final Long id, final String alt) { return Collections.EMPTY_LIST; }

        @InvalidateMultiCache(namespace="A", keyIndex=1)
        public List<String> imGood(final Long id, final String alt) { return Collections.EMPTY_LIST; }

        @InvalidateMultiCache(namespace="A", keyIndex = 0, keyTemplate=TMPL)
        public List<String> imGoodTemplate(final Long id, final String alt) { return Collections.EMPTY_LIST; }


        @InvalidateSingleCache(namespace="A", keyIndex=-1, keyTemplate=TMPL)
        public String isBad1(final Long id, final String alt) { return ""; }

        @InvalidateSingleCache(namespace="A", keyIndex=1)
        public String isGood(final Long id, final String alt) { return ""; }

        @InvalidateSingleCache(namespace="A", keyTemplate=TMPL)
        public String isGoodTemplate(final Long id, final String alt) { return ""; }


        @ReadThroughMultiCache(namespace="A", expiration=123, keyIndex=-1)
        public List<String> rtmBad1(final Long id, final String alt) { return Collections.EMPTY_LIST; }

        @ReadThroughMultiCache(namespace="A", expiration=123)
        public List<String> rtmBad2(final Long id, final String alt) { return Collections.EMPTY_LIST; }

        @ReadThroughMultiCache(namespace="A", expiration=123, keyTemplate=TMPL)
        public List<String> rtmBad3(final Long id, final String alt) { return Collections.EMPTY_LIST; }

        @ReadThroughMultiCache(namespace="A", expiration=123, keyIndex=1)
        public List<String> rtmGood(final Long id, final String alt) { return Collections.EMPTY_LIST; }

        @ReadThroughMultiCache(namespace="A", expiration=123, keyIndex=0, keyTemplate=TMPL)
        public List<String> rtmGoodTemplate(final Long id, final String alt) { return Collections.EMPTY_LIST; }


        @ReadThroughSingleCache(namespace="A", expiration=123, keyIndex = 0, keyTemplate=TMPL)
        public String rtsBad1(final Long id, final String alt) { return ""; }

        @ReadThroughSingleCache(namespace="A", expiration=123)
        public String rtsBad2(final Long id, final String alt) { return ""; }

        @ReadThroughSingleCache(namespace="A", expiration=123, keyIndex=-1)
        public String rtsBad3(final Long id, final String alt) { return ""; }

        @ReadThroughSingleCache(namespace="A", expiration=123, keyIndex=1)
        public String rtsGoodIndex(final Long id, final String alt) { return ""; }

        @ReadThroughSingleCache(namespace="A", expiration=123, keyTemplate=TMPL)
        public String rtsGoodTemplate(final Long id, final String alt) { return ""; }
    }
}
