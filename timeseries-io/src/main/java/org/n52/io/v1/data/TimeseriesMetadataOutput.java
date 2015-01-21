/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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

import java.util.ArrayList;
import java.util.List;

import org.n52.io.Utils;

public class TimeseriesMetadataOutput extends ParameterOutput {

    private String uom;

    private StationOutput station;

    private ReferenceValueOutput[] referenceValues;

    private TimeseriesValue firstValue;

    private TimeseriesValue lastValue;

    private TimeseriesOutput parameters;

    private StyleProperties renderingHints;

    private StatusInterval[] statusIntervals;

    private List<Object> extras;

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public StationOutput getStation() {
        return station;
    }

    public Object[] getExtras() {
        if (extras != null) {
            return extras.toArray();
        }
        return null;
    }

    public void addExtra(Object extra) {
        if (extras == null) {
            extras = new ArrayList<Object>();
        }
        extras.add(extra);
    }

    public void setStation(StationOutput station) {
        this.station = station;
    }

    public ReferenceValueOutput[] getReferenceValues() {
        return Utils.copy(referenceValues);
    }

    public void setReferenceValues(ReferenceValueOutput[] referenceValues) {
        this.referenceValues = Utils.copy(referenceValues);
    }

    public TimeseriesValue getFirstValue() {
        return firstValue;
    }

    public void setFirstValue(TimeseriesValue firstValue) {
        this.firstValue = firstValue;
    }

    public TimeseriesValue getLastValue() {
        return lastValue;
    }

    public void setLastValue(TimeseriesValue lastValue) {
        this.lastValue = lastValue;
    }

    public TimeseriesOutput getParameters() {
        return parameters;
    }

    public void setParameters(TimeseriesOutput timeseries) {
        this.parameters = timeseries;
    }

    public StyleProperties getRenderingHints() {
        return this.renderingHints;
    }

    public void setRenderingHints(StyleProperties renderingHints) {
        this.renderingHints = renderingHints;
    }

    public StatusInterval[] getStatusIntervals() {
        return statusIntervals;
    }

    public void setStatusIntervals(StatusInterval[] statusIntervals) {
        this.statusIntervals = statusIntervals;
    }

}
