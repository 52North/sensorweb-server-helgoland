/**
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.series.api.v1.db.da.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.io.IoParameters;
import org.n52.series.api.v1.db.da.DataAccessException;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.api.v1.db.da.beans.CategoryEntity;
import org.n52.series.api.v1.db.da.beans.I18nCategoryEntity;

public class CategoryDao extends AbstractDao<CategoryEntity> {

    public CategoryDao(Session session) {
        super(session);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryEntity> find(String search, DbQuery query) {
        Criteria criteria = getDefaultCriteria("category");
        if (hasTranslation(query, I18nCategoryEntity.class)) {
            criteria = query.addLocaleTo(criteria, I18nCategoryEntity.class);
        }
        criteria.add(Restrictions.ilike("name", "%" + search + "%"));
        return criteria.list();
    }

    @Override
    public CategoryEntity getInstance(Long key) throws DataAccessException {
        return getInstance(key, DbQuery.createFrom(IoParameters.createDefaults()));
    }

    @Override
    public CategoryEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (CategoryEntity) getDefaultCriteria("category")
                .add(Restrictions.eq("pkid", key))
                .uniqueResult();
//        return (CategoryEntity) session.get(CategoryEntity.class, key);
    }
    
    @Override
    public List<CategoryEntity> getAllInstances() throws DataAccessException {
        return getAllInstances(DbQuery.createFrom(IoParameters.createDefaults()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria("category");
        if (hasTranslation(parameters, I18nCategoryEntity.class)) {
            parameters.addLocaleTo(criteria, I18nCategoryEntity.class);
        }
        
        criteria = parameters.addDetachedFilters("category", criteria);
        parameters.addPagingTo(criteria);
        return criteria.list();
    }

    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = getDefaultCriteria("category")
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

    @Override
    protected String getDefaultAlias() {
        return "category";
    }

    @Override
    protected Class<?> getEntityClass() {
        return CategoryEntity.class;
    }
}
