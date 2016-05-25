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
package org.n52.io.response.v1.ext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.n52.io.response.ParameterOutput;
import org.n52.io.response.SeriesParameters;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class SeriesMetadataOutput extends ParameterOutput {

    private final ObservationType observationType;

    private SeriesParameters seriesParameters;

    private String uom;

    private Set<String> rawFormats;

    public SeriesMetadataOutput(ObservationType observationType) {
        this.observationType = observationType;
    }

    @Override
    public void setId(String id) {
        super.setId(getUrlIdSuffix() + "/" + id);
    }

    public String getObservationType() {
        return getType().getObservationType();
    }

    protected String getUrlIdSuffix() {
        return getType().getObservationType();
    }

    @JsonIgnore
    public ObservationType getType() {
        return observationType;
    }

    public SeriesParameters getSeriesParameters() {
        return seriesParameters;
    }

    public void setSeriesParameters(SeriesParameters parameters) {
        this.seriesParameters = parameters;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    @Override
    public String[] getRawFormats() {
        if (rawFormats != null) {
            return rawFormats.toArray(new String[0]);
        }
        return null;
    }

    @Override
    public void addRawFormat(String format) {
        if (format != null && !format.isEmpty()) {
            if (rawFormats == null) {
                rawFormats = new HashSet<>();
            }
            rawFormats.add(format);
        }
    }

    @Override
    public void setRawFormats(Collection<String> formats) {
        if (formats != null && !formats.isEmpty()) {
            if (rawFormats == null) {
                rawFormats = new HashSet<>();
            } else {
                rawFormats.clear();
            }
            this.rawFormats.addAll(formats);
        }
    }

}
