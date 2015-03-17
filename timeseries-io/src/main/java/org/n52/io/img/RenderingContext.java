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
package org.n52.io.img;

import java.text.SimpleDateFormat;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.IoFactory;
import org.n52.io.IoHandler;
import org.n52.io.IoParameters;
import org.n52.io.v1.data.DesignedParameterSet;
import org.n52.io.v1.data.StyleProperties;
import org.n52.io.v1.data.TimeseriesMetadataOutput;

public final class RenderingContext {

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
     * @return a rendering context to be used by {@link IoFactory} to create an {@link IoHandler}.
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

    public static RenderingContext createContextForSingleTimeseries(TimeseriesMetadataOutput metadata,
                                                                    IoParameters ioConfig) {
        DesignedParameterSet parameters = ioConfig.toDesignedParameterSet();
        parameters.addTimeseriesWithStyleOptions(metadata.getId(), ioConfig.getStyle());
        return createContextWith(parameters, metadata);
    }

    public void setDimensions(ChartDimension dimension) {
        chartStyleDefinitions.setWidth(dimension.getWidth());
        chartStyleDefinitions.setHeight(dimension.getHeight());
    }

    public DesignedParameterSet getChartStyleDefinitions() {
        return chartStyleDefinitions;
    }

    public TimeseriesMetadataOutput[] getTimeseriesMetadatas() {
        return timeseriesMetadatas.clone();
    }

    public String getTimeAxisFormat() {
        if (chartStyleDefinitions.containsParameter("timeaxis.format")) {
            return chartStyleDefinitions.getAsString("timeaxis.format");
        } else {
            return "yyyy-MM-dd, HH:mm";
        }
    }

}
