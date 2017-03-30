/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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

    private static int DEFAULT_WIDTH = 800;

    private static int DEFAULT_HEIGHT = 500;

    private static boolean DEFAULT_GRID = true;

    private static boolean DEFAULT_LEGEND = false;

    // TODO lean at revised prerendering config

    /**
     * Style options for each timeseriesId of interest.
     */
    @JsonProperty(required = true)
    private Map<String, StyleProperties> styleOptions;

    /**
     * Creates an instance with non-null default values.
     */
    public RequestStyledParameterSet() {
        styleOptions = new HashMap<>();
    }

    public int getWidth() {
        return getAsInt("width", DEFAULT_WIDTH);
    }

    /**
     * @param width the image width to set.
     */
    public void setWidth(int width) {
        width = width < 0 ? DEFAULT_WIDTH : width;
        setParameter("width", width);
    }

    /**
     * @return the requested height.
     */
    public int getHeight() {
        return getAsInt("height", DEFAULT_HEIGHT);
    }

    public void setHeight(int height) {
        height = height < 0 ? DEFAULT_HEIGHT : height;
        setParameter("height", height);
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
        setParameter("grid", grid);
    }

    /**
     * @return <code>true</code> if charts shall be rendered on a grid, <code>false</code> otherwise.
     */
    public boolean isGrid() {
        return getAsBoolean("grid", DEFAULT_GRID);
    }

    public boolean isLegend() {
        return getAsBoolean("legend", DEFAULT_LEGEND);
    }

    public void setLegend(boolean legend) {
        setParameter("legend", legend);
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
