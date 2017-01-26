/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    public CategoryEntity putCategory(String name) {
        return categories.put(name, EntityBuilder.createCategory(name, service));
    }

    public FeatureEntity putFeature(String name, double latitude, double longitude, int srid) {
        return features.put(name,
                EntityBuilder.createFeature(
                        name,
                        EntityBuilder.createGeometry(latitude, longitude, srid),
                        service
                )
        );
    }

    public OfferingEntity putOffering(String name) {
        return offerings.put(name, EntityBuilder.createOffering(name, service));
    }

    public PhenomenonEntity putPhenomenon(String name) {
        return phenomenons.put(name, EntityBuilder.createPhenomenon(name, service));
    }

    public ProcedureEntity putProcedure(String name) {
        return procedures.put(name, EntityBuilder.createProcedure(name, true, true, service));
    }

    public boolean add(DatasetConstellation e) {
        return datasets.add(e);
    }

}
