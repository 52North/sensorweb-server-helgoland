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
package org.n52.io;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DatasetFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetFactory.class);

    protected final Map<String, T> cache = new HashMap<>();

    protected final File configFile;

    protected Properties mappings;

    protected DatasetFactory(String defaultConfig) {
        this(getDefaultConfigFile(defaultConfig));
    }

    protected static File getDefaultConfigFile(String configFile) {
        try {
            Path path = Paths.get(DatasetFactory.class.getResource("/").toURI());
            return path.resolve(configFile).toFile();
        } catch (URISyntaxException e) {
            LOGGER.info("Could not find config file '{}'. Load from compiled default.", configFile, e);
            return null;
        }
    }

    protected DatasetFactory(File configFile) {
        if (configFile == null) {
            throw new NullPointerException("mapping file must not be null");
        }
        this.configFile = configFile;
        mappings = new Properties();
        loadMappings(mappings, configFile);
    }

    private void loadMappings(Properties mappings, File file) {
        try (InputStream is = createConfigStream(file, getClass())) {
            mappings.load(is);
        } catch (IOException e) {
            LOGGER.error("Could not load mapping file: '{}'", file.getAbsolutePath(), e);
        }
    }

    protected InputStream createConfigStream(File file, Class<?> clazz) throws FileNotFoundException {
        if (file != null && file.exists()) {
            LOGGER.debug("loading factory config from '{}'", file.getAbsolutePath());
            return new FileInputStream(file);
        }
        if (file != null && !file.exists()) {
            LOGGER.debug("Config file not found: '{}'", file.getAbsolutePath());
        }

        LOGGER.debug("loading fallback config.");
        return new BufferedInputStream(getFallbackConfigResource());
    }

    public boolean isKnown(String datasetType) {
        return mappings.containsKey(datasetType);
    }

    public boolean hasCacheEntry(String datasetType) {
        return cache.containsKey(datasetType);
    }

    public T getCacheEntry(String datasetType) {
        return cache.get(datasetType);
    }

    public T create(String datasetType) throws DatasetFactoryException {
        if (cache.containsKey(datasetType)) {
            return cache.get(datasetType);
        }
        if ( !mappings.containsKey(datasetType)) {
            LOGGER.debug("No mapping entry for type '{}'", datasetType);
            throw new DatasetFactoryException("No datasets available for '" + datasetType + "'.");
        }
        final String clazz = mappings.getProperty(datasetType);
        try {
            final Class<?> type = Class.forName(clazz);
            T instance = createInstance(type);
            cache.put(datasetType, instance);
            initInstance(instance);
            return instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
            LOGGER.error("Invalid mapping entry '{}'='{}'", datasetType, clazz, e);
            throw new DatasetFactoryException("No datasets available for '" + datasetType + "'.");
        }
    }

    private T createInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        final Object instance = clazz.newInstance();
        try {
            return (T) instance;
        } catch (ClassCastException e) {
            String targetType = getTargetType().getName();
            String instanceType = instance.getClass().getName();
            LOGGER.error("Config entry ('{}') must be assignable to '{}'!" , instanceType, targetType);
            throw e;
        }
    }

    protected abstract InputStream getFallbackConfigResource();

    protected T initInstance(T instance) {
        return instance; // override if needed
    }

    protected abstract Class<T> getTargetType();

}
