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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.CountDatasetEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.TextDatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.util.JTSHelper;

public class EntityBuilder {

    public static ProxyServiceEntity createService(String name, String description, String connector, String url, String version) {
        ProxyServiceEntity service = new ProxyServiceEntity();
        service.setName(name);
        service.setDescription(description);
        service.setVersion(version);
        service.setType("SOS");
        service.setUrl(url);
        service.setConnector(connector);
        return service;
    }

    public static ProcedureEntity createProcedure(String domainId, String name, boolean insitu, boolean mobile, ProxyServiceEntity service) {
        ProcedureEntity procedure = new ProcedureEntity();
        procedure.setName(name);
        procedure.setDomainId(domainId);
        procedure.setInsitu(insitu);
        procedure.setMobile(mobile);
        procedure.setService(service);
        return procedure;
    }

    public static OfferingEntity createOffering(String domainId, String name, ProxyServiceEntity service) {
        OfferingEntity offering = new OfferingEntity();
        offering.setDomainId(domainId);
        offering.setName(name);
        offering.setService(service);
        return offering;
    }

    public static CategoryEntity createCategory(String domainId, String name, ProxyServiceEntity service) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        category.setDomainId(domainId);
        category.setService(service);
        return category;
    }

    public static FeatureEntity createFeature(String domainId, String name, GeometryEntity geometry, ProxyServiceEntity service) {
        FeatureEntity feature = new FeatureEntity();
        feature.setName(name);
        feature.setDomainId(domainId);
        feature.setGeometryEntity(geometry);
        feature.setService(service);
        return feature;
    }

    public static GeometryEntity createGeometry(double latitude, double longitude, int srid) {
        GeometryEntity geometry = new GeometryEntity();
        try {
            geometry.setGeometry(JTSHelper.createGeometryFromWKT("POINT (" + longitude + " " + latitude + ")", srid));
        } catch (DecodingException ex) {
            Logger.getLogger(EntityBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return geometry;
    }

    public static PhenomenonEntity createPhenomenon(String domainId, String name, ProxyServiceEntity service) {
        PhenomenonEntity phenomenon = new PhenomenonEntity();
        phenomenon.setName(name);
        phenomenon.setDomainId(domainId);
        phenomenon.setService(service);
        return phenomenon;
    }

    public static UnitEntity createUnit(String unit, ProxyServiceEntity service) {
        UnitEntity entity = new UnitEntity();
        entity.setName(unit);
        entity.setService(service);
        return entity;
    }

    public static MeasurementDatasetEntity createMeasurementDataset(ProcedureEntity procedure, CategoryEntity category, FeatureEntity feature, OfferingEntity offering, PhenomenonEntity phenomenon, UnitEntity unit, ProxyServiceEntity service) {
        MeasurementDatasetEntity measurementDataset = new MeasurementDatasetEntity();
        updateDataset(measurementDataset, procedure, category, feature, offering, phenomenon, service);
        measurementDataset.setUnit(unit);
        measurementDataset.setFirstValueAt(new Date());
        measurementDataset.setLastValueAt(new Date());
        return measurementDataset;
    }

    public static TextDatasetEntity createTextDataset(ProcedureEntity procedure, CategoryEntity category, FeatureEntity feature, OfferingEntity offering, PhenomenonEntity phenomenon, ProxyServiceEntity service) {
        TextDatasetEntity textDataset = new TextDatasetEntity();
        updateDataset(textDataset, procedure, category, feature, offering, phenomenon, service);
        return textDataset;
    }

    public static CountDatasetEntity createCountDataset(ProcedureEntity procedure, CategoryEntity category, FeatureEntity feature, OfferingEntity offering, PhenomenonEntity phenomenon, ProxyServiceEntity service) {
        CountDatasetEntity countDataset = new CountDatasetEntity();
        updateDataset(countDataset, procedure, category, feature, offering, phenomenon, service);
        countDataset.setFirstValueAt(new Date());
        countDataset.setLastValueAt(new Date());
        return countDataset;
    }

    private static void updateDataset(DatasetEntity dataset, ProcedureEntity procedure, CategoryEntity category, FeatureEntity feature, OfferingEntity offering, PhenomenonEntity phenomenon, ProxyServiceEntity service) {
        dataset.setProcedure(procedure);
        dataset.setCategory(category);
        dataset.setFeature(feature);
        dataset.setPhenomenon(phenomenon);
        dataset.setOffering(offering);
        dataset.setPublished(Boolean.TRUE);
        dataset.setDeleted(Boolean.FALSE);
        dataset.setService(service);
    }

}
