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

import static org.hibernate.criterion.Projections.rowCount;
import static org.hibernate.criterion.Subqueries.propertyIn;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.I18nEntity;

public abstract class AbstractDao<T> implements GenericDao<T, Long> {

    protected Session session;

    public AbstractDao(Session session) {
        if (session == null) {
            throw new NullPointerException("Cannot operate on a null session.");
        }
        this.session = session;
    }

    public abstract List<T> find(DbQuery query);

    protected abstract Criteria getDefaultCriteria();
    
    protected abstract String getSeriesProperty();

    public boolean hasTranslation(DbQuery parameters, Class<? extends I18nEntity> clazz) {
        Criteria i18nCriteria = session.createCriteria(clazz);
        return parameters.checkTranslationForLocale(i18nCriteria);
    }
    
    @Override
    public Integer getCount(DbQuery query) throws DataAccessException {
        Criteria criteria = getDefaultCriteria().setProjection(rowCount());
        return ((Long)  addFilters(criteria, query)
                .uniqueResult()).intValue();
    }

    protected Criteria addFilters(Criteria criteria, DbQuery query) {
        String filterProperty = getSeriesProperty();
        DetachedCriteria filter = query.createDetachedFilterCriteria(filterProperty);
        criteria = query.addPlatformTypeFilter(filterProperty, criteria);
        criteria = query.addDatasetTypeFilter(filterProperty, criteria);
        return query.addSpatialFilterTo(criteria, query)
                .add(propertyIn(filterProperty + ".pkid", filter));
    }

    protected Criteria getDefaultCriteria(String alias, Class<? extends T> clazz) {
        Criteria criteria;
        if (alias == null || alias.isEmpty()) {
            criteria = session.createCriteria(clazz);
        } else {
            criteria = session.createCriteria(clazz, alias);
        }
        return criteria;
    }

    @Override
    public boolean hasInstance(Long id, Class<? extends T> clazz) {
        return session.get(clazz, id) != null;
    }
}
