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
package org.n52.io.response.dataset;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.n52.io.Utils;
import org.n52.io.request.StyleProperties;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.StatusInterval;
import org.n52.io.response.dataset.quantity.QuantityDatasetOutput;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @deprecated since 2.0.0. use {@link QuantityDatasetOutput} instead.
 */
@Deprecated
public class TimeseriesMetadataOutput extends QuantityDatasetOutput {

    @Deprecated
    private StyleProperties renderingHints;

    @Deprecated
    private StatusInterval[] statusIntervals;

    private StationOutput station;

    private Set<String> rawFormats;

    @Override
    @JsonIgnore
    public String getValueType() {
        return super.getValueType();
    }

    @Override
    public String getId() {
        return ValueType.extractId(super.getId());
    }

    public StationOutput getStation() {
        return station;
    }

    public void setStation(StationOutput station) {
        this.station = station;
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

    @Deprecated
    public StyleProperties getRenderingHints() {
        return this.renderingHints;
    }

    @Deprecated
    public void setRenderingHints(StyleProperties renderingHints) {
        this.renderingHints = renderingHints;
    }

    @Deprecated
    public StatusInterval[] getStatusIntervals() {
        return Utils.copy(statusIntervals);
    }

    @Deprecated
    public void setStatusIntervals(StatusInterval[] statusIntervals) {
        this.statusIntervals = Utils.copy(statusIntervals);
    }

    @Override
    @JsonProperty("parameters")
    public SeriesParameters getSeriesParameters() {
        return new AdaptedSeriesParameters(super.getSeriesParameters());
    }

    private static class AdaptedSeriesParameters extends SeriesParameters {

        private final SeriesParameters parameters;

        AdaptedSeriesParameters(SeriesParameters parameters) {
            this.parameters = parameters == null
                    ? new SeriesParameters()
                    : parameters;
        }

        @Override
        public ParameterOutput getPlatform() {
            // stay backwards compatible
            return null;
        }

        @Override
        public void setPhenomenon(ParameterOutput phenomenon) {
            parameters.setPhenomenon(phenomenon);
        }

        @Override
        public void setProcedure(ParameterOutput procedure) {
            parameters.setProcedure(procedure);
        }

        @Override
        public void setCategory(ParameterOutput category) {
            parameters.setCategory(category);
        }

        @Override
        public void setOffering(ParameterOutput offering) {
            parameters.setOffering(offering);
        }

        @Override
        public void setFeature(ParameterOutput feature) {
            parameters.setFeature(feature);
        }

        @Override
        public void setService(ParameterOutput service) {
            parameters.setService(service);
        }

        @Override
        public void setPlatform(ParameterOutput platform) {
            parameters.setPlatform(platform);
        }

        @Override
        public ParameterOutput getPhenomenon() {
            return parameters.getPhenomenon();
        }

        @Override
        public ParameterOutput getProcedure() {
            return parameters.getProcedure();
        }

        @Override
        public ParameterOutput getCategory() {
            return parameters.getCategory();
        }

        @Override
        public ParameterOutput getOffering() {
            return parameters.getOffering();
        }

        @Override
        public ParameterOutput getFeature() {
            return parameters.getFeature();
        }

        @Override
        public ParameterOutput getService() {
            return parameters.getService();
        }

    }
}
