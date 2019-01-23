/**
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.io.v1.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class TimeseriesDataMetadata implements Serializable {

    private static final long serialVersionUID = 7422416308386483575L;

    private Map<String, TimeseriesData> referenceValues = new HashMap<>();

    private TimeseriesValue valueBeforeTimespan;

    private TimeseriesValue valueAfterTimespan;

    public Map<String, TimeseriesData> getReferenceValues() {
        return referenceValues == null
                ? Collections.<String, TimeseriesData>emptyMap()
                : referenceValues;
    }

    public boolean hasReferenceValues() {
        return (referenceValues != null) && !referenceValues.isEmpty();
    }

    public void setReferenceValues(Map<String, TimeseriesData> referenceValues) {
        this.referenceValues = referenceValues;
    }

    @JsonInclude(Include.ALWAYS)
    public TimeseriesValue getValueBeforeTimespan() {
        return valueBeforeTimespan;
    }

    public void setValueBeforeTimespan(TimeseriesValue valueBeforeTimespan) {
        this.valueBeforeTimespan = valueBeforeTimespan;
    }

    @JsonInclude(Include.ALWAYS)
    public TimeseriesValue getValueAfterTimespan() {
        return valueAfterTimespan;
    }

    public void setValueAfterTimespan(TimeseriesValue valueAfterTimespan) {
        this.valueAfterTimespan = valueAfterTimespan;
    }

}
