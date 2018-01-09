/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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

import org.n52.series.spi.srv.RawFormats;

/**
 * POST request body used for serialization.
 */
public class RequestSimpleParameterSet extends RequestParameterSet {

    private String rawFormat;

    RequestSimpleParameterSet() {
    }

    /**
     * @return the series ids
     */
    @Override
    public String[] getDatasets() {
        return getAsStringArray(Parameters.DATASETS, new String[0]);
    }

    /**
     * @param datasets
     *        The series ids of interest.
     */
    void setDatasets(String[] datasets) {
        setParameter(Parameters.DATASETS, IoParameters.getJsonNodeFrom(datasets));
    }

    @Deprecated
    void setTimeseries(String[] timeseries) {
        setDatasets(timeseries);
    }

    @Deprecated
    void setTimeseriesIds(String[] timeseriesIds) {
        setDatasets(timeseriesIds);
    }

    @Deprecated
    void setSeries(String[] series) {
        setDatasets(series);
    }

    @Deprecated
    void setSeriesIds(String[] seriesIds) {
        setDatasets(seriesIds);
    }

    /**
     * @return the output format the raw data shall have.
     */
    public String getFormat() {
        return getAsString(Parameters.FORMAT);
    }

    /**
     * @param format
     *        Which output format the raw data shall have.
     */
    public void setFormat(String format) {
        setParameter(Parameters.FORMAT, IoParameters.getJsonNodeFrom(format));
    }

    /**
     * @return the raw output format the raw data shall have.
     */
    public String getRawFormat() {
        if ((rawFormat == null || rawFormat.isEmpty())
                && containsParameter(RawFormats.RAW_FORMAT.toLowerCase())) {
            setRawFormat(getAsString(RawFormats.RAW_FORMAT.toLowerCase()));
        }
        return rawFormat;
    }

    /**
     * @param rawFormat
     *        Which raw output format the raw data shall have.
     */
    public void setRawFormat(String rawFormat) {
        this.rawFormat = rawFormat;
    }

    /**
     * @return <code>true</code> if rawFormat is set
     */
    public boolean isSetRawFormat() {
        return getRawFormat() != null && !getRawFormat().isEmpty();
    }

}
