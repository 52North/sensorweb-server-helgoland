/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
import java.util.Set;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OptionalOutput;
import org.n52.io.response.ParameterOutput;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetOutput<V extends AbstractValue< ? >> extends ParameterOutput {

    public static final String VALUE_TYPE = "valuetype";
    public static final String PLATFORM_TYPE = "platformtype";
    public static final String DATASET_PARAMETERS = "parameters";
    public static final String REFERENCE_VALUES = "referencevalues";
    public static final String FIRST_VALUE = "firstvalue";
    public static final String LAST_VALUE = "lastvalue";
    public static final String UOM = "uom";
    public static final String STATION_CLASSIFICATION = "stationClassification";
    public static final String PRIMARY_DATA = "primaryData";
    public static final String AGGREGATION_TYPES = "aggregationTypess";

    private OptionalOutput<String> valueType;

    private OptionalOutput<String> platformType;

    private OptionalOutput<DatasetParameters> datasetParameters;

    private OptionalOutput<List<ReferenceValueOutput<V>>> referenceValues;

    private OptionalOutput<V> firstValue;

    private OptionalOutput<V> lastValue;

    private OptionalOutput<String> uom;

    private OptionalOutput<String> stationClassification;

    private OptionalOutput<String> primaryData;

    private OptionalOutput<Set<String>> aggregationTypes;

    protected DatasetOutput() {
        // use static constructor method
    }

    public static <V extends AbstractValue< ? >> DatasetOutput<V> create(String type, IoParameters params) {
        DatasetOutput<V> output = new DatasetOutput<>();
        output.setValue(VALUE_TYPE, type, params, output::setValueType);
        return output;
    }

    @Override
    public DatasetOutput<V> setId(String id) {
        String type = getIfSet(valueType, true);
        super.setId(ValueType.createId(type, id));
        return this;
    }

    public String getValueType() {
        return getIfSerialized(valueType);
    }

    protected void setValueType(OptionalOutput<String> valueType) {
        this.valueType = valueType;
    }

    public String getPlatformType() {
        return getIfSerialized(platformType);
    }

    public DatasetOutput<V> setPlatformType(OptionalOutput<String> platformType) {
        this.platformType = platformType;
        return this;
    }

    @JsonProperty("parameters")
    public DatasetParameters getDatasetParameters() {
        return getDatasetParameters(false);
    }

    public DatasetParameters getDatasetParameters(boolean forced) {
        return getIfSet(datasetParameters, forced);
    }

    public DatasetOutput<V> setDatasetParameters(OptionalOutput<DatasetParameters> parameters) {
        this.datasetParameters = parameters;
        return this;
    }

    public String getUom() {
        return getIfSerialized(uom);
    }

    public DatasetOutput<V> setUom(OptionalOutput<String> uom) {
        this.uom = uom;
        return this;
    }

    public String getStationClassification() {
        return getIfSerialized(stationClassification);
    }

    public void setStationClassification(OptionalOutput<String> stationClassification) {
        this.stationClassification = stationClassification;
    }

    @JsonIgnore
    public boolean isSetStationClassification() {
        return isSet(stationClassification) && stationClassification.isSerialize();
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

    /**
     * @return the primaryData
     */
    public String getPrimaryData() {
        return getIfSerialized(primaryData);
    }

    /**
     * @param primaryData the primaryData to set
     */
    public void setPrimaryData(OptionalOutput<String> primaryData) {
        this.primaryData = primaryData;
    }

    @JsonIgnore
    public boolean isSetPrimaryData() {
        return isSet(primaryData) && primaryData.isSerialize();
    }

    /**
     * @return the aggregationTypes
     */
    public Set<String> getAggregationTypes() {
        return getIfSerialized(aggregationTypes);
    }

    /**
     * @param aggregationTypes the aggregationTypes to set
     */
    public void setAggregationTypes(OptionalOutput<Set<String>> aggregationTypes) {
        this.aggregationTypes = aggregationTypes;
    }

    @JsonIgnore
    public boolean isSetAggregationTypes() {
        return isSet(aggregationTypes) && aggregationTypes.isSerialize();
    }
}
