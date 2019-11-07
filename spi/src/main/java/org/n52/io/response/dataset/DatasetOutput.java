/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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

import java.util.List;

import org.n52.io.response.AbstractOutput;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.OptionalOutput;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.TimeOutputConverter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DatasetOutput<V extends AbstractValue< ? >> extends AbstractOutput {

    public static final String COLLECTION_PATH = "datasets";

    public static final String DATASET_TYPE = "datasetType";
    public static final String OBSERVATION_TYPE = "observationType";
    public static final String VALUE_TYPE = "valueType";
    public static final String MOBILE = "mobile";
    public static final String INSITU = "insitu";
    public static final String UOM = "uom";
    public static final String DATASET_PARAMETERS = "parameters";
    public static final String ORIGIN_TIMEZONE = "originTimezone";
    public static final String SMAPLING_TIME_START = "samplingTimeStart";
    public static final String SMAPLING_TIME_END = "samplingTimeEnd";
    public static final String FEATURE = "feature";
    public static final String REFERENCE_VALUES = "referenceValues";
    public static final String FIRST_VALUE = "firstValue";
    public static final String LAST_VALUE = "lastValue";
    private OptionalOutput<String> datasetType;
    private OptionalOutput<String> observationType;
    private OptionalOutput<String> valueType;
    private OptionalOutput<Boolean> mobile;
    private OptionalOutput<Boolean> insitu;
    private OptionalOutput<String> uom;
    private OptionalOutput<String> originTimezone;
    private OptionalOutput<TimeOutput> samplingTimeStart;
    private OptionalOutput<TimeOutput> samplingTimeEnd;
    private OptionalOutput<FeatureOutput> feature;
    private OptionalOutput<DatasetParameters> datasetParameters;
    private OptionalOutput<List<ReferenceValueOutput<V>>> referenceValues;
    private OptionalOutput<V> firstValue;
    private OptionalOutput<V> lastValue;

    public DatasetOutput() {
    }

    public String getDatasetType() {
        return getIfSerialized(datasetType);
    }

    public void setDatasetType(OptionalOutput<String> datasetType) {
        this.datasetType = datasetType;
    }

    public String getObservationType() {
        return getIfSerialized(observationType);
    }

    public DatasetOutput<?> setObservationType(OptionalOutput<String> observationType) {
        this.observationType = observationType;
        return this;
    }

    public String getValueType() {
        return getIfSerialized(valueType);
    }

    public void setValueType(OptionalOutput<String> valueType) {
        this.valueType = valueType;
    }

    public Boolean getMobile() {
        return getIfSerialized(mobile);
    }

    public DatasetOutput<V> setMobile(OptionalOutput<Boolean> mobile) {
        this.mobile = mobile;
        return this;
    }

    public Boolean getInsitu() {
        return getIfSerialized(insitu);
    }

    public DatasetOutput<V> setInsitu(OptionalOutput<Boolean> insitu) {
        this.insitu = insitu;
        return this;
    }

    public String getUom() {
        return getIfSerialized(uom);
    }

    public DatasetOutput<?> setUom(OptionalOutput<String> uom) {
        this.uom = uom;
        return this;
    }

    public String getOriginTimezone() {
        return getIfSerialized(originTimezone);
    }

    public void setOriginTimezone(OptionalOutput<String> originTimezone) {
        this.originTimezone = originTimezone;
    }

    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getSamplingTimeStart() {
        return getIfSerialized(samplingTimeStart);
    }

    public void setSamplingTimeStart(OptionalOutput<TimeOutput> samplingTimeStart) {
        this.samplingTimeStart = samplingTimeStart;
    }

    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getSamplingTimeEnd() {
        return getIfSerialized(samplingTimeEnd);
    }

    public void setSamplingTimeEnd(OptionalOutput<TimeOutput> samplingTimeEnd) {
        this.samplingTimeEnd = samplingTimeEnd;
    }

    public FeatureOutput getFeature() {
        return getIfSerialized(feature);
    }

    public void setFeature(OptionalOutput<FeatureOutput> feature) {
        this.feature = feature;
    }

    public DatasetParameters getDatasetParameters(boolean forced) {
        return getIfSet(datasetParameters, forced);
    }

    @JsonProperty("parameters")
    public DatasetParameters getDatasetParameters() {
        return getDatasetParameters(false);
    }

    public DatasetOutput<?> setDatasetParameters(OptionalOutput<DatasetParameters> parameters) {
        this.datasetParameters = parameters;
        return this;
    }

    @Override
    protected String getCollectionName() {
        return COLLECTION_PATH;
    }

    public V getFirstValue() {
        return getIfSerialized(firstValue);
    }

    public DatasetOutput<V> setFirstValue(OptionalOutput<V> firstValue) {
        this.firstValue = firstValue;
        return this;
    }

    public V getLastValue() {
        return getIfSerialized(lastValue);
    }

    public DatasetOutput<V> setLastValue(OptionalOutput<V> lastValue) {
        this.lastValue = lastValue;
        return this;
    }

    public List<ReferenceValueOutput<V>> getReferenceValues() {
        return getIfSerializedCollection(referenceValues);
    }

    public DatasetOutput<V> setReferenceValues(OptionalOutput<List<ReferenceValueOutput<V>>> referenceValues) {
        this.referenceValues = referenceValues;
        return this;
    }
}
