package org.flite.cach3.level2.aop;

import java.util.*;

/**
 * Copyright (c) 2013 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

public class AnnotationInfo {
    final Map<String, AnnotationDatum> data = new HashMap<String, AnnotationDatum>(10);

    AnnotationInfo add(AnnotationDatum datum) {
        data.put(datum.getName(), datum);
        return this;
    }

    AnnotationDatum get(final String name) {
        return data.get(name);
    }
}
