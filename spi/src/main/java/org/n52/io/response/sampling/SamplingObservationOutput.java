/*
 * Copyright (C) 2013-2021 52°North Initiative for Geospatial Open Source
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
import org.n52.io.response.DetectionLimitOutput;
import org.n52.io.response.OfferingOutput;
import org.n52.io.response.OptionalOutput;
import org.n52.io.response.PhenomenonOutput;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.SelfSerializedOutput;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.TimeOutputConverter;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.DatasetOutput;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonPropertyOrder({ "value", "detectionLimit", "timestamp", "uom"})
public class SamplingObservationOutput extends SelfSerializedOutput {

    private AbstractValue<?> value;
    private OptionalOutput<String> uom;
    private DatasetOutput<AbstractValue<?>> dataset;
    private PhenomenonOutput phenomenon;
    private CategoryOutput category;
    private ProcedureOutput procedure;
    private PlatformOutput platform;
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

    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getTimestart() {
        return getAbstractValue().getTimestart();
    }

    public String getUom() {
        return getIfSerialized(uom);
    }

    public SamplingObservationOutput setUom(OptionalOutput<String> uom) {
        this.uom = uom;
        return this;
    }

    @JsonInclude(content = Include.ALWAYS)
    public Object getValue() {
        return getAbstractValue().getValue();
    }

    public DetectionLimitOutput getDetectionLimit() {
        return getAbstractValue().getDetectionLimit();
    }

    @JsonSerialize(using = GeoJSONGeometrySerializer.class)
    public Geometry getGeometry() {
        return getAbstractValue().getGeometry();
    }


    public void setValue(AbstractValue<?> value) {
        this.value = value;
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


    public PlatformOutput getPlatform() {
        return platform;
    }


    public void setPlatform(PlatformOutput platform) {
        this.platform = platform;
    }


    public OfferingOutput getOffering() {
        return offering;
    }


    public void setOffering(OfferingOutput offering) {
        this.offering = offering;
    }

}
