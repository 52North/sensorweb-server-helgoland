/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Holds some metadata about a whole data container. Examples are {@link #referenceValues} or values which
 * indicate the first value falling beyond the upper or lower time range bound requested
 * ({@link #valueAfterTimespan} or {@link #valueBeforeTimespan}).
 *
 * @param <T>
 *        the data type
 */
public class DatasetMetadata<T extends AbstractValue< ? >> implements Serializable {

    private static final long serialVersionUID = -2670379436251511249L;

    private Map<String, Data<T>> referenceValues = new HashMap<>();

    private T valueBeforeTimespan;

    private T valueAfterTimespan;

    public boolean hasReferenceValues() {
        return (referenceValues != null) && !referenceValues.isEmpty();
    }

    public Map<String, Data<T>> getReferenceValues() {
        return referenceValues;
    }

    public void setReferenceValues(final Map<String, Data<T>> referenceValues) {
        this.referenceValues = referenceValues;
    }

    /**
     * @return the value before to the lower timespan bounds
     */
    @JsonInclude(Include.ALWAYS)
    public T getValueBeforeTimespan() {
        return valueBeforeTimespan;
    }

    /**
     * @param valueBeforeTimespan
     *        sets the value before to the lower timespan bounds
     */
    public void setValueBeforeTimespan(final T valueBeforeTimespan) {
        this.valueBeforeTimespan = valueBeforeTimespan;
    }

    public boolean hasValueBeforeTimespan() {
        return valueBeforeTimespan != null;
    }

    /**
     * @return the value after to the upper timespan bounds
     */
    @JsonInclude(Include.ALWAYS)
    public T getValueAfterTimespan() {
        return valueAfterTimespan;
    }

    /**
     * @param valueBeforeTimespan
     *        sets the value after to the upper timespan bounds
     */
    public void setValueAfterTimespan(final T valueBeforeTimespan) {
        this.valueAfterTimespan = valueBeforeTimespan;
    }

    public boolean hasValueAfterTimespan() {
        return valueAfterTimespan != null;
    }

}
