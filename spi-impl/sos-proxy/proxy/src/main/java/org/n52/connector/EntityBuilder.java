/**
 * Copyright (C) 2012-2016 52°North Initiative for Geospatial Open Source
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
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.connector;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.n52.series.db.beans.CategoryTEntity;
import org.n52.series.db.beans.FeatureTEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.MeasurementDatasetTEntity;
import org.n52.series.db.beans.PhenomenonTEntity;
import org.n52.series.db.beans.ProcedureTEntity;
import org.n52.series.db.beans.ServiceTEntity;
import org.n52.series.db.beans.UnitTEntity;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.JTSHelper;

public class EntityBuilder {

    public static ServiceTEntity createService(String name, String description, String version) {
        ServiceTEntity service = new ServiceTEntity();
        service.setDescription(description);
        service.setName(name);
        service.setVersion(version);
        service.setType("SOS");
        return service;
    }

    public static ProcedureTEntity createProcedure(String name, boolean insitu, boolean mobile, ServiceTEntity service) {
        ProcedureTEntity procedure = new ProcedureTEntity();
        procedure.setName(name);
        procedure.setDomainId(name);
        procedure.setInsitu(insitu);
        procedure.setMobile(mobile);
        procedure.setService(service);
        return procedure;
    }

    public static CategoryTEntity createCategory(String name, ServiceTEntity service) {
        CategoryTEntity category = new CategoryTEntity();
        category.setName(name);
        category.setDomainId(name);
        category.setService(service);
        return category;
    }

    public static FeatureTEntity createFeature(String name, GeometryEntity geometry, ServiceTEntity service) throws OwsExceptionReport {
        FeatureTEntity feature = new FeatureTEntity();
        feature.setName(name);
        feature.setDomainId(name);
        feature.setGeometry(geometry);
        feature.setService(service);
        return feature;
    }

    public static GeometryEntity createGeometry(double latitude, double longitude) {
        GeometryEntity geometry = new GeometryEntity();
        try {
            geometry.setGeometry(JTSHelper.createGeometryFromWKT("POINT (" + longitude + " " + latitude + ")", 4326));
        } catch (OwsExceptionReport ex) {
            Logger.getLogger(EntityBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return geometry;
    }

    public static PhenomenonTEntity createPhenomenon(String name, ServiceTEntity service) {
        PhenomenonTEntity phenomenon = new PhenomenonTEntity();
        phenomenon.setName(name);
        phenomenon.setDomainId(name);
        phenomenon.setService(service);
        return phenomenon;
    }

    public static UnitTEntity createUnit(String unit, ServiceTEntity service) {
        UnitTEntity entity = new UnitTEntity();
        entity.setName("unit" + unit);
        entity.setService(service);
        return entity;
    }

    public static MeasurementDatasetTEntity createMeasurementDataset(String measurement, ProcedureTEntity procedure, CategoryTEntity category, FeatureTEntity feature, PhenomenonTEntity phenomenon, UnitTEntity unit, ServiceTEntity service) {
        MeasurementDatasetTEntity measurementDataset = new MeasurementDatasetTEntity();
        measurementDataset.setName(measurement);
        measurementDataset.setProcedure(procedure);
        measurementDataset.setCategory(category);
        measurementDataset.setFeature(feature);
        measurementDataset.setPhenomenon(phenomenon);
        measurementDataset.setUnit(unit);
        //measurementDataset.setFirstValueAt(new GregorianCalendar(2016, 9, 15, 1, 0, 0).getTime());
        //measurementDataset.setLastValueAt(new GregorianCalendar(2016, 9, 15, 2, 0, 0).getTime());
        measurementDataset.setPublished(Boolean.TRUE);
        measurementDataset.setService(service);
        return measurementDataset;
    }

}
