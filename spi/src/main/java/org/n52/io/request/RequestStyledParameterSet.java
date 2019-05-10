/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POST request body used for serialization.
 */
public class RequestStyledParameterSet extends RequestParameterSet {

    /**
     * Style options for each timeseriesId of interest.
     */
    private Map<String, StyleProperties> styleOptions;

    /**
     * Creates an instance with non-null default values.
     */
    RequestStyledParameterSet(@JsonProperty(value = "styleOptions",
        required = true) Map<String, StyleProperties> styleOptions) {
        this.styleOptions = styleOptions;
    }

    public int getWidth() {
        return getAsInt(Parameters.WIDTH, Parameters.DEFAULT_WIDTH);
    }

    /**
     * @param width
     *        the image width to set.
     */
    public void setWidth(int width) {
        int w = width < 0
                ? Parameters.DEFAULT_WIDTH
                : width;
        setParameter(Parameters.WIDTH, w);
    }

    /**
     * @return the requested height.
     */
    public int getHeight() {
        return getAsInt(Parameters.HEIGHT, Parameters.DEFAULT_HEIGHT);
    }

    public void setHeight(int height) {
        int h = height < 0
                ? Parameters.DEFAULT_HEIGHT
                : height;
        setParameter(Parameters.HEIGHT, h);
    }

    @Override
    public String[] getDatasets() {
        Set<String> datasetIds = styleOptions.keySet();
        return datasetIds.toArray(new String[0]);
    }

    /**
     * @param grid
     *        <code>true</code> if charts shall be rendered on a grid, <code>false</code> otherwise.
     */
    public void setGrid(boolean grid) {
        setParameter(Parameters.GRID, grid);
    }

    /**
     * @return <code>true</code> if charts shall be rendered on a grid, <code>false</code> otherwise.
     */
    public boolean isGrid() {
        return getAsBoolean(Parameters.GRID, Parameters.DEFAULT_GRID);
    }

    public boolean isLegend() {
        return getAsBoolean(Parameters.LEGEND, Parameters.DEFAULT_LEGEND);
    }

    public void setLegend(boolean legend) {
        setParameter(Parameters.LEGEND, legend);
    }

    public String getTimeFormat() {
        return getAsString(Parameters.TIME_FORMAT, Parameters.DEFAULT_TIME_FORMAT);
    }

    public StyleProperties getStyleProperties(String timeseriesId) {
        return styleOptions.get(timeseriesId);
    }

    public StyleProperties getReferenceDatasetStyleOptions(String timeseriesId, String referenceSeriesId) {
        if (!styleOptions.containsKey(timeseriesId)) {
            return null;
        }
        StyleProperties styleProperties = getStyleProperties(timeseriesId);
        Map<String, StyleProperties> properties = styleProperties.getReferenceValueStyleProperties();
        return properties.containsKey(referenceSeriesId)
                ? properties.get(referenceSeriesId)
                : null;
    }

    @Override
    public IoParameters toParameters() {
        setParameter(Parameters.STYLES, styleOptions);
        return super.toParameters();
    }

}
