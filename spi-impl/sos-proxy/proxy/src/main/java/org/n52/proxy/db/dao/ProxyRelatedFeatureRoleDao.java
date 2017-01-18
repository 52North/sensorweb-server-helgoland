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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.n52.proxy.db.beans.RelatedFeatureRoleEntity;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.dao.AbstractDao;
import org.n52.series.db.dao.DbQuery;

public class ProxyRelatedFeatureRoleDao extends AbstractDao<RelatedFeatureRoleEntity> implements InsertDao<RelatedFeatureRoleEntity>, ClearDao<RelatedFeatureRoleEntity> {


    private static final String SERIES_PROPERTY = "relatedFeature";

    public ProxyRelatedFeatureRoleDao(Session session) {
        super(session);
    }

    @Override
    public List<RelatedFeatureRoleEntity> find(DbQuery query) {
        return new ArrayList<RelatedFeatureRoleEntity>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RelatedFeatureRoleEntity> getAllInstances(DbQuery query) throws DataAccessException {
        return (List<RelatedFeatureRoleEntity>) getDefaultCriteria().list();
    }

    @Override
    protected String getSeriesProperty() {
        return SERIES_PROPERTY;
    }

    @Override
    protected Class<RelatedFeatureRoleEntity> getEntityClass() {
        return RelatedFeatureRoleEntity.class;
    }

    @Override
    public RelatedFeatureRoleEntity getOrInsertInstance(RelatedFeatureRoleEntity relatedFeature) {
        RelatedFeatureRoleEntity instance = getInstance(relatedFeature);
        if (instance == null) {
            this.session.save(relatedFeature);
            instance = relatedFeature;
        }
        return instance;
    }

    @Override
    public void clearUnusedForService(ServiceEntity service) {
        // nothing to do
    }

    private RelatedFeatureRoleEntity getInstance(RelatedFeatureRoleEntity relatedFeature) {
        Criteria criteria = session.createCriteria(getEntityClass());;
        return (RelatedFeatureRoleEntity) criteria.uniqueResult();
    }

}
