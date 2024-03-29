/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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

import org.n52.io.request.IoParameters;
import org.n52.io.request.StyleProperties;
import org.n52.io.response.OptionalOutput;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.StatusInterval;
import org.n52.io.response.dataset.quantity.QuantityDatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class TimeseriesMetadataOutput extends QuantityDatasetOutput {

    public static final String COLLECTION_PATH = "timeseries";

    public static final String STATION = "station";
    public static final String RENDERING_HINTS = "renderingHints";
    public static final String STATUS_INTERVALS = "statusIntervals";

    private OptionalOutput<StationOutput> station;

    @Deprecated
    private OptionalOutput<StyleProperties> renderingHints;

    @Deprecated
    private OptionalOutput<Collection<StatusInterval>> statusIntervals;

    public TimeseriesMetadataOutput(IoParameters parameters) {
        setValue(DatasetOutput.VALUE_TYPE, QuantityValue.TYPE, parameters, this::setValueType);
    }

    @Override
    @JsonIgnore
    public String getValueType() {
        return super.getValueType();
    }

    public StationOutput getStation() {
        return getIfSerialized(station);
    }

    public void setStation(OptionalOutput<StationOutput> station) {
        this.station = station;
    }

    @Deprecated
    public StyleProperties getRenderingHints() {
        return getIfSerialized(renderingHints);
    }

    @Deprecated
    public void setRenderingHints(OptionalOutput<StyleProperties> renderingHints) {
        this.renderingHints = renderingHints;
    }

    @Deprecated
    public Collection<StatusInterval> getStatusIntervals() {
        return getIfSerializedCollection(statusIntervals);
    }

    @Deprecated
    public void setStatusIntervals(OptionalOutput<Collection<StatusInterval>> statusIntervals) {
        this.statusIntervals = statusIntervals;
    }

    @Override
    @JsonProperty("parameters")
    public DatasetParameters getDatasetParameters() {
        DatasetParameters datasetParameters = super.getDatasetParameters();
        return datasetParameters != null
                ? new AdaptedSeriesParameters(datasetParameters)
                : null;
    }

    private static class AdaptedSeriesParameters extends DatasetParameters {

        private final DatasetParameters parameters;

        AdaptedSeriesParameters(DatasetParameters parameters) {
            this.parameters = parameters == null
                    ? new DatasetParameters()
                    : parameters;
        }

        @Override
        public ParameterOutput getPlatform() {
            // stay backwards compatible
            return null;
        }

        @Override
        public AdaptedSeriesParameters setPhenomenon(ParameterOutput phenomenon) {
            parameters.setPhenomenon(phenomenon);
            return this;
        }

        @Override
        public AdaptedSeriesParameters setProcedure(ParameterOutput procedure) {
            parameters.setProcedure(procedure);
            return this;
        }

        @Override
        public AdaptedSeriesParameters setCategory(ParameterOutput category) {
            parameters.setCategory(category);
            return this;
        }

        @Override
        public AdaptedSeriesParameters setOffering(ParameterOutput offering) {
            parameters.setOffering(offering);
            return this;
        }

        @Override
        public AdaptedSeriesParameters setService(ParameterOutput service) {
            parameters.setService(service);
            return this;
        }

        @Override
        public AdaptedSeriesParameters setPlatform(ParameterOutput platform) {
            parameters.setPlatform(platform);
            return this;
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
        public ParameterOutput getService() {
            return parameters.getService();
        }

    }

}
