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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ServiceEntity extends DescribableEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEntity.class);

    private String url;

    private String type = "Thin DB access layer service.";

    private List<String> noDataValues;

    private String version;

    public ServiceEntity() {
        noDataValues = Collections.emptyList();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    public boolean isNoDataValue(DataEntity<?> observation) {
        return observation.isNoDataValue(noDataValues);
    }

    public String getNoDataValues() {
        final String csv = Arrays.toString(noDataValues.toArray(new Double[0])); //XXX
        return csv.substring(1).substring(0, csv.length() - 2);
    }

    public void setNoDataValues(String noDataValues) {
        LOGGER.debug("Set noData values: {}", noDataValues);
        if (noDataValues == null || noDataValues.isEmpty()) {
            this.noDataValues = Collections.emptyList();
        } else {
            String[] values = noDataValues.split(",");
            this.noDataValues = Arrays.asList(values);
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append(" url: ").append(getUrl());
        sb.append(", type: ").append(getType());
        sb.append(", version: ").append(getVersion());
        sb.append(", noDataValues: ").append(getNoDataValues());
        return sb.append(" ]").toString();
    }

}
