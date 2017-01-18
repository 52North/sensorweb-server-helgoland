/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.proxy.db.beans;

import java.util.HashSet;
import java.util.Set;

import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.ServiceEntity;


public class RelatedFeatureEntity {

    public static final String FEATURE = "feature";
    public final static String SERVICE = "service";

    private long relatedFeatureId;
    private FeatureEntity feature;
    private ServiceEntity service;
    private Set<RelatedFeatureRoleEntity> relatedFeatureRoles = new HashSet<RelatedFeatureRoleEntity>(0);
    private Set<OfferingEntity> offerings = new HashSet<OfferingEntity>(0);

    public RelatedFeatureEntity() {
    }

    public long getRelatedFeatureId() {
        return this.relatedFeatureId;
    }

    public void setRelatedFeatureId(long relatedFeatureId) {
        this.relatedFeatureId = relatedFeatureId;
    }

    public FeatureEntity getFeature() {
        return this.feature;
    }

    public void setFeature(FeatureEntity feature) {
        this.feature = feature;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public Set<RelatedFeatureRoleEntity> getRelatedFeatureRoles() {
        return this.relatedFeatureRoles;
    }

    public void setRelatedFeatureRoles(Set<RelatedFeatureRoleEntity> relatedFeatureRoles) {
        this.relatedFeatureRoles = relatedFeatureRoles;
    }

    public Set<OfferingEntity> getOfferings() {
        return this.offerings;
    }

    @SuppressWarnings("unchecked")
    public void setOfferings(final Object offerings) {
        if (offerings instanceof Set<?>) {
            this.offerings = (Set<OfferingEntity>) offerings;
        } else {
            getOfferings().add((OfferingEntity)offerings);
        }
    }
}
