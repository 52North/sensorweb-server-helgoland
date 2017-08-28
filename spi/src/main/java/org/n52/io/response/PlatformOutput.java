/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response;

import java.util.Collection;
import java.util.List;

import org.n52.io.geojson.GeoJSONGeometrySerializer;
import org.n52.io.response.dataset.DatasetOutput;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @since 2.0.0
 */
public class PlatformOutput extends OutputWithParameters {

    // TODO use plain string in output and let repository assert correctness
    private final PlatformType platformType;

    private Collection<DatasetOutput> datasets;

    private Geometry geometry;

    public PlatformOutput(PlatformType platformType) {
        this.platformType = platformType;
    }

    @Override
    public String getHrefBase() {
        String base = super.getHrefBase();
        String suffix = getType().getPlatformType();
        return base != null && base.endsWith(suffix)
                ? base.substring(0, base.lastIndexOf(suffix) - 1)
                : base;
    }

    public String getPlatformType() {
        return getType().getPlatformType();
    }

    @JsonIgnore
    public PlatformType getType() {
        return platformType != null
                ? platformType
                // stay backward compatible
                : PlatformType.STATIONARY_INSITU;
    }

    @Override
    public PlatformOutput setId(String id) {
        super.setId(getType().createId(id));
        return this;
    }

    public Collection<DatasetOutput> getDatasets() {
        return datasets;
    }

    public PlatformOutput setDatasets(List<DatasetOutput> series) {
        this.datasets = series;
        return this;
    }

    @JsonSerialize(using = GeoJSONGeometrySerializer.class)
    public Geometry getGeometry() {
        return geometry;
    }

    public PlatformOutput setGeometry(Geometry geometry) {
        this.geometry = geometry;
        return this;
    }

}
