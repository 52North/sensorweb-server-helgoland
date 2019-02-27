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
package org.n52.io.response.sampling;

import org.locationtech.jts.geom.Geometry;
import org.n52.io.geojson.GeoJSONGeometrySerializer;
import org.n52.io.response.CategoryOutput;
import org.n52.io.response.OfferingOutput;
import org.n52.io.response.PhenomenonOutput;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.TimeOutputConverter;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.DatasetOutput;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class SamplingObservationOutput {

    private AbstractValue<?> value;
    private DetectionLimitOutput detectionLimit;
    private DatasetOutput<AbstractValue<?>> dataset;
    private PhenomenonOutput phenomenon;
    private CategoryOutput category;
    private ProcedureOutput procedure;
    private PlatformOutput platfrom;
    private OfferingOutput offering;


    private AbstractValue<?> getAbstractValue() {
        return value;
    }

    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getTimestamp() {
        return getAbstractValue().getTimestamp();
    }

    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getTimeend() {
        return getAbstractValue().getTimeend();
    }

    public Object getTimestart() {
        return getAbstractValue().getTimestart();
    }

    @JsonInclude(content = Include.ALWAYS)
    public Object getValue() {
        return getAbstractValue().getValue();
    }

    @JsonSerialize(using = GeoJSONGeometrySerializer.class)
    public Geometry getGeometry() {
        return getAbstractValue().getGeometry();
    }


    public void setValue(AbstractValue<?> value) {
        this.value = value;
    }

    public DetectionLimitOutput getDetectionLimit() {
        return detectionLimit;
    }

    public void setDetectionLimit(DetectionLimitOutput detectionLimit) {
        this.detectionLimit = detectionLimit;
    }

    public DatasetOutput<AbstractValue<?>> getDataset() {
        return dataset;
    }


    public void setDataset(DatasetOutput<AbstractValue<?>> dataset) {
        this.dataset = dataset;
    }


    public PhenomenonOutput getPhenomenon() {
        return phenomenon;
    }


    public void setPhenomenon(PhenomenonOutput phenomenon) {
        this.phenomenon = phenomenon;
    }


    public CategoryOutput getCategory() {
        return category;
    }


    public void setCategory(CategoryOutput category) {
        this.category = category;
    }


    public ProcedureOutput getProcedure() {
        return procedure;
    }


    public void setProcedure(ProcedureOutput procedure) {
        this.procedure = procedure;
    }


    public PlatformOutput getPlatfrom() {
        return platfrom;
    }


    public void setPlatfrom(PlatformOutput platfrom) {
        this.platfrom = platfrom;
    }


    public OfferingOutput getOffering() {
        return offering;
    }


    public void setOffering(OfferingOutput offering) {
        this.offering = offering;
    }

}
