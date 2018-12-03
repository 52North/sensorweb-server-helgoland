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
package org.n52.io.response.dataset;

import org.n52.io.response.OptionalOutput;
import org.n52.io.response.ParameterOutput;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractDatasetOutput extends ParameterOutput {

    public static final String AGGREGATION = "aggregation";
    public static final String MOBILE = "mobile";
    public static final String INSITU = "insitu";
    public static final String UOM = "uom";
    public static final String DATASET_PARAMETERS = "parameters";
    public static final String ORIGIN_TIMEZONE = "originTimezone";

    private OptionalOutput<String> aggregation;
    private OptionalOutput<Boolean> mobile;
    private OptionalOutput<Boolean> insitu;
    private OptionalOutput<String> uom;
    private OptionalOutput<String> originTimezone;
    private OptionalOutput<DatasetParameters> datasetParameters;

    public String getAggregation() {
        return getIfSerialized(aggregation);
    }

    public AbstractDatasetOutput setAggrgation(OptionalOutput<String> aggregation) {
        this.aggregation = aggregation;
        return this;
    }

    public Boolean getMobile() {
        return getIfSerialized(mobile);
    }

    public AbstractDatasetOutput setMobile(OptionalOutput<Boolean> mobile) {
        this.mobile = mobile;
        return this;
    }

    public Boolean getInsitu() {
        return getIfSerialized(insitu);
    }

    public AbstractDatasetOutput setInsitu(OptionalOutput<Boolean> insitu) {
        this.insitu = insitu;
        return this;
    }

    public String getUom() {
        return getIfSerialized(uom);
    }

    public AbstractDatasetOutput setUom(OptionalOutput<String> uom) {
        this.uom = uom;
        return this;
    }

    /**
     * @return the originTimezone
     */
    public OptionalOutput<String> getOriginTimezone() {
        return originTimezone;
    }

    /**
     * @param originTimezone the originTimezone to set
     */
    public void setOriginTimezone(OptionalOutput<String> originTimezone) {
        this.originTimezone = originTimezone;
    }

    public DatasetParameters getDatasetParameters(boolean forced) {
        return getIfSet(datasetParameters, forced);
    }

    public AbstractDatasetOutput setDatasetParameters(OptionalOutput<DatasetParameters> parameters) {
        this.datasetParameters = parameters;
        return this;
    }

    @JsonProperty("parameters")
    public DatasetParameters getDatasetParameters() {
        return getDatasetParameters(false);
    }
}
