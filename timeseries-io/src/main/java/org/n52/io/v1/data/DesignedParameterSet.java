/**
 * ﻿Copyright (C) 2013
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.n52.io.IoParseException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a parameter object to request a rendered chart output from multiple timeseries.
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
     * Indicates if a legend shall be drawn on the chart.
     */
    private boolean legend = false;

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
     * @param grid
     *        <code>true</code> if charts shall be rendered on a grid, <code>false</code> otherwise.
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
        try {
            if ( !styleOptions.containsKey(timeseriesId)) {
                return null;
            }
            StyleProperties styleProperties = styleOptions.get(timeseriesId);
            Map<String, String> properties = styleProperties.getProperties();
            return properties.containsKey(referenceSeriesId)
                ? new ObjectMapper().readValue(properties.get(referenceSeriesId), StyleProperties.class)
                : null;
        }
        catch (JsonMappingException e) {
            throw new IoParseException("Unable to read style properties for reference series: " + referenceSeriesId, e);
        }
        catch (JsonParseException e) {
            throw new IoParseException("Could not parse style properties.", e);
        }
        catch (IOException e) {
            throw new IoParseException("Could handle I/O operations while parsing JSON properties.", e);
        }
    }

    public void addTimeseriesWithStyleOptions(String timeseriesId, StyleProperties styleOptions) {
        this.styleOptions.put(timeseriesId, styleOptions);
    }

}
