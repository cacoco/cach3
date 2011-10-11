package org.flite.cach3.config;

import org.apache.commons.logging.*;
import org.flite.cach3.api.*;

import java.security.*;

/**
 * Copyright (c) 2011 Flite, Inc
 * <p/>
 * All rights reserved.
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

public class ConfigurationHelper {
    private static final Log LOG = LogFactory.getLog(ConfigurationHelper.class);

    public static void setCacheDisabled(final Cach3State state, final boolean disabled) {
        if (state == null) { throw new InvalidParameterException("Cach3State must be defined."); }
        state.setCacheDisabled(disabled);
    }

    public static void addCacheListeners(final Cach3State state, final CacheListener... listeners) {
        if (state == null) { throw new InvalidParameterException("Cach3State must be defined."); }
        if (listeners == null || listeners.length == 0) { return; }

        for (final CacheListener listener : listeners) {
            if (listener == null) { continue; }

            if (listener instanceof InvalidateAssignCacheListener) {
                state.registerIAListener((InvalidateAssignCacheListener)listener);
            } else if (listener instanceof InvalidateSingleCacheListener) {
                state.registerISListener((InvalidateSingleCacheListener)listener);
            } else if (listener instanceof InvalidateMultiCacheListener) {
                state.registerIMListener((InvalidateMultiCacheListener)listener);
            } else if (listener instanceof ReadThroughAssignCacheListener) {
                state.registerRTAListener((ReadThroughAssignCacheListener)listener);
            } else if (listener instanceof ReadThroughSingleCacheListener) {
                state.registerRTSListener((ReadThroughSingleCacheListener)listener);
            } else if (listener instanceof ReadThroughMultiCacheListener) {
                state.registerRTMListener((ReadThroughMultiCacheListener)listener);
            } else if (listener instanceof UpdateAssignCacheListener) {
                state.registerUAListener((UpdateAssignCacheListener)listener);
            } else if (listener instanceof UpdateSingleCacheListener) {
                state.registerUSListener((UpdateSingleCacheListener)listener);
            } else if (listener instanceof UpdateMultiCacheListener) {
                state.registerUMListener((UpdateMultiCacheListener)listener);
            } else {
                LOG.info(String.format("Unrecognized CacheListener type: [%s]", listener.getClass().getName()));
            }
        }
    }
}
