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
package org.n52.series.db.dao;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.ServiceTEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ServiceDao extends AbstractDao<ServiceTEntity> implements InsertDao<ServiceTEntity> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceDao.class);

    private static final String SERIES_PROPERTY = "service";

    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TYPE = "type";

    public ServiceDao(Session session) {
        super(session);
    }

    @Override
    public List<ServiceTEntity> find(DbQuery query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Class<ServiceTEntity> getEntityClass() {
        return ServiceTEntity.class;
    }

    @Override
    protected String getSeriesProperty() {
        return SERIES_PROPERTY;
    }

    @Override
    public List<ServiceTEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria();
        return criteria.list();
    }

    @Override
    public ServiceTEntity getOrInsertInstance(ServiceTEntity service) {
        ServiceTEntity instance = getInstance(service);
        if (instance == null) {
            this.session.save(service);
            LOGGER.info("Save service: " + service);
            instance = service;
        }
        return instance;
    }

    private ServiceTEntity getInstance(ServiceTEntity service) {
        Criteria criteria = session.createCriteria(getEntityClass())
                .add(Restrictions.eq(COLUMN_NAME, service.getName()))
                .add(Restrictions.eq(COLUMN_TYPE, service.getType()));
        return (ServiceTEntity) criteria.uniqueResult();
    }

}
