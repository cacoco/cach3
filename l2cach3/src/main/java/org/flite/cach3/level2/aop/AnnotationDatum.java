package org.flite.cach3.level2.aop;

/**
 * Copyright (c) 2013 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

public interface AnnotationDatum<T> {
    String getName();
    T getValue();
}