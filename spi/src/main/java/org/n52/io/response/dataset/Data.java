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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Data<T extends AbstractValue<?>> implements Serializable {

    private static final long serialVersionUID = 3119211667773416585L;

    private List<T> values = new ArrayList<>();

    public void addValues(T... toAdd) {
        if (toAdd != null && toAdd.length > 0) {
            this.values.addAll(Arrays.asList(toAdd));
        }
    }

    public void setValues(T[] values) {
        this.values = Arrays.asList(values);
    }

    public void addNewValue(T value) {
        values.add(value);
    }

    /**
     * @return a sorted list of quantity values.
     */
    public List<T> getValues() {
        return Collections.unmodifiableList(values);
    }

    public long size() {
        return values.size();
    }

    @JsonIgnore
    public boolean hasReferenceValues() {
        return getMetadata() != null
                && getMetadata().getReferenceValues() != null;
    }

    @JsonProperty("extra")
    public abstract DatasetMetadata<?> getMetadata();


}
