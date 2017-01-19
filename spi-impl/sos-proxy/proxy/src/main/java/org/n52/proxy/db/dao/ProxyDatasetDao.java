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
package org.n52.proxy.db.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DatasetDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyDatasetDao<T extends DatasetEntity> extends DatasetDao<T> implements InsertDao<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyDatasetDao.class);

    private static final String COLUMN_SERVICE_PKID = "service.pkid";
    private static final String COLUMN_CATEGORY_PKID = "category.pkid";
    private static final String COLUMN_FEATURE_PKID = "feature.pkid";
    private static final String COLUMN_PROCEDURE_PKID = "procedure.pkid";
    private static final String COLUMN_PHENOMENON_PKID = "phenomenon.pkid";
    private static final String COLUMN_UNIT_PKID = "unit.pkid";

    public ProxyDatasetDao(Session session) {
        super(session);
    }

    public ProxyDatasetDao(Session session, Class<T> clazz) {
        super(session, clazz);
    }

    @Override
    public T getOrInsertInstance(T dataset) {
        if (dataset.getUnit() != null) {
            dataset.setUnit(getOrInsertUnit(dataset.getUnit()));
        }
        DatasetEntity instance = getInstance(dataset);
        if (instance == null) {
            session.save(dataset);
            LOGGER.info("Save dataset: " + dataset);
            session.flush();
            session.refresh(dataset);
        } else {
            instance.setDeleted(Boolean.FALSE);
            session.update(instance);
            LOGGER.info("Mark dataset as undeleted: " + instance);
        }
        return dataset;
    }

    public UnitEntity getOrInsertUnit(UnitEntity unit) {
        UnitEntity instance = getUnit(unit);
        if (instance == null) {
            this.session.save(unit);
            instance = unit;
        }
        return instance;
    }

    public void markAsDeletedForService(ServiceEntity service) {
        List<T> datasets = getDatasetsForService(service);
        datasets.stream().map((dataset) -> {
            dataset.setDeleted(Boolean.TRUE);
            return dataset;
        }).forEach((dataset) -> {
            session.saveOrUpdate(dataset);
            LOGGER.info("Mark dataset as deleted: " + dataset);
        });
    }

    public void removeDeletedForService(ServiceEntity service) {
        List<T> datasets = getDeletedMarkDatasets(service);
        datasets.forEach((dataset) -> {
            session.delete(dataset);
            LOGGER.info("Delete dataset: " + dataset);
        });
        session.flush();
    }

    private UnitEntity getUnit(UnitEntity unit) {
        Criteria criteria = session.createCriteria(UnitEntity.class)
                .add(Restrictions.eq("name", unit.getName()))
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, unit.getService().getPkid()));
        return (UnitEntity) criteria.uniqueResult();
    }

    private DatasetEntity getInstance(DatasetEntity dataset) {
        Criteria criteria = getDefaultCriteria()
                .add(Restrictions.eq("datasetType", dataset.getDatasetType()))
                .add(Restrictions.eq(COLUMN_CATEGORY_PKID, dataset.getCategory().getPkid()))
                .add(Restrictions.eq(COLUMN_FEATURE_PKID, dataset.getFeature().getPkid()))
                .add(Restrictions.eq(COLUMN_PROCEDURE_PKID, dataset.getProcedure().getPkid()))
                .add(Restrictions.eq(COLUMN_PHENOMENON_PKID, dataset.getPhenomenon().getPkid()))
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, dataset.getService().getPkid()));
        if (dataset.getUnit() != null) {
            criteria.add(Restrictions.eq(COLUMN_UNIT_PKID, dataset.getUnit().getPkid()));
        }
        return (T) criteria.uniqueResult();
    }

    private List<T> getDatasetsForService(ServiceEntity service) {
        Criteria criteria = getDefaultCriteria()
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, service.getPkid()));
        return criteria.list();
    }

    private List<T> getDeletedMarkDatasets(ServiceEntity service) {
        Criteria criteria = getDefaultCriteria()
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, service.getPkid()))
                .add(Restrictions.eq("deleted", Boolean.TRUE));
        return criteria.list();
    }
}
