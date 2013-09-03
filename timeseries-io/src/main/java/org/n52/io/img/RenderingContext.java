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

package org.n52.io.img;

import org.n52.io.IOFactory;
import org.n52.io.IOHandler;
import org.n52.io.v1.data.DesignedParameterSet;
import org.n52.io.v1.data.StyleProperties;
import org.n52.io.v1.data.TimeseriesMetadataOutput;

public class RenderingContext {

    private DesignedParameterSet chartStyleDefinitions;

    private TimeseriesMetadataOutput[] timeseriesMetadatas;

    // use static constructors
    private RenderingContext(DesignedParameterSet timeseriesStyles, TimeseriesMetadataOutput[] timeseriesMetadatas) {
        this.timeseriesMetadatas = timeseriesMetadatas == null ? new TimeseriesMetadataOutput[0] : timeseriesMetadatas;
        this.chartStyleDefinitions = timeseriesStyles;
    }

    public static RenderingContext createEmpty() {
        return new RenderingContext(new DesignedParameterSet(), new TimeseriesMetadataOutput[0]);
    }

    /**
     * @param timeseriesStyles
     *        the style definitions for each timeseries.
     * @param timeseriesMetadatas
     *        the metadata for each timeseries.
     * @throws NullPointerException
     *         if any of the given arguments is <code>null</code>.
     * @throws IllegalStateException
     *         if amount of timeseries described by the given arguments is not in sync.
     * @return a rendering context to be used by {@link IOFactory} to create an {@link IOHandler}.
     */
    public static RenderingContext createContextWith(DesignedParameterSet timeseriesStyles,
                                                     TimeseriesMetadataOutput... timeseriesMetadatas) {
        if (timeseriesStyles == null || timeseriesMetadatas == null) {
            throw new NullPointerException("Designs and metadatas cannot be null.!");
        }
        String[] timeseriesIds = timeseriesStyles.getTimeseries();
        if (timeseriesIds.length != timeseriesMetadatas.length) {
            int amountTimeseries = timeseriesIds.length;
            int amountMetadatas = timeseriesMetadatas.length;
            StringBuilder sb = new StringBuilder();
            sb.append("Size of designs and metadatas do not match: ");
            sb.append("#Timeseries: ").append(amountTimeseries).append(" vs. ");
            sb.append("#Metadatas: ").append(amountMetadatas);
            throw new IllegalStateException(sb.toString());
        }
        return new RenderingContext(timeseriesStyles, timeseriesMetadatas);
    }
    
    public static RenderingContext createContextForSingleTimeseries(TimeseriesMetadataOutput metadata, StyleProperties style, String timespan) {
        DesignedParameterSet parameters = new DesignedParameterSet();
        parameters.addTimeseriesWithStyleOptions(metadata.getId(), style);
        parameters.setTimespan(timespan);
        return createContextWith(parameters, metadata);
    }
    
    public void setDimensions(int width, int height) {
        chartStyleDefinitions.setWidth(width);
        chartStyleDefinitions.setHeight(height);
    }

    public DesignedParameterSet getChartStyleDefinitions() {
        return chartStyleDefinitions;
    }

    public TimeseriesMetadataOutput[] getTimeseriesMetadatas() {
        return timeseriesMetadatas;
    }

}
