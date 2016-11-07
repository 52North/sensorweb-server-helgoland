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

package org.n52.series.db_custom.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.n52.io.response.dataset.measurement.MeasurementDatasetOutput;
import org.n52.series.db.beans.DataEntity;


public class DatasetTEntity<T extends DataEntity<?>> extends DescribableTEntity {

    public static final String PROCEDURE = "procedure";
    public static final String CATEGORY = "category";
    public static final String PHENOMENON = "phenomenon";
    public static final String FEATURE = "feature";
    public static final String PLATFORM = "platform";
    public static final String OBSERVATION_TYPE = "observationType";

    private CategoryTEntity category;

    private PhenomenonTEntity phenomenon;

    private ProcedureTEntity procedure;

    private FeatureTEntity feature;

    private PlatformTEntity platform;

    private Boolean published;

    private Boolean deleted;

    private List<T> observations;

    private String datasetType;

    @Deprecated
    private T firstValue;

    @Deprecated
    private T lastValue;

    private Date firstValueAt;

    private Date lastValueAt;

    private UnitTEntity unit;

    public DatasetTEntity() {
        this.observations = new ArrayList<>();
    }

    public CategoryTEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryTEntity category) {
        this.category = category;
    }

    public PhenomenonTEntity getPhenomenon() {
        return phenomenon;
    }

    public void setPhenomenon(PhenomenonTEntity phenomenon) {
        this.phenomenon = phenomenon;
    }

    public ProcedureTEntity getProcedure() {
        return procedure;
    }

    public void setProcedure(ProcedureTEntity procedure) {
        this.procedure = procedure;
    }

    public FeatureTEntity getFeature() {
        return feature;
    }

    public void setFeature(FeatureTEntity feature) {
        this.feature = feature;
    }

    public PlatformTEntity getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformTEntity platform) {
        this.platform = platform;
    }

    public List<T> getObservations() {
        return observations;
    }

    public void setObservations(List<T> observations) {
        this.observations = observations;
    }

    public Boolean isPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * @return
     * @deprecated since 2.0.0, use {@link #getFirstValueAt()}
     */
    @Deprecated
    public T getFirstValue() {
        return firstValue;
    }

    /**
     * @param firstValue
     * @deprecated since 2.0.0, use {@link #setFirstValueAt(Date)}
     */
    @Deprecated
    public void setFirstValue(T firstValue) {
        this.firstValue = firstValue;
    }

    /**
     * @return
     * @deprecated since 2.0.0, use {@link #getLastValueAt()}
     */
    @Deprecated
    public T getLastValue() {
        return lastValue;
    }

    /**
     * @param lastValue
     * @deprecated since 2.0.0, use {@link #setLastValueAt(Date)}
     */
    @Deprecated
    public void setLastValue(T lastValue) {
        this.lastValue = lastValue;
    }

    public Date getFirstValueAt() {
        return firstValueAt;
    }

    public void setFirstValueAt(Date firstValueAt) {
        this.firstValueAt = firstValueAt;
    }

    public Date getLastValueAt() {
        return lastValueAt;
    }

    public void setLastValueAt(Date lastValueAt) {
        this.lastValueAt = lastValueAt;
    }

    public String getDatasetType() {
        return datasetType == null || datasetType.isEmpty()
                ? MeasurementDatasetOutput.DATASET_TYPE // backward compatible
                : datasetType;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }

    public UnitTEntity getUnit() {
        return unit;
    }

    public void setUnit(UnitTEntity unit) {
        this.unit = unit;
    }

    public boolean hasUnit() {
        return unit != null;
    }

    public String getUnitI18nName(String locale) {
        String name = null;
        if (unit != null) {
            name = unit.getNameI18n(locale);
        }
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append(" id: ").append(getPkid());
        sb.append(" , category: ").append(category);
        sb.append(" , phenomenon: ").append(phenomenon);
        sb.append(" , procedure: ").append(procedure);
        sb.append(" , feature: ").append(feature);
        sb.append(" , service: ").append(getService());
        sb.append(" , #observations: ").append(observations.size());
        return sb.append(" ]").toString();
    }

}
