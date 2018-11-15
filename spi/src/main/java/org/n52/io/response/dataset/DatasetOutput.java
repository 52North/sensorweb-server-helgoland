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

import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OptionalOutput;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DatasetOutput<V extends AbstractValue< ? >> extends AbstractDatasetOutput {

    public static final String COLLECTION_PATH = "datasets";

    public static final String DATASET_TYPE = "datasettype";
    public static final String VALUE_TYPE = "valuetype";
    public static final String REFERENCE_VALUES = "referencevalues";
    public static final String FIRST_VALUE = "firstvalue";
    public static final String LAST_VALUE = "lastvalue";

    private OptionalOutput<String> datasetType;

    private OptionalOutput<String> valueType;

    private OptionalOutput<List<ReferenceValueOutput<V>>> referenceValues;

    private OptionalOutput<V> firstValue;

    private OptionalOutput<V> lastValue;

    protected DatasetOutput() {
        // use static constructor method
    }

    @Override
    protected String getCollectionName() {
        return COLLECTION_PATH;
    }

    public static <V extends AbstractValue< ? >> DatasetOutput<V> create(IoParameters params) {
        DatasetOutput<V> output = new DatasetOutput<>();
        return output;
    }

    public String getDatasetType() {
        return getIfSerialized(datasetType);
    }

    public void setDatasetType(OptionalOutput<String> datasetType) {
        this.datasetType = datasetType;
    }

    public String getValueType() {
        return getIfSerialized(valueType);
    }

    public void setValueType(OptionalOutput<String> valueType) {
        this.valueType = valueType;
    }

    // TODO @JsonSerialize may not be needed anymore from jackson 2.9.6
    // https://github.com/FasterXML/jackson-databind/issues/1964#issuecomment-382877148
    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    public V getFirstValue() {
        return getIfSerialized(firstValue);
    }

    public DatasetOutput<V> setFirstValue(OptionalOutput<V> firstValue) {
        this.firstValue = firstValue;
        return this;
    }

    // TODO @JsonSerialize may not be needed anymore from jackson 2.9.6
    // https://github.com/FasterXML/jackson-databind/issues/1964#issuecomment-382877148
    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
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
