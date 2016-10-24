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
package org.n52.hibernate;

import java.util.GregorianCalendar;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.n52.io.request.IoParameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.FeatureDao;
import org.n52.series.db.dao.ServiceDao;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.JTSHelper;

public class HibernateTestCase {

    SessionFactory sessionFactory;

    @Before
    public void init() {
        Configuration configuration = new Configuration().configure();
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/CategoryResource.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/DataParameter.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/DataResource.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/DatasetResource.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/FeatureResource.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/PhenomenonResource.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/PlatformResource.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/ProcedureResource.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/SamplingGeometryResource.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/ServiceResource.hbm.xml"));
        configuration.addInputStream(getClass().getResourceAsStream("/hbm/sos/v44/UnitResource.hbm.xml"));
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
        sessionFactory = configuration.buildSessionFactory(builder.build());
    }

    @Test
    public void addData() throws OwsExceptionReport, DataAccessException {

        Session session = sessionFactory.openSession();

        try {
            Transaction transaction = session.beginTransaction();

            for (int i = 1; i < 11; i++) {
                ServiceEntity service1 = createService("test@" + i, session);

                ProcedureEntity procedure1 = createProcedure("proc1@"+i, service1, session);
                ProcedureEntity procedure2 = createProcedure("proc2@"+i, service1, session);
                ProcedureEntity procedure3 = createProcedure("proc3@"+i, service1, session);
                ProcedureEntity procedure4 = createProcedure("proc4@"+i, service1, session);

                CategoryEntity category = createCategory("cat1@"+i, service1, session);

                FeatureEntity feature1 = createFeature("fea1@"+i, service1, session);
                FeatureEntity feature2 = createFeature("fea2@"+i, service1, session);

                PhenomenonEntity phenomenon1 = createPhenomenon("phen1@"+i, service1, session);
                PhenomenonEntity phenomenon2 = createPhenomenon("phen2@"+i, service1, session);
                PhenomenonEntity phenomenon3 = createPhenomenon("phen3@"+i, service1, session);

                UnitEntity unit = createUnit("unit1@"+i, service1, session);

                PlatformEntity platform1 = createPlatform("platform1@"+i, service1, session);
                PlatformEntity platform2 = createPlatform("platform2@"+i, service1, session);
                PlatformEntity platform3 = createPlatform("platform3@"+i, service1, session);
                PlatformEntity platform4 = createPlatform("platform4@"+i, service1, session);

                createMeasurementDataset("mea1@"+i, procedure1, category, feature1, phenomenon1, platform1, unit, service1, session);
                createMeasurementDataset("mea2@"+i, procedure2, category, feature1, phenomenon2, platform2, unit, service1, session);
                createMeasurementDataset("mea3@"+i, procedure3, category, feature1, phenomenon3, platform3, unit, service1, session);
                createMeasurementDataset("mea4@"+i, procedure4, category, feature2, phenomenon1, platform4, unit, service1, session);
                createMeasurementDataset("mea5@"+i, procedure4, category, feature2, phenomenon2, platform4, unit, service1, session);
            }

//            GregorianCalendar cal = new GregorianCalendar(2016, 9, 15, 0, 0, 0);
//            for (int i = 0; i < 1000; i++) {
//                MeasurementDataEntity entity = new MeasurementDataEntity();
//                cal.add(GregorianCalendar.MINUTE, 1);
//                entity.setTimestamp(cal.getTime());
//                entity.setSeriesPkid(measurementDataset.getPkid());
//                entity.setValue(Math.random());
//                entity.setDeleted(Boolean.FALSE);
//                session.save(entity);
//                session.flush();
//            }
//            CountDatasetEntity countDataset = new CountDatasetEntity();
//            countDataset.setProcedure(procedure);
//            countDataset.setCategory(category);
//            countDataset.setFeature(feature);
//            countDataset.setPhenomenon(phenomenon);
//            session.save(countDataset);
//            session.flush();
//            session.refresh(countDataset);
//
//            CountDataEntity countData = new CountDataEntity();
//            countData.setTimestamp(new Date());
//            countData.setSeriesPkid(countDataset.getPkid());
//            countData.setValue(123);
//            countData.setDeleted(Boolean.FALSE);
//            session.save(countData);
            session.flush();
            transaction.commit();
        } finally {
            session.close();
        }

    }

