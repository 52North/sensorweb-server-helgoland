/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response.series.text;

import org.n52.io.Utils;
import org.n52.io.response.v1.ext.ObservationType;
import org.n52.io.response.v1.ext.DatasetOutput;

public class TextObservationSeriesOutput extends DatasetOutput<TextObservationReferenceValueOutput> {

    private String uom;

    private TextObservationReferenceValueOutput[] referenceValues;

    private TextObservationValue firstValue;

    private TextObservationValue lastValue;

    public TextObservationSeriesOutput() {
        super(ObservationType.TEXT);
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public TextObservationReferenceValueOutput[] getReferenceValues() {
        return Utils.copy(referenceValues);
    }

    public void setReferenceValues(TextObservationReferenceValueOutput[] referenceValues) {
        this.referenceValues = Utils.copy(referenceValues);
    }

    public TextObservationValue getFirstValue() {
        return firstValue;
    }

    public void setFirstValue(TextObservationValue firstValue) {
        this.firstValue = firstValue;
    }

    public TextObservationValue getLastValue() {
        return lastValue;
    }

    public void setLastValue(TextObservationValue lastValue) {
        this.lastValue = lastValue;
    }

}
