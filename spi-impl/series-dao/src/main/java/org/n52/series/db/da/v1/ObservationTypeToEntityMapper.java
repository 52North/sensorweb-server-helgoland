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

package org.n52.series.db.da.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.n52.io.response.v1.ext.ObservationType;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class ObservationTypeToEntityMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationTypeToEntityMapper.class);

    private static final Class<? extends AbstractSeriesEntity> DEFAULT = AbstractSeriesEntity.class;

    private static final String DEFAULT_CONFIG_FILE = "observation-types.properties";

    private final Properties mappings;

    ObservationTypeToEntityMapper() {
        this(getDefaultConfigFile());
    }

    private static File getDefaultConfigFile() {
        try {
            Path path = Paths.get(ObservationTypeToEntityMapper.class.getResource("/").toURI());
            return path.resolve(DEFAULT_CONFIG_FILE).toFile();
        } catch (URISyntaxException e) {
            LOGGER.error("Could not find default config file '{}'", DEFAULT_CONFIG_FILE, e);
            return null;
        }
    }

    ObservationTypeToEntityMapper(File file) {
        if (file == null) {
            throw new NullPointerException("mapping file must not be null");
        }
        mappings = new Properties();
        loadMappings(mappings, file);
    }

    private static void loadMappings(Properties mappings, File file) {
        try {
            mappings.load(new FileInputStream(file));
        } catch (IOException e) {
            LOGGER.error("Could not load mapping file: '{}'", file.getAbsolutePath(), e);
        }
    }

    public boolean hasMappings() {
        return !mappings.isEmpty();
    }

    Class<? extends AbstractSeriesEntity> mapToEntityClass(String datasetType) {
        return !isAllEntitiesRequested(datasetType)
                ? getRequestedEntityFor(datasetType)
                : DEFAULT;
    }

    private boolean isAllEntitiesRequested(String datasetType) {
        return hasMappings() && "all".equalsIgnoreCase(datasetType);
    }

    @Deprecated
    Class<? extends AbstractSeriesEntity> mapToEntityClass(ObservationType type) {
        return getRequestedEntityFor(type.name().toLowerCase());
    }

    private Class<? extends AbstractSeriesEntity> getRequestedEntityFor(String type) {
        if ( !mappings.containsKey(type)) {
            LOGGER.error("No mapping entry for type '{}'", type);
            throw new ResourceNotFoundException("No datasets available for '" + type + "'.");
        }
        final String clazz = mappings.getProperty(type);
        try {
            final Class<?> instance = Class.forName(clazz);
            return (Class<? extends AbstractSeriesEntity>) instance;
        } catch (ClassNotFoundException e) {
            LOGGER.error("Invalid mapping entry '{}'='{}'", type, clazz, e);
            throw new ResourceNotFoundException("No datasets available for '" + type + "'.");
        }
    }

}
