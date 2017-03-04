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
package org.n52.series.db.beans;


import java.util.Set;

import org.n52.series.db.beans.parameter.Parameter;

import com.vividsolutions.jts.geom.Geometry;

public class FeatureEntity extends HierarchicalEntity<FeatureEntity> {

    /**
     * @since 2.0.0
     */
    private GeometryEntity geometryEntity;

    public Geometry getGeometry() {
        return getGeometry(null);
    }

    public Geometry getGeometry(String srid) {
        return geometryEntity != null ? geometryEntity.getGeometry(srid) : null;
    }

    public void setGeometry(Geometry geometry) {
        this.geometryEntity = new GeometryEntity();
        this.geometryEntity.setGeometry(geometry);
    }

    public GeometryEntity getGeometryEntity() {
        return geometryEntity;
    }

    public void setGeometryEntity(GeometryEntity geometryEntity) {
        this.geometryEntity = geometryEntity;
    }

    public boolean isSetGeometry() {
        return geometryEntity != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append(" Domain id: ").append(getDomainId());
        sb.append(", service: ").append(getService());
        return sb.append(" ]").toString();
    }

}
