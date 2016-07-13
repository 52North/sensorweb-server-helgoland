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
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.io.response.v1.ext.ObservationType;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.beans.ext.CountObservationSeriesEntity;
import org.n52.series.db.da.beans.ext.MeasurementSeriesEntity;
import org.n52.series.db.da.beans.ext.TextObservationSeriesEntity;
import org.n52.web.exception.ResourceNotFoundException;

public class ObservationTypeToEntityMapperTest {

    private ObservationTypeToEntityMapper mapper;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws URISyntaxException {
        File file = getMappingFile("observationTypeMapping.properties");
        mapper = new ObservationTypeToEntityMapper(file);
    }

    @Test
    public void when_createWithNoConfig_useDefaultConfig() {
        ObservationTypeToEntityMapper m = new ObservationTypeToEntityMapper();
        Assert.assertTrue(m.hasMappings());
        assertTrue(mapper.mapToEntityClass("measurement") == MeasurementSeriesEntity.class);
    }

    @Test
    public void when_all_then_returnAbstractSeriesEntityAsDefault() {
        assertTrue(mapper.mapToEntityClass("all") == AbstractSeriesEntity.class);
    }

    @Test
    public void when_mapToText_then_returnTextSeriesEntity() {
        assertTrue(mapper.mapToEntityClass("text") == TextObservationSeriesEntity.class);
    }

    @Test
    public void when_mapToText_then_returnCountSeriesEntity() {
        assertTrue(mapper.mapToEntityClass("count") == CountObservationSeriesEntity.class);
    }

    @Test
    public void when_mapToText_then_returnMeasurementSeriesEntity() {
        assertTrue(mapper.mapToEntityClass("measurement") == MeasurementSeriesEntity.class);
    }

    @Test
    @Deprecated
    public void when_mapToMEASUREMENT_then_returnMeasurementSeriesEntity() {
        assertTrue(mapper.mapToEntityClass(ObservationType.MEASUREMENT) == MeasurementSeriesEntity.class);
    }

    @Test
    @Deprecated
    public void when_mapToCOUNT_then_returnCountSeriesEntity() {
        assertTrue(mapper.mapToEntityClass(ObservationType.COUNT) == CountObservationSeriesEntity.class);
    }

    @Test
    @Deprecated
    public void when_mapToTEXT_then_returnMeasurementSeriesEntity() {
        assertTrue(mapper.mapToEntityClass(ObservationType.TEXT) == TextObservationSeriesEntity.class);
    }

    @Test
    public void when_mapToInvalid_then_throwException() {
        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage(is("No datasets available for 'invalid'."));
        mapper.mapToEntityClass("invalid");
    }

    @Test
    public void when_createdWithInvalidFile_then_throwExceptionOnAll() throws URISyntaxException {
        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage(is("No datasets available for 'all'."));
        final File file = getMappingFile("empty_observationTypeMapping.properties");
        new ObservationTypeToEntityMapper(file).mapToEntityClass("all");
    }

    private File getMappingFile(String name) throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/files").toURI());
        return root.resolve(name).toFile();
    }

}
