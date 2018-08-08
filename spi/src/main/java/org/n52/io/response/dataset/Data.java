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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Data<V extends AbstractValue< ? >> implements Serializable {

    private static final long serialVersionUID = 3119211667773416585L;

    private final List<V> values = new ArrayList<>();

    private final DatasetMetadata<Data<V>> metadata;

    public Data() {
        this(new DatasetMetadata<>());
    }

    public Data(DatasetMetadata<Data<V>> metadata) {
        this.metadata = metadata;
    }

    public void addValues(final V[] toAdd) {
        if ((toAdd != null) && (toAdd.length > 0)) {
            this.values.addAll(Arrays.asList(toAdd));
        }
    }

    public Data<V> addNewValue(final V value) {
        this.values.add(value);
        return this;
    }

    public Data<V> addData(Data<V> toAdd) {
        Data<V> data = new Data<>(metadata);
        data.values.addAll(Stream.concat(values.stream(), toAdd.values.stream())
                                 .collect(Collectors.toList()));
        return data;
    }

    /**
     * @return a sorted list of quantity values.
     */
    // TODO @JsonSerialize may not be needed anymore from jackson 2.9.6
    // https://github.com/FasterXML/jackson-databind/issues/1964#issuecomment-382877148
    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    public List<V> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public long size() {
        return this.values.size();
    }

    @JsonProperty("extra")
    public DatasetMetadata<Data<V>> getMetadata() {
        return this.metadata;
    }

    @JsonIgnore
    public boolean hasReferenceValues() {
        return (metadata != null)
                && (metadata.getReferenceValues() != null)
                && !metadata.getReferenceValues().isEmpty();
    }

}
