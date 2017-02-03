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
package org.n52.proxy.db.da;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.proxy.db.beans.RelatedFeatureEntity;
import org.n52.proxy.db.beans.RelatedFeatureRoleEntity;
import org.n52.proxy.db.dao.ProxyCategoryDao;
import org.n52.proxy.db.dao.ProxyDatasetDao;
import org.n52.proxy.db.dao.ProxyFeatureDao;
import org.n52.proxy.db.dao.ProxyOfferingDao;
import org.n52.proxy.db.dao.ProxyPhenomenonDao;
import org.n52.proxy.db.dao.ProxyProcedureDao;
import org.n52.proxy.db.dao.ProxyRelatedFeatureDao;
import org.n52.proxy.db.dao.ProxyRelatedFeatureRoleDao;
import org.n52.proxy.db.dao.ProxyServiceDao;
import org.n52.series.db.SessionAwareRepository;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertRepository extends SessionAwareRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(InsertRepository.class);

    public synchronized void prepareInserting(ProxyServiceEntity service) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();
            new ProxyDatasetDao(session).markAsDeletedForService(service);
            session.flush();
            transaction.commit();
        } finally {
            returnSession(session);
        }
    }

    public void cleanUp(ProxyServiceEntity service) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();

            new ProxyDatasetDao(session).removeDeletedForService(service);
            new ProxyCategoryDao(session).clearUnusedForService(service);
            new ProxyOfferingDao(session).clearUnusedForService(service);
            new ProxyProcedureDao(session).clearUnusedForService(service);
            new ProxyFeatureDao(session).clearUnusedForService(service);
            new ProxyPhenomenonDao(session).clearUnusedForService(service);
            new ProxyRelatedFeatureDao(session).clearUnusedForService(service);

            session.flush();
            transaction.commit();
        } finally {
            returnSession(session);
        }
    }

    public ProxyServiceEntity insertService(ProxyServiceEntity service) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();
            ProxyServiceEntity insertedService = insertService(service, session);
            session.flush();
            transaction.commit();
            return insertedService;
        } finally {
            returnSession(session);
        }
    }

    public void insertOffering(OfferingEntity offeringEntity) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();
            insertOffering(offeringEntity, session);
            session.flush();
            transaction.commit();
        } finally {
            returnSession(session);
        }

    }

    public synchronized void insertDataset(DatasetEntity dataset) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();

            ProcedureEntity procedure = insertProcedure(dataset.getProcedure(), session);
            CategoryEntity category = insertCategory(dataset.getCategory(), session);
            OfferingEntity offering = insertOffering(dataset.getOffering(), session);
            FeatureEntity feature = insertFeature(dataset.getFeature(), session);
            PhenomenonEntity phenomenon = insertPhenomenon(dataset.getPhenomenon(), session);

            insertDataset(dataset, category, procedure, offering, feature, phenomenon, session);

            session.flush();
            transaction.commit();
        } catch (HibernateException e) {
            LOGGER.error("Error occured while saving dataset: ", e);
        } finally {
            returnSession(session);
        }
    }

    public synchronized void insertRelatedFeature(Collection<RelatedFeatureEntity> relatedFeatures) {
        Session session = getSession();
        try {
            Transaction transaction = session.beginTransaction();
            for (RelatedFeatureEntity relatedFeature : relatedFeatures) {
                insertRelatedFeature(relatedFeature, session);
            }
            session.flush();
            transaction.commit();
        } catch (HibernateException e) {
            LOGGER.error("Error occured while saving dataset: ", e);
        } finally {
            returnSession(session);
        }
    }

    private ProxyServiceEntity insertService(ProxyServiceEntity service, Session session) {
        return new ProxyServiceDao(session).getOrInsertInstance(service);
    }

    private ProcedureEntity insertProcedure(ProcedureEntity procedure, Session session) {
        return new ProxyProcedureDao(session).getOrInsertInstance(procedure);
    }

    private CategoryEntity insertCategory(CategoryEntity category, Session session) {
        return new ProxyCategoryDao(session).getOrInsertInstance(category);
    }

    private FeatureEntity insertFeature(FeatureEntity feature, Session session) {
        return new ProxyFeatureDao(session).getOrInsertInstance(feature);
    }

    private PhenomenonEntity insertPhenomenon(PhenomenonEntity phenomenon, Session session) {
        return new ProxyPhenomenonDao(session).getOrInsertInstance(phenomenon);
    }

    private OfferingEntity insertOffering(OfferingEntity offering, Session session) {
        return new ProxyOfferingDao(session).getOrInsertInstance(offering);
    }

    private RelatedFeatureEntity insertRelatedFeature(RelatedFeatureEntity relatedFeature, Session session) {
        // insert related feature roles
        Set<RelatedFeatureRoleEntity> roles =
                new HashSet<RelatedFeatureRoleEntity>(relatedFeature.getRelatedFeatureRoles().size());
        for (RelatedFeatureRoleEntity relatedFeatureRole : relatedFeature.getRelatedFeatureRoles()) {
            roles.add(insertRelatedFeatureRole(relatedFeatureRole, session));
        }
        relatedFeature.setRelatedFeatureRoles(roles);
        // insert offerings
        Set<OfferingEntity> offerings = new HashSet<OfferingEntity>(relatedFeature.getOfferings().size());
        for (OfferingEntity offering : relatedFeature.getOfferings()) {
            offerings.add(insertOffering(offering, session));
        }
        relatedFeature.setOfferings(offerings);
        return new ProxyRelatedFeatureDao(session).getOrInsertInstance(relatedFeature);
    }

    private RelatedFeatureRoleEntity insertRelatedFeatureRole(RelatedFeatureRoleEntity relatedFeatureRole, Session session) {
        return new ProxyRelatedFeatureRoleDao(session).getOrInsertInstance(relatedFeatureRole);
    }

    private DatasetEntity insertDataset(DatasetEntity dataset, CategoryEntity category, ProcedureEntity procedure, OfferingEntity offering, FeatureEntity feature, PhenomenonEntity phenomenon, Session session) {
        dataset.setCategory(category);
        dataset.setProcedure(procedure);
        dataset.setOffering(offering);
        dataset.setFeature(feature);
        dataset.setPhenomenon(phenomenon);
        if (dataset.getUnit() != null) {
            dataset.getUnit().setService(dataset.getService());
        }
        return new ProxyDatasetDao(session).getOrInsertInstance(dataset);
    }

}
