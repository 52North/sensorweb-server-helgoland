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
package org.n52.io.format;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class FlotData {

    private List<Number[]> values;

    private Map<String, FlotData> referenceValues;

    private Number[] valueBeforeTimespan;

    private Number[] valueAfterTimespan;

    public FlotData() {
        referenceValues = new HashMap<>();
    }

    public List<Number[]> getValues() {
        return values;
    }

    public void setValues(List<Number[]> values) {
        this.values = values;
    }

    @JsonInclude(Include.NON_EMPTY)
    public Map<String, FlotData> getReferenceValues() {
        return referenceValues;
    }

    public void setReferenceValues(Map<String, FlotData> referenceValues) {
        this.referenceValues = referenceValues;
    }

    public void addReferenceValues(String id, FlotData toAdd) {
        this.referenceValues.put(id, toAdd);
    }

    public Number[] getValueBeforeTimespan() {
        return valueBeforeTimespan != null
                ? Arrays.copyOf(valueBeforeTimespan, valueBeforeTimespan.length)
                : null;
    }

    public void setValueBeforeTimespan(Number[] valueBeforeTimespan) {
        this.valueBeforeTimespan = valueBeforeTimespan != null
                ? Arrays.copyOf(valueBeforeTimespan, valueBeforeTimespan.length)
                : null;
    }

    public Number[] getValueAfterTimespan() {
        return valueAfterTimespan != null
                ? Arrays.copyOf(valueAfterTimespan, valueAfterTimespan.length)
                : null;
    }

    public void setValueAfterTimespan(Number[] valueAfterTimespan) {
        this.valueAfterTimespan = valueAfterTimespan != null
                ? Arrays.copyOf(valueAfterTimespan, valueAfterTimespan.length)
                : null;
    }

}
