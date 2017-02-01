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
package org.n52.proxy.connector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ServiceEntity;

public class ServiceConstellation {

    // service
    private ServiceEntity service;

    // map für procedures
    private final Map<String, ProcedureEntity> procedures = new HashMap<>();

    // map für offerings
    private final Map<String, OfferingEntity> offerings = new HashMap<>();

    // map für categories
    private final Map<String, CategoryEntity> categories = new HashMap<>();

    // map für phenomenons
    private final Map<String, PhenomenonEntity> phenomenons = new HashMap<>();

    // map für feature
    private final Map<String, FeatureEntity> features = new HashMap<>();

    // dataset collection
    private final Collection<DatasetConstellation> datasets = new HashSet<>();

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public Map<String, ProcedureEntity> getProcedures() {
        return procedures;
    }

    public Map<String, OfferingEntity> getOfferings() {
        return offerings;
    }

    public Map<String, CategoryEntity> getCategories() {
        return categories;
    }

    public Map<String, PhenomenonEntity> getPhenomenons() {
        return phenomenons;
    }

    public Map<String, FeatureEntity> getFeatures() {
        return features;
    }

    public Collection<DatasetConstellation> getDatasets() {
        return datasets;
    }

    public CategoryEntity putCategory(String id, String name) {
        return categories.put(id, EntityBuilder.createCategory(id, name, service));
    }

    public FeatureEntity putFeature(String id, String name, double latitude, double longitude, int srid) {
        return features.put(id,
                EntityBuilder.createFeature(
                        id,
                        name,
                        EntityBuilder.createGeometry(latitude, longitude, srid),
                        service
                )
        );
    }

    public OfferingEntity putOffering(String id, String name) {
        return offerings.put(id, EntityBuilder.createOffering(id, name, service));
    }

    public PhenomenonEntity putPhenomenon(String id, String name) {
        return phenomenons.put(id, EntityBuilder.createPhenomenon(id, name, service));
    }

    public ProcedureEntity putProcedure(String id, String name, boolean insitu, boolean mobile) {
        return procedures.put(id, EntityBuilder.createProcedure(id, name, insitu, mobile, service));
    }

    public boolean add(DatasetConstellation e) {
        return datasets.add(e);
    }

}
