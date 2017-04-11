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
import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

public class FeatureEntity extends DescribableEntity<I18nFeatureEntity> implements MergableEntity, Serializable {

    private static final long serialVersionUID = 3269095078825157692L;

    private Geometry geom;

    private Set<FeatureEntity> mergableFeatures = new HashSet<>();
    
    /**
     * Only relevant for some e-reporting cases
     */
    private String mergeRole;
    
    static FeatureEntity getMergedFeature(FeatureEntity feature) {
        if (feature == null) {
            throw new IllegalStateException("Cannot merge null feature");
        }
        FeatureEntity mergedFeature = feature;
        Set<FeatureEntity> toMerge = feature.getMergableFeatures();
        for (FeatureEntity otherSeries : toMerge) {
            if (Long.compare(mergedFeature.getPkid(), otherSeries.getPkid()) > 0) {
                mergedFeature = otherSeries;
            }
        }
        return mergedFeature;
    }
    
    @Override
    public String getDomainId() {
        return getMergedFeature(this).domainId;
    }

    @Override
    public String getName() {
        return getMergedFeature(this).name;
    }

    @Override
    public String getDescription() {
        return getMergedFeature(this).description;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }
    
    public boolean isSetGeom() {
        return getGeom() != null && !getGeom().isEmpty();
    }

    public Set<FeatureEntity> getMergableFeatures() {
        return mergableFeatures;
    }

    public void setMergableFeatures(Set<FeatureEntity> mergableFeatures) {
        this.mergableFeatures = mergableFeatures;
    }

    public String getMergeRole() {
        return mergeRole;
    }

    public void setMergeRole(String mergeRole) {
        this.mergeRole = mergeRole;
    }
    
    @Override
    public Set<Long> getMergablePkids() {
        Set<Long> pkids = new HashSet<>();
        for (FeatureEntity entity : getMergableFeatures()) {
            pkids.add(entity.getPkid());
        }
        pkids.add(pkid);
        return pkids;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append(" Domain id: ").append(getDomainId());
        return sb.append(" ]").toString();
    }
    
}
