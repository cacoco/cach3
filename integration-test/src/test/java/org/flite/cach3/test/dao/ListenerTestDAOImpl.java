package org.flite.cach3.test.dao;

import org.apache.commons.lang.math.RandomUtils;
import org.flite.cach3.annotations.InvalidateSingleCache;
import org.flite.cach3.annotations.ReadThroughSingleCache;
import org.flite.cach3.annotations.UpdateSingleCache;
import org.flite.cach3.annotations.groups.InvalidateSingleCaches;
import org.flite.cach3.annotations.groups.UpdateSingleCaches;
import org.flite.cach3.api.InvalidateSingleCacheListener;
import org.flite.cach3.api.UpdateSingleCacheListener;
import org.flite.cach3.test.model.TestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Copyright 2013 Flite, Inc.
 * All rights reserved.
 * <p/>
 * THIS PROGRAM IS CONFIDENTIAL AND AN UNPUBLISHED WORK AND TRADE
 * SECRET OF THE COPYRIGHT HOLDER, AND DISTRIBUTED ONLY UNDER RESTRICTION.
 */

public class ListenerTestDAOImpl implements ListenerTestDAO, UpdateSingleCacheListener, InvalidateSingleCacheListener {

    private static final Logger LOG = LoggerFactory.getLogger(ListenerTestDAOImpl.class);


    private Map<Long, TestObject> localStorageById = new HashMap<Long, TestObject>();
    private Map<String, TestObject> localStorageByGuid = new HashMap<String, TestObject>();

    private static final String NAMESPACE = "Awesome";
    private static final int FIFTEEN_MINUTES = 60 * 15; //15 min


    @UpdateSingleCaches({
            @UpdateSingleCache(namespace = NAMESPACE, keyTemplate = "$retVal.getId()", dataIndex = -1, expiration = FIFTEEN_MINUTES),
            @UpdateSingleCache(namespace = NAMESPACE, keyTemplate = "$retVal.getGuid()", dataIndex = -1, expiration = FIFTEEN_MINUTES)
    })
    public TestObject save(TestObject testObject) {
        return undercoverSave(testObject);
    }

    public TestObject undercoverSave(TestObject testObject) {
        if (testObject.getId() == null) {
            testObject.setId(RandomUtils.nextLong());
        }
        if (testObject.getGuid() == null) {
            testObject.setGuid(UUID.randomUUID().toString());
        }

        localStorageById.put(testObject.getId(), testObject);
        localStorageByGuid.put(testObject.getGuid(), testObject);

        return testObject;
    }



    @InvalidateSingleCaches({
            @InvalidateSingleCache(namespace = NAMESPACE, keyTemplate = "$args[0].getId()"),
            @InvalidateSingleCache(namespace = NAMESPACE, keyTemplate = "$args[0].getGuid()")
    })
    public void delete(TestObject testObject) {
        undercoverDelete(testObject);
    }


    public void undercoverDelete(TestObject testObject) {
        if (localStorageById.containsKey(testObject.getId())) {
            localStorageById.remove(testObject.getId());
        }
        if (localStorageByGuid.containsKey(testObject.getGuid())) {
            localStorageByGuid.remove(testObject.getGuid());
        }
    }

    @ReadThroughSingleCache(namespace = NAMESPACE, keyIndex = 0, expiration = FIFTEEN_MINUTES)
    public TestObject getById(Long id) {
        LOG.debug("Cache miss " + id);
        if (localStorageById.containsKey(id)) {
            return localStorageById.get(id);
        }
        return null;
    }

    @ReadThroughSingleCache(namespace = NAMESPACE, keyIndex = 0, expiration = FIFTEEN_MINUTES)
    public TestObject getByGuid(String guid) {
        if (localStorageByGuid.containsKey(guid)) {
            return localStorageByGuid.get(guid);
        }
        return null;
    }


    public TestObject getByIdFromListener(Long id) {
        String key = getListenerCacheKey(NAMESPACE, null, id.toString());
        if (listenerCacheMap.containsKey(key)) {
            return (TestObject) listenerCacheMap.get(key);
        }
        return null;
    }

    public TestObject getByGuidFromListener(String guid) {
        String key = getListenerCacheKey(NAMESPACE, null, guid);
        if (listenerCacheMap.containsKey(key)) {
            return (TestObject) listenerCacheMap.get(key);
        }
        return null;
    }




    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/
    /** *                  Cache listeners                                              * **/
    /** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * **/


    private Set<String> namespacesOfInterest = null;
    private Map<String, Object> listenerCacheMap = new HashMap<String, Object>();



    public void triggeredInvalidateSingleCache(String namespace, String prefix, String baseCacheId, Object retVal, Object[] args) {
        String cacheKey = getListenerCacheKey(namespace, prefix, baseCacheId);
        if (listenerCacheMap.containsKey(cacheKey))    {
            listenerCacheMap.remove(cacheKey);
        }
    }

    public void triggeredUpdateSingleCache(String namespace, String prefix, String baseCacheId, Object submission, Object retVal, Object[] args) {
        String cacheKey = getListenerCacheKey(namespace, prefix, baseCacheId);
        listenerCacheMap.put(cacheKey, submission);
    }


    private String getListenerCacheKey(String namespace, String prefix, String baseCacheId) {
        return (namespace == null ? "-NULL-" : namespace) + ":"
                + (prefix == null ? "-NULL-" : prefix) + ":"
                + (baseCacheId == null ? "-NULL-" : baseCacheId);
    }

    public Object getFromListenerCacheMap(String namespace, String prefix, String baseCacheId) {
        String cacheKey = getListenerCacheKey(namespace, prefix, baseCacheId);
        if (listenerCacheMap.containsKey(cacheKey))    {
            return listenerCacheMap.get(cacheKey);
        }
        return null;
    }


    public Set<String> getNamespacesOfInterest() {
        return null;
    }

    public void setNamespacesOfInterest(Set<String> namespacesOfInterest) {
        this.namespacesOfInterest = namespacesOfInterest;
    }

}
