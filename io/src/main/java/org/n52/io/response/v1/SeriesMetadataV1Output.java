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

package org.n52.io.response.v1;

import org.n52.io.response.ParameterOutput;
import org.n52.io.response.ServiceOutput;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.io.response.dataset.SeriesParameters;

@Deprecated
public class SeriesMetadataV1Output extends TimeseriesMetadataOutput {

    @Override
    public SeriesParameters getSeriesParameters() {
        return new AdaptedSeriesParameters(super.getSeriesParameters());
    }

    private class AdaptedSeriesParameters extends SeriesParameters {

        private final SeriesParameters parameters;

        public AdaptedSeriesParameters(SeriesParameters parameters) {
            this.parameters = parameters == null
                    ? new SeriesParameters()
                    : parameters;
        }

        @Override
        public ParameterOutput getPlatform() {
            return SeriesMetadataV1Output.this.getStation();
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
        public void setService(ServiceOutput service) {
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