    @Test
    public void loadFeature() throws DataAccessException {

        Session session = sessionFactory.openSession();

        try {
            Transaction transaction = session.beginTransaction();

            FeatureDao featureDao = new FeatureDao(session);
            IoParameters test = IoParameters.createDefaults()
                    .extendWith("timeformat", "YYYY-MM-dd, HH:mm")
                    .extendWith("platformtypes", "stationary", "inistu")
                    .extendWith("datasettypes", "measurement")
                    .extendWith("services", "1");
            DbQuery createFrom = DbQuery.createFrom(test);
            List<FeatureEntity> allInstances = featureDao.getAllInstances(createFrom);
            System.out.println("Features:");
            allInstances.stream().forEach((allInstance) -> {
                System.out.println(allInstance);
            });

            FeatureEntity instance = featureDao.getInstance("1", createFrom);
            System.out.println(instance);

            session.flush();
            transaction.commit();
        } finally {
            session.close();
        }

    }

    private ServiceEntity createService(String name, Session session) {
        ServiceEntity service = new ServiceEntity();
        service.setDescription("description of " + name);
        service.setName(name);
        service.setType("SOS");
        service.setVersion("2.0.0");
        new ServiceDao(session).insertInstance(service);
        return service;
    }

    private ProcedureEntity createProcedure(String name, ServiceEntity service, Session session) {
        ProcedureEntity procedure = new ProcedureEntity();
        procedure.setInsitu(true);
        procedure.setMobile(false);
        procedure.setName(name);
        procedure.setDomainId("procedureDomain for " + name);
        procedure.setService(service);
        session.save(procedure);
        return procedure;
    }

    private CategoryEntity createCategory(String name, ServiceEntity service, Session session) {
        CategoryEntity category = new CategoryEntity();
        category.setDescription("description of " + name);
        category.setDomainId("categoryDomain" + name);
        category.setName(name);
        category.setService(service);
        session.save(category);
        return category;
    }

    private FeatureEntity createFeature(String name, ServiceEntity service, Session session) throws OwsExceptionReport {
        FeatureEntity feature = new FeatureEntity();
        feature.setDescription("description" + name);
        feature.setDomainId("featureDomain" + name);
        feature.setName("featureName" + name);
        feature.setService(service);
        GeometryEntity geom = new GeometryEntity();
        geom.setGeometry(JTSHelper.createGeometryFromWKT("POINT (" + (7 + Math.random()) + " " + (52 + Math.random()) + ")", 4326));
        feature.setGeometry(geom);
        session.save(feature);
        return feature;
    }

    private PhenomenonEntity createPhenomenon(String name, ServiceEntity service, Session session) {
        PhenomenonEntity phenomenon = new PhenomenonEntity();
        phenomenon.setDomainId("phenomenonDomain" + name);
        phenomenon.setName("phenomenonName" + name);
        phenomenon.setService(service);
        session.save(phenomenon);
        return phenomenon;
    }

    private UnitEntity createUnit(String name, ServiceEntity service, Session session) {
        UnitEntity unit = new UnitEntity();
        unit.setName("unitName" + name);
        unit.setDomainId("unitDomain" + name);
        unit.setService(service);
        session.save(unit);
        return unit;
    }

    private PlatformEntity createPlatform(String name, ServiceEntity service, Session session) {
        PlatformEntity platform = new PlatformEntity();
        platform.setName("platformName" + name);
        platform.setDomainId("platformDomain" + name);
        platform.setInsitu(true);
        platform.setMobile(false);
        platform.setService(service);
        session.save(platform);
        return platform;
    }

    private MeasurementDatasetEntity createMeasurementDataset(String measurement, ProcedureEntity procedure, CategoryEntity category, FeatureEntity feature, PhenomenonEntity phenomenon, PlatformEntity platform, UnitEntity unit, ServiceEntity service, Session session) {
        MeasurementDatasetEntity measurementDataset = new MeasurementDatasetEntity();
        measurementDataset.setName(measurement);
        measurementDataset.setProcedure(procedure);
        measurementDataset.setCategory(category);
        measurementDataset.setFeature(feature);
        measurementDataset.setPhenomenon(phenomenon);
        measurementDataset.setPlatform(platform);
        measurementDataset.setUnit(unit);
        measurementDataset.setFirstValueAt(new GregorianCalendar(2016, 9, 15, 1, 0, 0).getTime());
        measurementDataset.setLastValueAt(new GregorianCalendar(2016, 9, 15, 2, 0, 0).getTime());
        measurementDataset.setPublished(Boolean.TRUE);
        measurementDataset.setService(service);
        session.save(measurementDataset);
        session.flush();
        session.refresh(measurementDataset);
        return measurementDataset;
    }

}
