package org.flite.cach3.aop;

import org.flite.cach3.annotations.*;

/**
 * Copyright (c) 2013 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

public class AType {
    public static final String NAMESPACE = "ns";
    public static final String KEY_INDEX = "idx";
    public static final String KEY_TEMPLATE = "tmpl";
    public static final String KEY_PREFIX = "prfx";
    public static final String WINDOW = "wndw";
    public static final String DATA_INDEX = "didx";
    public static final String DATA_TEMPLATE = "dtmpl";
    public static final String DATA_TEMPLATE_TYPE = "dtmpltype";
    public static final String ASSIGN_KEY = "assn";
    public static final String EXPIRATION = "expn";
    public static final String JITTER = "jitr";

    private AType() { }

    public static class Namespace implements AnnotationDatum<String> {
        private String value;
        public Namespace(String value) { this.value = value; }
        public String getName() { return NAMESPACE; }
        public String getValue() { return value; }
    }

    public static class KeyIndex implements AnnotationDatum<Integer> {
        private Integer value;
        public KeyIndex(Integer value) { this.value = value; }
        public String getName() { return KEY_INDEX; }
        public Integer getValue() { return value; }
    }

    public static class KeyTemplate implements AnnotationDatum<String> {
        private String value;
        public KeyTemplate(String value) { this.value = value; }
        public String getName() { return KEY_TEMPLATE; }
        public String getValue() { return value; }
    }

    public static class KeyPrefix implements AnnotationDatum<String> {
        private String value;
        public KeyPrefix(String value) { this.value = value; }
        public String getName() { return KEY_PREFIX; }
        public String getValue() { return value; }
    }

    public static class Window implements AnnotationDatum<Duration> {
        private Duration value;
        public Window(Duration value) { this.value = value; }
        public String getName() { return WINDOW; }
        public Duration getValue() { return value; }
    }

    public static class DataIndex implements AnnotationDatum<Integer> {
        private Integer value;
        public DataIndex(Integer value) { this.value = value; }
        public String getName() { return DATA_INDEX; }
        public Integer getValue() { return value; }
    }

    public static class DataTemplate implements AnnotationDatum<String> {
        private String value;
        public DataTemplate(String value) { this.value = value; }
        public String getName() { return DATA_TEMPLATE; }
        public String getValue() { return value; }
    }

    public static class DataTemplateType implements AnnotationDatum<Class> {
        private Class value;
        public DataTemplateType(Class value) { this.value = value; }
        public String getName() { return DATA_TEMPLATE_TYPE; }
        public Class getValue() { return value; }
    }

    public static class AssignKey implements AnnotationDatum<String> {
        private String value;
        public AssignKey(String value) { this.value = value; }
        public String getName() { return ASSIGN_KEY; }
        public String getValue() { return value; }
    }

    public static class Expiration implements AnnotationDatum<Integer> {
        private Integer value;
        public Expiration(Integer value) { this.value = value; }
        public String getName() { return EXPIRATION; }
        public Integer getValue() { return value; }
    }

    public static class Jitter implements AnnotationDatum<Integer> {
        private Integer value;
        public Jitter(Integer value) { this.value = value; }
        public String getName() { return JITTER; }
        public Integer getValue() { return value; }
    }

}
