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
package org.n52.io.response.sampling;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.n52.io.geojson.GeoJSONGeometrySerializer;
import org.n52.io.response.OptionalOutput;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.dataset.DatasetOutput;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class MeasuringProgramOutput extends ParameterOutput {

    public static final String COLLECTION_PATH = "measuringPrograms";
    public static final String ORDER_ID = "orderId";
    public static final String MEASURING_PROGRAM_TIME_START = "measuringProgramTimeStart";
    public static final String MEASURING_PROGRAM_TIME_END = "measuringProgramTimeEnd";
    public static final String OBSERVED_AREA = "observedArea";
    public static final String PRODUCER = "producer";
    public static final String SAMPLINGS = "samplings";
    public static final String DATASETS = "datasets";

    private OptionalOutput<String> orderId;
    private OptionalOutput<Long> measuringProgramTimeStart;
    private OptionalOutput<Long> measuringProgramTimeEnd;
    private OptionalOutput<Geometry> observedArea;
    private OptionalOutput<ProducerOutput> producer;

    private OptionalOutput<List<DatasetOutput<?>>> datasets;
    private OptionalOutput<List<SamplingOutput>> samplings;

    @Override
    protected String getCollectionName() {
        return COLLECTION_PATH;
    }

    /**
     * @return the orderId
     */
    public OptionalOutput<String> getOrderId() {
        return orderId;
    }

    /**
     * @param orderId the orderId to set
     */
    public void setOrderId(OptionalOutput<String> orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the measuringProgramTimeStart
     */
    public OptionalOutput<Long> getMeasuringProgramTimeStart() {
        return measuringProgramTimeStart;
    }

    /**
     * @param measuringProgramTimeStart the measuringProgramTimeStart to set
     */
    public void setMeasuringProgramTimeStart(OptionalOutput<Long> measuringProgramTimeStart) {
        this.measuringProgramTimeStart = measuringProgramTimeStart;
    }

    /**
     * @return the measuringProgramTimeEnd
     */
    public OptionalOutput<Long> getMeasuringProgramTimeEnd() {
        return measuringProgramTimeEnd;
    }

    /**
     * @param measuringProgramTimeEnd the measuringProgramTimeEnd to set
     */
    public void setMeasuringProgramTimeEnd(OptionalOutput<Long> measuringProgramTimeEnd) {
        this.measuringProgramTimeEnd = measuringProgramTimeEnd;
    }

    /**
     * @return the observedArea
     */
    @JsonSerialize(using = GeoJSONGeometrySerializer.class)
    public OptionalOutput<Geometry> getObservedArea() {
        return observedArea;
    }

    /**
     * @param observedArea the observedArea to set
     */
    public void setObservedArea(OptionalOutput<Geometry> observedArea) {
        this.observedArea = observedArea;
    }

    /**
     * @return the producer
     */
    public OptionalOutput<ProducerOutput> getProducer() {
        return producer;
    }

    /**
     * @param producer the producer to set
     */
    public void setProducer(OptionalOutput<ProducerOutput> producer) {
        this.producer = producer;
    }

    /**
     * @return the datasets
     */
    public OptionalOutput<List<DatasetOutput<?>>> getDatasets() {
        return datasets;
    }

    /**
     * @param datasets the datasets to set
     */
    public void setDatasets(OptionalOutput<List<DatasetOutput<?>>> datasets) {
        this.datasets = datasets;
    }

    /**
     * @return the samplings
     */
    public OptionalOutput<List<SamplingOutput>> getSamplings() {
        return samplings;
    }

    /**
     * @param samplings the samplings to set
     */
    public void setSamplings(OptionalOutput<List<SamplingOutput>> samplings) {
        this.samplings = samplings;
    }
}
