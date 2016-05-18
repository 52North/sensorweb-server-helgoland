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
package org.n52.series.db.da.beans.ext;

import java.util.ArrayList;
import java.util.List;
import org.n52.io.response.v1.ext.PlatformType;

import org.n52.series.db.da.beans.CategoryEntity;
import org.n52.series.db.da.beans.FeatureEntity;
import org.n52.series.db.da.beans.PhenomenonEntity;
import org.n52.series.db.da.beans.ProcedureEntity;
import org.n52.series.db.da.beans.UnitEntity;

public abstract class AbstractSeriesEntity<T extends AbstractObservationEntity> {

    private Long pkid;

    private CategoryEntity category;

    private PhenomenonEntity phenomenon;

    private ProcedureEntity procedure;

    private FeatureEntity feature;

    private PlatformEntity platform;

    private Boolean published;

    private List<T> observations = new ArrayList<>();

    private boolean mobile = false;

    private boolean insitu = true;

    private Class<T> entityType;

    private UnitEntity unit;

    private String observationtype;

    public AbstractSeriesEntity() {
        this.entityType = (Class<T>) AbstractObservationEntity.class;
    }

    /**
     *
     * @return the platform type
     * @since 2.0.0
     */
    public PlatformType getPlatformType() {
        return PlatformType.toInstance(mobile, insitu);
    }

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

    public PlatformEntity getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformEntity platform) {
        this.platform = platform;
    }

    public List<T> getObservations() {
        return observations;
    }

    public void setObservations(List<T> observations) {
        this.observations = observations;
    }

    public boolean isMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public boolean isInsitu() {
        return insitu;
    }

    public void setInsitu(boolean insitu) {
        this.insitu = insitu;
    }

    public Boolean isPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public UnitEntity getUnit() {
        return unit;
    }

    public void setUnit(UnitEntity unit) {
        this.unit = unit;
    }

    public boolean hasUnit() {
        return unit != null;
    }

    public String getObservationtype() {
        return observationtype;
    }

    public void setObservationtype(String observationType) {
        this.observationtype = observationType;
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
        sb.append(" id: ").append(pkid);
        sb.append(" , category: ").append(category);
        sb.append(" , phenomenon: ").append(phenomenon);
        sb.append(" , procedure: ").append(procedure);
        sb.append(" , feature: ").append(feature);
        sb.append(" , #observations: ").append(observations.size());
        return sb.append(" ]").toString();
    }

}
