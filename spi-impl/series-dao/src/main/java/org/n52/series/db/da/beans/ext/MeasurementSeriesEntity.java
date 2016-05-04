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
package org.n52.series.db.da.beans.v1;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.n52.series.db.da.beans.CategoryEntity;
import org.n52.series.db.da.beans.FeatureEntity;
import org.n52.series.db.da.beans.PhenomenonEntity;
import org.n52.series.db.da.beans.ProcedureEntity;
import org.n52.series.db.da.beans.UnitEntity;

public class SeriesEntity {

    private Long pkid;

    private CategoryEntity category;

    private PhenomenonEntity phenomenon;

    private ProcedureEntity procedure;

    private FeatureEntity feature;

    private int numberOfDecimals;

    private UnitEntity unit;

    private boolean published;

    private List<ObservationEntity> observations = new ArrayList<ObservationEntity>();

    private Set<SeriesEntity> referenceValues = new HashSet<SeriesEntity>();

    private ObservationEntity firstValue;

    private ObservationEntity lastValue;

    public Long getPkid() {
        return pkid;
    }

    public void setPkid(Long pkid) {
        this.pkid = pkid;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public PhenomenonEntity getPhenomenon() {
        return phenomenon;
    }

    public void setPhenomenon(PhenomenonEntity phenomenon) {
        this.phenomenon = phenomenon;
    }

    public ProcedureEntity getProcedure() {
        return procedure;
    }

    public void setProcedure(ProcedureEntity procedure) {
        this.procedure = procedure;
    }

    public FeatureEntity getFeature() {
        return feature;
    }

    public void setFeature(FeatureEntity feature) {
        this.feature = feature;
    }

    public List<ObservationEntity> getObservations() {
        return observations;
    }

    public void setObservations(List<ObservationEntity> observations) {
        this.observations = observations;
    }

    public Set<SeriesEntity> getReferenceValues() {
        return referenceValues;
    }

    public void setReferenceValues(Set<SeriesEntity> referenceValues) {
        this.referenceValues = referenceValues;
    }

    public int getNumberOfDecimals() {
        return numberOfDecimals;
    }

    public void setNumberOfDecimals(int numberOfDecimals) {
        this.numberOfDecimals = numberOfDecimals;
    }

    public UnitEntity getUnit() {
        return unit;
    }

    public void setUnit(UnitEntity unit) {
        this.unit = unit;
    }

    public Boolean isPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public ObservationEntity getFirstValue() {
        if (firstValue != null) {
            Date when = firstValue.getTimestamp();
            Double value = firstValue.getValue();
            if (when == null || value == null) {
                return null; // empty component
            }
        }
        return firstValue;
    }

    public void setFirstValue(ObservationEntity firstValue) {
        this.firstValue = firstValue;
    }

    public ObservationEntity getLastValue() {
        if (lastValue != null) {
            Date when = lastValue.getTimestamp();
            Double value = lastValue.getValue();
            if (when == null || value == null) {
                return null; // empty component
            }
        }
        return lastValue;
    }

    public void setLastValue(ObservationEntity lastValue) {
        this.lastValue = lastValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append(" id: ").append(pkid);
        sb.append(" , category: ").append(category);
        sb.append(" , phenomenon: ").append(phenomenon);
        sb.append(" , procedure: ").append(procedure);
        sb.append(" , feature: ").append(feature);
        sb.append(" , #observations: ").append(observations.size());
        return sb.append(" ]").toString();
    }

}
