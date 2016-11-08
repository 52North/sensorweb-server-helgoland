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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.series.db.SessionAwareRepository;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db_custom.beans.DatasetTEntity;
import org.n52.series.db_custom.beans.FeatureTEntity;
import org.n52.series.db_custom.beans.ProcedureTEntity;
import org.n52.series.db_custom.dao.CategoryDao;
import org.n52.series.db_custom.dao.DatasetDao;
import org.n52.series.db_custom.dao.FeatureDao;
import org.n52.series.db_custom.dao.PhenomenonDao;
import org.n52.series.db_custom.dao.ProcedureDao;
import org.n52.series.db_custom.dao.ServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertRepository extends SessionAwareRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(InsertRepository.class);

//    @Autowired
//    private ServiceRepository proxyServiceRepository;
//
//    public ServiceRepository getProxyServiceRepository() {
//        return proxyServiceRepository;
//    }
//
//    public void setProxyServiceRepository(ServiceRepository proxyServiceRepository) {
//        this.proxyServiceRepository = proxyServiceRepository;
//    }

    public synchronized void prepareInserting(ServiceEntity service) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();
            new DatasetDao(session).markAsDeletedForService(service);
            session.flush();
            transaction.commit();
        } finally {
            returnSession(session);
        }
    }

    public void cleanUp(ServiceEntity service) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();

            new DatasetDao(session).removeDeletedForService(service);
            new CategoryDao(session).clearUnusedForService(service);
            new ProcedureDao(session).clearUnusedForService(service);
            new FeatureDao(session).clearUnusedForService(service);
            new PhenomenonDao(session).clearUnusedForService(service);

            session.flush();
            transaction.commit();
        } finally {
            returnSession(session);
        }
    }

    public ServiceEntity insertService(ServiceEntity service) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();
            ServiceEntity insertedService = insertService(service, session);
            session.flush();
            transaction.commit();
            return insertedService;
        } finally {
            returnSession(session);
        }
    }

    public synchronized void insertDataset(DatasetTEntity dataset) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();

            ProcedureTEntity procedure = insertProcedure(dataset.getProcedure(), session);
            CategoryEntity category = insertCategory(dataset.getCategory(), session);
            FeatureTEntity feature = insertFeature(dataset.getFeature(), session);
            PhenomenonEntity phenomenon = insertPhenomenon(dataset.getPhenomenon(), session);

            insertDataset(dataset, category, procedure, feature, phenomenon, session);

            session.flush();
            transaction.commit();
        } catch (HibernateException e) {
            LOGGER.error("Error occured while saving dataset: ", e);
        } finally {
            returnSession(session);
        }
    }

    private ServiceEntity insertService(ServiceEntity service, Session session) {
        return new ServiceDao(session).getOrInsertInstance(service);
    }

    private ProcedureTEntity insertProcedure(ProcedureTEntity procedure, Session session) {
        return new ProcedureDao(session).getOrInsertInstance(procedure);
    }

    private CategoryEntity insertCategory(CategoryEntity category, Session session) {
        return new CategoryDao(session).getOrInsertInstance(category);
    }

    private FeatureTEntity insertFeature(FeatureTEntity feature, Session session) {
        return new FeatureDao(session).getOrInsertInstance(feature);
    }

    private PhenomenonEntity insertPhenomenon(PhenomenonEntity phenomenon, Session session) {
        return new PhenomenonDao(session).getOrInsertInstance(phenomenon);
    }

    private DatasetTEntity insertDataset(DatasetTEntity dataset, CategoryEntity category, ProcedureTEntity procedure, FeatureTEntity feature, PhenomenonEntity phenomenon, Session session) {
        dataset.setCategory(category);
        dataset.setProcedure(procedure);
        dataset.setFeature(feature);
        dataset.setPhenomenon(phenomenon);
        if (dataset.getUnit() != null) {
            dataset.getUnit().setService(dataset.getService());
        }
        return new DatasetDao(session).getOrInsertInstance(dataset);
    }

}
