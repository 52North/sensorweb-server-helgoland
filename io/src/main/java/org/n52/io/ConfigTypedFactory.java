/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class ConfigTypedFactory<T> implements
        ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTypedFactory.class);

    protected final Map<String, T> cache = new HashMap<>();

    protected Properties mappings;

    private transient AutowireCapableBeanFactory beanFactory;

    protected ConfigTypedFactory(File configFile) {
        this.mappings = new Properties();
        try {
            loadMappings(createConfigStream(configFile, getClass()), mappings);
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not find mapping file: '{}'", configFile.getAbsolutePath(), e);
        }
    }

    protected ConfigTypedFactory(InputStream is) {
        this.mappings = new Properties();
        loadMappings(is, mappings);
    }

    protected ConfigTypedFactory(String defaultConfig) {
        this(getDefaultConfigFile(defaultConfig));
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        beanFactory = context.getAutowireCapableBeanFactory();
    }

    protected static InputStream getDefaultConfigFile(String configLocation) {
        try {
            Path path = Paths.get(ConfigTypedFactory.class.getResource("/")
                                                          .toURI());
            File file = path.resolve(configLocation)
                            .toFile();
            if (file.exists()) {
                return new FileInputStream(file);
            } else {
                InputStream stream = ConfigTypedFactory.class.getResourceAsStream(configLocation);
                return stream == null
                        ? ConfigTypedFactory.class.getResourceAsStream("/" + configLocation)
                        : stream;
            }
        } catch (URISyntaxException | FileNotFoundException e) {
            LOGGER.info("Could not find config file '{}'. Load from compiled default.", configLocation, e);
            return null;
        }
    }

    private void loadMappings(InputStream loadFrom, Properties loadTo) {
        try (InputStream stream = loadFrom) {
            loadTo.load(stream);
        } catch (IOException e) {
            LOGGER.error("Could not load mapping from stream!", e);
        }
    }

    private InputStream createConfigStream(File file, Class< ? > clazz) throws FileNotFoundException {
        if (file != null && file.exists()) {
            LOGGER.debug("loading factory config from '{}'", file.getAbsolutePath());
            return new FileInputStream(file);
        }
        if (file != null && !file.exists()) {
            LOGGER.debug("Config file not found: '{}'", file.getAbsolutePath());
        }

        LOGGER.debug("loading fallback config.");
        final String jarResource = getFallbackConfigResource();
        return new BufferedInputStream(getTargetType().getResourceAsStream(jarResource));
    }

    public boolean isKnown(String type) {
        return mappings.containsKey(type);
    }

    public Set<String> getKnownTypes() {
        return mappings.stringPropertyNames();
    }

    public boolean hasCacheEntry(String type) {
        return cache.containsKey(type);
    }

    public T getCacheEntry(String type) {
        return cache.get(type);
    }

    public T create(String type) throws DatasetFactoryException {
        if (cache.containsKey(type)) {
            return cache.get(type);
        }
        if (!mappings.containsKey(type)) {
            LOGGER.debug("No mapping entry for type '{}'", type);
            throwNewNoDatasetsAvailableForTypeException(type);
            return null;
        }
        final String clazz = mappings.getProperty(type);
        try {
            final Class< ? > instanceType = Class.forName(clazz);
            T instance = createInstance(instanceType);
            cache.put(type, instance);
            initInstance(instance);
            return instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
            LOGGER.error("Invalid mapping entry '{}'='{}'", type, clazz, e);
            throwNewNoDatasetsAvailableForTypeException(type);
            return null;
        }
    }

    private T createInstance(Class< ? > clazz) throws InstantiationException, IllegalAccessException {
        final Object instance = clazz.newInstance();
        try {
            return (T) instance;
        } catch (ClassCastException e) {
            String targetType = getTargetType().getName();
            String instanceType = instance.getClass()
                                          .getName();
            LOGGER.error("Config entry ('{}') must be assignable to '{}'!", instanceType, targetType);
            throw e;
        }
    }

    protected abstract String getFallbackConfigResource();

    // override if needed
    protected T initInstance(T instance) {
        if (beanFactory != null) {
            beanFactory.autowireBean(instance);
        }
        return instance;
    }

    protected abstract Class< ? > getTargetType();

    private void throwNewNoDatasetsAvailableForTypeException(String type) throws DatasetFactoryException {
        throw new DatasetFactoryException("No datasets available for '" + type + "'.");
    }

}
