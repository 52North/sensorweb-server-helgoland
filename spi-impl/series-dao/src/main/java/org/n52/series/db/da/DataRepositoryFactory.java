/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package org.n52.series.db.da;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.n52.io.DatasetFactory;
import org.n52.series.db.HibernateSessionStore;
import org.n52.series.db.beans.ServiceInfo;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class DataRepositoryFactory extends DatasetFactory<DataRepository> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRepositoryFactory.class);

    private static final Class<? extends DataRepository> DEFAULT = DataRepository.class;

    private static final String DEFAULT_CONFIG_FILE = "dataset-factory.properties";

    private Properties mappings;

    // TODO autowiring

    @Autowired
    private HibernateSessionStore sessionStore;

    @Autowired
    private ServiceInfo serviceInfo;

    public DataRepositoryFactory() {
        super(DEFAULT_CONFIG_FILE);
    }

    public DataRepositoryFactory(File configFile) {
        super(configFile);
    }

    private void lazyLoadMappings() {
        if (mappings == null) {
            mappings = new Properties();
            loadMappings(mappings, configFile);
        }
    }

    private void loadMappings(Properties mappings, File file) {
        try (InputStream is = createConfigStream(file, DEFAULT_CONFIG_FILE, DataRepositoryFactory.class)) {
            mappings.load(is);
        } catch (IOException e) {
            LOGGER.error("Could not load mapping file: '{}'", file.getAbsolutePath(), e);
        }
    }

    public boolean isKnownEntry(String datasetType) {
        lazyLoadMappings();
        return mappings.containsKey(datasetType);
    }

    public DataRepository createRepository(String datasetType) {
        lazyLoadMappings();
        if (cache.containsKey(datasetType)) {
            return cache.get(datasetType);
        }
        if ( !mappings.containsKey(datasetType)) {
            LOGGER.debug("No mapping entry for type '{}'", datasetType);
            throw new ResourceNotFoundException("No datasets available for '" + datasetType + "'.");
        }
        final String clazz = mappings.getProperty(datasetType);
        try {
            final Class<?> type = Class.forName(clazz);
            DataRepository instance = createInstance(type);
            instance.setServiceInfo(serviceInfo);
            instance.setSessionStore(sessionStore);
            cache.put(datasetType, instance);
            return instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
            LOGGER.error("Invalid mapping entry '{}'='{}'", datasetType, clazz, e);
            throw new ResourceNotFoundException("No datasets available for '" + datasetType + "'.");
        }
    }

    private DataRepository createInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        final Object instance = clazz.newInstance();
        try {
            return DEFAULT.cast(instance);
        } catch (ClassCastException e) {
            String targetType = DEFAULT.getName();
            String instanceType = instance.getClass().getName();
            LOGGER.error("Config entry ('{}') must be assignable to '{}'!" , instanceType, targetType);
            throw e;
        }
    }

    boolean hasCacheEntry(String datasetType) {
        return cache.containsKey(datasetType);
    }

    DataRepository getCacheEntry(String datasetType) {
        return cache.get(datasetType);
    }

    public HibernateSessionStore getSessionStore() {
        return sessionStore;
    }

    public void setSessionStore(HibernateSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

}
