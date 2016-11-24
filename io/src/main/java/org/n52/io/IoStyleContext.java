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
package org.n52.io;

import java.util.Collections;
import java.util.List;

import org.n52.io.measurement.MeasurementIoFactory;
import org.n52.io.measurement.img.ChartDimension;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.dataset.DatasetOutput;

public final class IoStyleContext {

    private final RequestStyledParameterSet chartStyleDefinitions;

    private final List<? extends DatasetOutput> seriesMetadatas;

    public IoStyleContext() {
        this.chartStyleDefinitions = new RequestStyledParameterSet();
        this.seriesMetadatas = Collections.<DatasetOutput>emptyList();
    }

    // use static constructors
    private IoStyleContext(RequestStyledParameterSet timeseriesStyles, List<? extends DatasetOutput> metadatas) {
        this.seriesMetadatas = metadatas.isEmpty()
                ? Collections.<DatasetOutput>emptyList()
                : metadatas;
        this.chartStyleDefinitions = timeseriesStyles;
    }

    public static IoStyleContext createEmpty() {
        List<? extends DatasetOutput> emptyList = Collections.emptyList();
        return new IoStyleContext(new RequestStyledParameterSet(), emptyList);
    }

    /**
     * @param styles the style definitions.
     * @param metadatas the metadata for each timeseries.
     * @throws NullPointerException if any of the given arguments is
     * <code>null</code>.
     * @throws IllegalStateException if amount of timeseries described by the
     * given arguments is not in sync.
     * @return a rendering context to be used by {@link MeasurementIoFactory} to create an
     * {@link IoHandler}.
     */
    public static IoStyleContext createContextWith(RequestStyledParameterSet styles, List<? extends DatasetOutput> metadatas) {
        if (styles == null || metadatas == null) {
            throw new NullPointerException("Designs and metadatas cannot be null.!");
        }
        String[] seriesIds = styles.getDatasets();
        if (seriesIds.length != metadatas.size()) {
            int amountTimeseries = seriesIds.length;
            int amountMetadatas = metadatas.size();
            StringBuilder sb = new StringBuilder();
            sb.append("Size of designs and metadatas do not match: ");
            sb.append("#Timeseries: ").append(amountTimeseries).append(" vs. ");
            sb.append("#Metadatas: ").append(amountMetadatas);
            throw new IllegalStateException(sb.toString());
        }
        return new IoStyleContext(styles, metadatas);
    }

    public static IoStyleContext createContextForSingleSeries(DatasetOutput metadata, IoParameters ioConfig) {
        RequestStyledParameterSet parameters = ioConfig.toRequestStyledParameterSet();
        parameters.addSeriesWithStyleOptions(metadata.getId(), ioConfig.getStyle());
        return createContextWith(parameters, Collections.singletonList(metadata));
    }

    public void setDimensions(ChartDimension dimension) {
        chartStyleDefinitions.setWidth(dimension.getWidth());
        chartStyleDefinitions.setHeight(dimension.getHeight());
    }

    public RequestStyledParameterSet getChartStyleDefinitions() {
        return chartStyleDefinitions;
    }

    public List<? extends DatasetOutput> getSeriesMetadatas() {
        return seriesMetadatas;
    }

    public String getTimeAxisFormat() {
        if (chartStyleDefinitions.containsParameter("timeaxis.format")) {
            return chartStyleDefinitions.getAsString("timeaxis.format");
        } else {
            return "yyyy-MM-dd, HH:mm";
        }
    }

}
