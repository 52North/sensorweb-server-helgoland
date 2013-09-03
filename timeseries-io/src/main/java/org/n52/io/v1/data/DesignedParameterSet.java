/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.io.v1.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a parameter object to request data from multiple timeseries.
 */
public class DesignedParameterSet extends ParameterSet {
    
    // XXX refactor ParameterSet, DesignedParameterSet, UndesingedParameterSet and QueryMap
    
    /**
     * The width of the chart image to render.
     */
    private int width = 800;

    /**
     * The height of the chart image to render.
     */
    private int height = 500;

    /**
     * Indicates a grid as rendering background. <code>true</code> is the default.
     */
    private boolean grid = true;

    /**
     * A language code to determine the requested locale.
     */
    private String language;
    
    /**
     * Style options for each timeseriesId of interest.
     */
    @JsonProperty(required = true)
    private Map<String, StyleProperties> styleOptions;

    /**
     * Creates an instance with non-null default values.
     */
    public DesignedParameterSet() {
        styleOptions = new HashMap<String, StyleProperties>();
    }

    /**
     * @return the requested width or negative number if no size was set.
     */
    public int getWidth() {
        return width;
    }

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

    public String[] getTimeseries() {
        return styleOptions.keySet().toArray(new String[0]);
    }
    
    /**
     * @param grid <code>true</code> if charts shall be rendered on a grid, <code>false</code> otherwise.
     */
    public void setGrid(boolean grid) {
        this.grid = grid;
    }

    /**
     * @return <code>true</code> if charts shall be rendered on a grid, <code>false</code> otherwise.
     */
    public boolean isGrid() {
        return this.grid;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setStyleOptions(Map<String, StyleProperties> renderingOptions) {
        this.styleOptions = renderingOptions;
    }

    public StyleProperties getStyleOptions(String timeseriesId) {
        return styleOptions.get(timeseriesId);
    }

    public void addTimeseriesWithStyleOptions(String timeseriesId, StyleProperties styleOptions) {
        this.styleOptions.put(timeseriesId, styleOptions);
    }

}
