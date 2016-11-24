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
package org.n52.io.request;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a parameter object to request a rendered chart output from
 * multiple timeseries.
 */
public class RequestStyledParameterSet extends RequestParameterSet {

    // TODO lean at revised prerendering config

    /**
     * Style options for each timeseriesId of interest.
     */
    @JsonProperty(required = true)
    private Map<String, StyleProperties> styleOptions;

    private int width = 800;

    private int height = 500;

    private boolean grid = true;

    private boolean legend = false;

    /**
     * Creates an instance with non-null default values.
     */
    public RequestStyledParameterSet() {
        styleOptions = new HashMap<>();
    }

    /**
     * @return the requested width or negative number if no size was set.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the image width to set.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the requested height or negative number if no size was set.
     */
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String[] getDatasets() {
        return styleOptions.keySet().toArray(new String[0]);
    }

    /**
     * @param grid <code>true</code> if charts shall be rendered on a grid,
     * <code>false</code> otherwise.
     */
    public void setGrid(boolean grid) {
        this.grid = grid;
    }

    /**
     * @return <code>true</code> if charts shall be rendered on a grid,
     * <code>false</code> otherwise.
     */
    public boolean isGrid() {
        return grid;
    }

    public boolean isLegend() {
        return legend;
    }

    public void setLegend(boolean legend) {
        this.legend = legend;
    }

    public void setStyleOptions(Map<String, StyleProperties> renderingOptions) {
        this.styleOptions = renderingOptions;
    }

    public StyleProperties getStyleOptions(String timeseriesId) {
        return styleOptions.get(timeseriesId);
    }

    public StyleProperties getReferenceSeriesStyleOptions(String timeseriesId, String referenceSeriesId) {
        if (!styleOptions.containsKey(timeseriesId)) {
            return null;
        }
        StyleProperties styleProperties = styleOptions.get(timeseriesId);
        Map<String, StyleProperties> properties = styleProperties.getReferenceValueStyleProperties();
        return properties.containsKey(referenceSeriesId)
                ? properties.get(referenceSeriesId)
                : null;
    }

    public void addSeriesWithStyleOptions(String timeseriesId, StyleProperties styleOptions) {
        this.styleOptions.put(timeseriesId, styleOptions);
    }

}
