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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.n52.series.db.beans.ServiceInfo;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class DataRepositoryFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRepositoryFactory.class);

    private static final Class<? extends DataRepository> DEFAULT = DataRepository.class;

    private static final String DEFAULT_CONFIG_FILE = "dataset-factory.properties";

    private final Map<String, DataRepository> cache;

    private final Properties mappings;
    
    @Autowired
    private ServiceInfo serviceInfo;

    public DataRepositoryFactory() {
        this(getDefaultConfigFile());
    }

    private static File getDefaultConfigFile() {
        try {
            Path path = Paths.get(DataRepositoryFactory.class.getResource("/").toURI());
            return path.resolve(DEFAULT_CONFIG_FILE).toFile();
        } catch (URISyntaxException e) {
            LOGGER.info("Could not find config file '{}'. Load from compiled default.", DEFAULT_CONFIG_FILE, e);
            return null;
        }
    }

    public DataRepositoryFactory(File file) {
        if (file == null) {
            throw new NullPointerException("mapping file must not be null");
        }
        cache = new HashMap<>();
        mappings = new Properties();
        loadMappings(mappings, file);
    }

    private static void loadMappings(Properties mappings, File file) {
        try (InputStream is = createConfigStream(file)) {
            mappings.load(is);
        } catch (IOException e) {
            LOGGER.error("Could not load mapping file: '{}'", file.getAbsolutePath(), e);
        }
    }

    private static InputStream createConfigStream(File file) throws FileNotFoundException {
        if (file != null && file.exists()) {
            return new FileInputStream(file);
        }
        final String jarResource = "/" + DEFAULT_CONFIG_FILE;
        final Class<DataRepositoryFactory> clazz = DataRepositoryFactory.class;
        return new BufferedInputStream(clazz.getResourceAsStream(jarResource));
    }

    public boolean isKnownEntry(String datasetType) {
        return mappings.containsKey(datasetType);
    }

    public DataRepository createRepository(String datasetType) {
        if (cache.containsKey(datasetType)) {
            return cache.get(datasetType);
        }
        if ( !mappings.containsKey(datasetType)) {
            LOGGER.error("No mapping entry for type '{}'", datasetType);
            throw new ResourceNotFoundException("No datasets available for '" + datasetType + "'.");
        }
        final String clazz = mappings.getProperty(datasetType);
        try {
            final Class<?> type = Class.forName(clazz);
            DataRepository instance = createInstance(type);
            instance.setServiceInfo(serviceInfo);
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

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    
}
