package org.flite.cach3.level2.aop;

import org.aspectj.lang.annotation.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;

/**
 * Copyright (c) 2013 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

@Aspect
@Order((Ordered.HIGHEST_PRECEDENCE / 2) - 1)
public class L2ReadThroughMultiCacheAdvice {
}
