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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.geotools.measure.Measure;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.io.request.IoParameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.CountDatasetEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.DbQueryTest;
import org.n52.series.db.dao.FeatureDao;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.JTSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateTestCase.class);

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

            for (int i = 0; i < 2; i++) {
                ServiceEntity service = new ServiceEntity();
                service.setDescription("description " + i);
                service.setName("name " + i);
                service.setType("SOS");
                service.setVersion("2.0.0");
                session.save(service);
            }

            ProcedureEntity procedure = new ProcedureEntity();
            procedure.setInsitu(true);
            procedure.setMobile(false);
            procedure.setName("procedureName");
            procedure.setDomainId("procedureDomain");
            session.save(procedure);

            CategoryEntity category = new CategoryEntity();
            category.setDescription("description");
            category.setDomainId("categoryDomain");
            category.setName("categoryName");
            session.save(category);

            FeatureEntity feature = new FeatureEntity();
            feature.setDescription("description");
            feature.setDomainId("featureDomain");
            feature.setName("featureName");

//            GeometryEntity geom = new GeometryEntity();
//            geom.setGeometry(JTSHelper.createGeometryFromWKT("POINT (52.7 7.52)", 4326));
//            feature.setGeometry(geom);
            session.save(feature);

            PhenomenonEntity phenomenon = new PhenomenonEntity();
            phenomenon.setDomainId("phenomenonDomain");
            phenomenon.setName("phenomenonName");
            session.save(phenomenon);

            UnitEntity unit = new UnitEntity();
            unit.setName("unitName");
            unit.setDomainId("unitDomain");
            session.save(unit);

            PlatformEntity platform = new PlatformEntity();
            platform.setName("platformName");
            platform.setDomainId("platformDomain");
            platform.setInsitu(true);
            platform.setMobile(false);
            session.save(platform);

            MeasurementDatasetEntity measurementDataset = new MeasurementDatasetEntity();
            measurementDataset.setProcedure(procedure);
            measurementDataset.setCategory(category);
            measurementDataset.setFeature(feature);
            measurementDataset.setPhenomenon(phenomenon);
            measurementDataset.setPlatform(platform);
            measurementDataset.setUnit(unit);
            measurementDataset.setFirstValueAt(new GregorianCalendar(2016, 9, 15, 1, 0, 0).getTime());
            measurementDataset.setLastValueAt(new GregorianCalendar(2016, 9, 15, 2, 0, 0).getTime());
            measurementDataset.setPublished(Boolean.TRUE);
            session.save(measurementDataset);
            session.flush();
            session.refresh(measurementDataset);

            for (int i = 0; i < 10; i++) {
                MeasurementDataEntity entity = new MeasurementDataEntity();
                entity.setTimestamp(new GregorianCalendar(2016, 9, 15, 1 * i, 0, 0).getTime());
                entity.setSeriesPkid(measurementDataset.getPkid());
                entity.setValue(1.0 * i);
                entity.setDeleted(Boolean.FALSE);
                session.save(entity);
                session.flush();
            }

            CountDatasetEntity countDataset = new CountDatasetEntity();
            countDataset.setProcedure(procedure);
            countDataset.setCategory(category);
            countDataset.setFeature(feature);
            countDataset.setPhenomenon(phenomenon);
            session.save(countDataset);
            session.flush();
            session.refresh(countDataset);

            CountDataEntity countData = new CountDataEntity();
            countData.setTimestamp(new Date());
            countData.setSeriesPkid(countDataset.getPkid());
            countData.setValue(123);
            countData.setDeleted(Boolean.FALSE);
            session.save(countData);

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
                    .extendWith("datasettypes", "measurement");
            DbQuery createFrom = DbQuery.createFrom(test);
            List<FeatureEntity> allInstances = featureDao.getAllInstances(createFrom);
            allInstances.stream().forEach((allInstance) -> {
                LOGGER.info(allInstance.toString());
            });

            FeatureEntity instance = featureDao.getInstance("1", createFrom);



            session.flush();
            transaction.commit();
        } finally {
            session.close();
        }

    }

}
