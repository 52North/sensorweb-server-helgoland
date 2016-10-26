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
package org.n52.series.db.da;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.series.db.SessionAwareRepository;
import org.n52.series.db.beans.CategoryTEntity;
import org.n52.series.db.beans.DatasetTEntity;
import org.n52.series.db.beans.FeatureTEntity;
import org.n52.series.db.beans.PhenomenonTEntity;
import org.n52.series.db.beans.PlatformTEntity;
import org.n52.series.db.beans.ProcedureTEntity;
import org.n52.series.db.beans.ServiceTEntity;
import org.n52.series.db.beans.UnitTEntity;
import org.n52.series.db.dao.CategoryDao;
import org.n52.series.db.dao.DatasetDao;
import org.n52.series.db.dao.FeatureDao;
import org.n52.series.db.dao.PhenomenonDao;
import org.n52.series.db.dao.PlatformDao;
import org.n52.series.db.dao.ProcedureDao;
import org.n52.series.db.dao.ServiceDao;
import org.n52.series.db.dao.UnitDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class InsertRepository extends SessionAwareRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(InsertRepository.class);

    @Autowired
    private ServiceRepository serviceRepository;

    public ServiceRepository getServiceRepository() {
        return serviceRepository;
    }

    public void setServiceRepository(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public synchronized void insertDataset(DatasetTEntity dataset) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();

            ServiceTEntity service = insertService(dataset.getService(), session);
            ProcedureTEntity procedure = insertProcedure(dataset.getProcedure(), service, session);
            CategoryTEntity category = insertCategory(dataset.getCategory(), service, session);
            FeatureTEntity feature = insertFeature(dataset.getFeature(), service, session);
            PhenomenonTEntity phenomenon = insertPhenomenon(dataset.getPhenomenon(), service, session);
            UnitTEntity unit = insertUnit(dataset.getUnit(), service, session);

            insertDataset(dataset, category, procedure, feature, phenomenon, unit, service, session);

            session.flush();
            transaction.commit();
        } catch (Exception e) {
            LOGGER.error("Error occured while saving dataset: ", e);
        } finally {
            returnSession(session);
        }
    }

    ;

    private ServiceTEntity insertService(ServiceTEntity service, Session session) {
        return new ServiceDao(session).getOrInsertInstance(service);
    }

    private ProcedureTEntity insertProcedure(ProcedureTEntity procedure, ServiceTEntity service, Session session) {
        procedure.setService(service);
        return new ProcedureDao(session).getOrInsertInstance(procedure);
    }

    private CategoryTEntity insertCategory(CategoryTEntity category, ServiceTEntity service, Session session) {
        category.setService(service);
        return new CategoryDao(session).getOrInsertInstance(category);
    }

    private FeatureTEntity insertFeature(FeatureTEntity feature, ServiceTEntity service, Session session) {
        feature.setService(service);
        return new FeatureDao(session).getOrInsertInstance(feature);
    }

    private PhenomenonTEntity insertPhenomenon(PhenomenonTEntity phenomenon, ServiceTEntity service, Session session) {
        phenomenon.setService(service);
        return new PhenomenonDao(session).getOrInsertInstance(phenomenon);
    }

    private PlatformTEntity insertPlatform(PlatformTEntity platform, ServiceTEntity service, Session session) {
        platform.setService(service);
        return new PlatformDao(session).getOrInsertInstance(platform);
    }

    private UnitTEntity insertUnit(UnitTEntity unit, ServiceTEntity service, Session session) {
        unit.setService(service);
        return new UnitDao(session).getOrInsertInstance(unit);
    }

    private DatasetTEntity insertDataset(DatasetTEntity dataset, CategoryTEntity category, ProcedureTEntity procedure, FeatureTEntity feature, PhenomenonTEntity phenomenon, UnitTEntity unit, ServiceTEntity service, Session session) {
        dataset.setCategory(category);
        dataset.setProcedure(procedure);
        dataset.setFeature(feature);
        dataset.setPhenomenon(phenomenon);
        dataset.setUnit(unit);
        dataset.setService(service);
        return new DatasetDao(session).getOrInsertInstance(dataset);
    }

}
