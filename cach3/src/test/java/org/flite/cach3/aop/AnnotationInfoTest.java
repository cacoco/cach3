package org.flite.cach3.aop;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.testng.AssertJUnit.*;

/**
 * Copyright (c) 2006-2013 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */
public class AnnotationInfoTest {

    public static final List<String> ALL_TYPES = Arrays.asList(AType.ASSIGN_KEY, AType.DATA_INDEX, AType.EXPIRATION,
            AType.JITTER, AType.KEY_INDEX, AType.KEY_PREFIX, AType.KEY_TEMPLATE, AType.NAMESPACE, AType.WINDOW);

    public static void ensureValuesNotSet(final AnnotationInfo info, final Collection<String> okToBeSet) {
        if (info == null) { throw new InvalidParameterException("Info object must be defined."); }
        for (final String type : ALL_TYPES) {
            if (okToBeSet != null && okToBeSet.contains(type)) { continue; }
            assertNull("Unexpected value for: " + type, info.get(type));
        }
    }
}
