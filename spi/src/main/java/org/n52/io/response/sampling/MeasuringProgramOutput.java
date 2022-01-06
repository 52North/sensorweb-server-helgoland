/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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
import org.n52.io.response.AbstractOutput;
import org.n52.io.response.CategoryOutput;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.OptionalOutput;
import org.n52.io.response.PhenomenonOutput;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.TimeOutputConverter;
import org.n52.io.response.dataset.DatasetOutput;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class MeasuringProgramOutput extends AbstractOutput {

    public static final String COLLECTION_PATH = "measuringPrograms";
    public static final String ORDER_ID = "orderId";
    public static final String MEASURING_PROGRAM_TIME_START = "measuringProgramTimeStart";
    public static final String MEASURING_PROGRAM_TIME_END = "measuringProgramTimeEnd";
    public static final String OBSERVED_AREA = "observedArea";
    public static final String PRODUCER = "producer";
    public static final String DATASETS = "datasets";
    public static final String SAMPLINGS = "samplings";
    public static final String FEATURES = "features";
    public static final String PHENOMENA = "phenomeny";
    public static final String CATEGORIES = "categories";
    private OptionalOutput<String> orderId;
    private OptionalOutput<TimeOutput> measuringProgramTimeStart;
    private OptionalOutput<TimeOutput> measuringProgramTimeEnd;
    private OptionalOutput<Geometry> observedArea;
    private OptionalOutput<ProducerOutput> producer;

    private OptionalOutput<List<DatasetOutput<?>>> datasets;
    private OptionalOutput<List<SamplingOutput>> samplings;
    private OptionalOutput<List<FeatureOutput>> features;
    private OptionalOutput<List<PhenomenonOutput>> phenomena;
    private OptionalOutput<List<CategoryOutput>> categories;

    @Override
    protected String getCollectionName() {
        return COLLECTION_PATH;
    }

    /**
     * @return the orderId
     */
    public String getOrderId() {
        return getIfSerialized(orderId);
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
    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getMeasuringProgramTimeStart() {
        return getIfSerialized(measuringProgramTimeStart);
    }

    /**
     * @param measuringProgramTimeStart the measuringProgramTimeStart to set
     */
    public void setMeasuringProgramTimeStart(OptionalOutput<TimeOutput> measuringProgramTimeStart) {
        this.measuringProgramTimeStart = measuringProgramTimeStart;
    }

    /**
     * @return the measuringProgramTimeEnd
     */
    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getMeasuringProgramTimeEnd() {
        return getIfSerialized(measuringProgramTimeEnd);
    }

    /**
     * @param measuringProgramTimeEnd the measuringProgramTimeEnd to set
     */
    @JsonInclude(Include.ALWAYS)
    public void setMeasuringProgramTimeEnd(OptionalOutput<TimeOutput> measuringProgramTimeEnd) {
        this.measuringProgramTimeEnd = measuringProgramTimeEnd;
    }

    /**
     * @return the observedArea
     */
    @JsonSerialize(using = GeoJSONGeometrySerializer.class)
    public Geometry getObservedArea() {
        return getIfSerialized(observedArea);
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
    public ProducerOutput getProducer() {
        return getIfSerialized(producer);
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
    public List<DatasetOutput<?>> getDatasets() {
        return getIfSerialized(datasets);
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
    public List<SamplingOutput> getSamplings() {
        return getIfSerialized(samplings);
    }

    /**
     * @param samplings the samplings to set
     */
    public void setSamplings(OptionalOutput<List<SamplingOutput>> samplings) {
        this.samplings = samplings;
    }

    /**
     * @return the features
     */
    public List<FeatureOutput> getFeatures() {
        return getIfSerialized(features);
    }

    /**
     * @param features the features to set
     */
    public void setFeatures(OptionalOutput<List<FeatureOutput>> features) {
        this.features = features;
    }

    /**
     * @return the phenomena
     */
    public List<PhenomenonOutput> getPhenomena() {
        return getIfSerialized(phenomena);
    }

    /**
     * @param phenomena the phenomena to set
     */
    public void setPhenomena(OptionalOutput<List<PhenomenonOutput>> phenomena) {
        this.phenomena = phenomena;
    }

    /**
     * @return the categories
     */
    public List<CategoryOutput> getCategories() {
        return getIfSerialized(categories);
    }

    /**
     * @param categories the categories to set
     */
    public void setCategories(OptionalOutput<List<CategoryOutput>> categories) {
        this.categories = categories;
    }
}
