/**
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.series.api.v1.db.da.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeriesEntity implements MergableEntity, Serializable {

    private static final long serialVersionUID = -6979717443102020645L;

    private Long pkid;
    
    /**
     * Only relevant for some e-reporting cases
     */
    private Long samplingPointId;

    /**
     * Only relevant for some e-reporting cases
     */
    private String mergeRole;

    private CategoryEntity category;

    private PhenomenonEntity phenomenon;

    private ProcedureEntity procedure;

    private FeatureEntity feature;

    private int numberOfDecimals;

    private UnitEntity unit;

    private Boolean published;
    
    private Boolean deleted;

    private OfferingEntity offering;
    
    private List<ObservationEntity> observations = new ArrayList<ObservationEntity>();

    private Set<SeriesEntity> referenceValues = new HashSet<SeriesEntity>();

    private ObservationEntity firstValue;

    private ObservationEntity lastValue;
    
    private Set<SeriesEntity> mergableSeries = new HashSet<>();
    
    private static SeriesEntity getMergedSeries(SeriesEntity series) {
        if (series == null) {
            throw new IllegalStateException("Cannot merge null series");
        }
        SeriesEntity mergedSeries = series;
        Set<SeriesEntity> toMerge = series.getMergableSeries();
        for (SeriesEntity otherSeries : toMerge) {
            if (Long.compare(mergedSeries.getPkid(), otherSeries.getPkid()) > 0) {
                mergedSeries = otherSeries;
            }
        }

        
        for (SeriesEntity otherSeries : toMerge) {
            final ObservationEntity mergedFirstObservation = mergedSeries.getFirstValue();
            final ObservationEntity otherFirstObservation = otherSeries.getFirstValue();
            if (mergedFirstObservation == null) {
                mergedSeries.setFirstValue(otherFirstObservation);
            } else {
                if (mergedFirstObservation != null 
                        && otherFirstObservation != null
                        && mergedFirstObservation.getTimestamp().after(otherFirstObservation.getTimestamp())) {
                    mergedSeries.setFirstValue(otherFirstObservation);
                }
            }
            final ObservationEntity mergedLastObservation = mergedSeries.getLastValue();
            final ObservationEntity otherLastObservation = otherSeries.getLastValue();
            if (mergedLastObservation == null) {
                mergedSeries.setLastValue(otherSeries.getLastValue());
            } else {
                if (mergedLastObservation != null 
                        && otherLastObservation != null
                        && mergedLastObservation.getTimestamp().before(otherLastObservation.getTimestamp())) {
                    mergedSeries.setLastValue(otherLastObservation);
                }
            }
        }
        
        return mergedSeries;
    }
    
    public Long getPkid() {
        return pkid;
    }

    public void setPkid(Long pkid) {
        this.pkid = pkid;
    }

    public Long getSamplingPointId() {
        return samplingPointId;
    }

    public void setSamplingPointId(Long samplingPointId) {
        this.samplingPointId = samplingPointId;
    }

    @Override
    public String getMergeRole() {
        return mergeRole;
    }

    @Override
    public void setMergeRole(String mergeRole) {
        this.mergeRole = mergeRole;
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
        return getMergedSeries(this).procedure;
    }

    public void setProcedure(ProcedureEntity procedure) {
        this.procedure = procedure;
    }

    public FeatureEntity getFeature() {
        return FeatureEntity.getMergedFeature(feature);
    }

    public void setFeature(FeatureEntity feature) {
        this.feature = feature;
    }
    
    public OfferingEntity getOffering() {
        return getMergedSeries(this).offering;
    }

    public void setOffering(OfferingEntity offering) {
        this.offering = offering;
    }

    public List<ObservationEntity> getObservations() {
        List<ObservationEntity> mergedObservations = new ArrayList<>();
        for (SeriesEntity entity : getMergableSeries()) {
            mergedObservations.addAll(entity.observations);
        }
        return mergedObservations;
    }

    public void setObservations(List<ObservationEntity> observations) {
        this.observations = observations;
    }

    public Set<SeriesEntity> getReferenceValues() {
        Set<SeriesEntity> mergedReferenceValues = new HashSet<>();
        for (SeriesEntity entity : getMergableSeries()) {
            mergedReferenceValues.addAll(entity.referenceValues);
        }
        return mergedReferenceValues;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public ObservationEntity getFirstValue() {
        ObservationEntity value = getValue(firstValue);
        for (SeriesEntity seriesEntity : getMergableSeries()) {
            ObservationEntity otherValue = seriesEntity.getValue(seriesEntity.firstValue);
            value = value == null ? otherValue : value;
            if (otherValue != null && otherValue.getTimestamp().before(value.getTimestamp())) {
                value = otherValue;
            }
        }
        return value;
    }

    public void setFirstValue(ObservationEntity firstValue) {
        this.firstValue = firstValue;
    }

    public ObservationEntity getLastValue() {
        ObservationEntity value = getValue(lastValue);
        for (SeriesEntity seriesEntity : getMergableSeries()) {
            ObservationEntity otherValue = seriesEntity.getValue(seriesEntity.lastValue);
            value = value == null ? otherValue : value;
            if (otherValue != null && otherValue.getTimestamp().after(value.getTimestamp())) {
                value = otherValue;
            }
        }
        return value;
    }
    
    private ObservationEntity getValue(ObservationEntity value) {
        if (value != null) {
            if (value.getTimestamp() == null) {
                 return null; // empty component
             }
         }
         return value;
    }

    public void setLastValue(ObservationEntity lastValue) {
        this.lastValue = lastValue;
    }

    /**
     * @return all series to be merged with this instance
     */
    public Set<SeriesEntity> getMergableSeries() {
        return mergableSeries;
    }

    public void setMergableSeries(Set<SeriesEntity> mergableSeries) {
        this.mergableSeries = mergableSeries;
    }
    
    @Override
    public Set<Long> getMergablePkids() {
        Set<Long> pkids = new HashSet<>();
        for (SeriesEntity entity : getMergableSeries()) {
            pkids.add(entity.getPkid());
        }
        pkids.add(pkid);
        return pkids;
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
