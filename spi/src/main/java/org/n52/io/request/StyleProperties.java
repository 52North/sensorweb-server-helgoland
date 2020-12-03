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
package org.n52.io.request;

import java.util.HashMap;
import java.util.Map;

/**
 * The chart style options for a timeseries.
 */
public final class StyleProperties {

    /**
     * The chart type, e.g. <code>line</code>, <code>bar</code>, ...
     */
    private String chartType = "line";

    private Map<String, String> properties = new HashMap<>();

    private Map<String, StyleProperties> referenceValueStyleProperties = new HashMap<>();

    private StyleProperties() {
        // use static constructor
    }

    /**
     * @return the chart type, e.g. <code>line</code>, or <code>bar</code>.
     */
    public String getChartType() {
        return chartType;
    }

    public void setChartType(String type) {
        this.chartType = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, StyleProperties> getReferenceValueStyleProperties() {
        return referenceValueStyleProperties;
    }

    public void setReferenceValueStyleProperties(Map<String, StyleProperties> referenceValueStyleProperties) {
        this.referenceValueStyleProperties = referenceValueStyleProperties;
    }

    public static StyleProperties createDefaults() {
        return new StyleProperties();
    }

}
