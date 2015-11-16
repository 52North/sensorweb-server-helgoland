/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.CategoryEntity;
import org.n52.series.db.da.beans.I18nCategoryEntity;

import com.google.common.base.Strings;

public class CategoryDao extends AbstractDao<CategoryEntity> {

    public CategoryDao(Session session) {
        super(session);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryEntity> find(String search, DbQuery query) {
        Criteria criteria = getDefaultCriteria();
        if (hasTranslation(query, I18nCategoryEntity.class)) {
            criteria = query.addLocaleTo(criteria, I18nCategoryEntity.class);
        }
        criteria.add(Restrictions.ilike("name", "%" + search + "%"));
        return criteria.list();
    }

//    @Override
//    public CategoryEntity getInstance(Long key) throws DataAccessException {
//        return getInstance(key, DbQuery.createFrom(IoParameters.createDefaults()));
//    }

    @Override
    public CategoryEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (CategoryEntity) session.get(CategoryEntity.class, key);
    }
    
//    @Override
//    public List<CategoryEntity> getAllInstances() throws DataAccessException {
//        return getAllInstances(DbQuery.createFrom(IoParameters.createDefaults()));
//    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria("c");
        if (hasTranslation(parameters, I18nCategoryEntity.class)) {
            parameters.addLocaleTo(criteria, I18nCategoryEntity.class);
        }
        
        DetachedCriteria filter = parameters.createDetachedFilterCriteria("category");
        criteria.add(Subqueries.propertyIn("c.pkid", filter));
        
        parameters.addPagingTo(criteria);
        return criteria.list();
    }

    @Override
    protected Criteria getDefaultCriteria() {
    	return getDefaultCriteria(null);
    }
    
    private Criteria getDefaultCriteria(String alias) {
    	Criteria criteria;
    	if (Strings.isNullOrEmpty(alias)) {
    		criteria = session.createCriteria(CategoryEntity.class);
    	} else {
    		criteria = session.createCriteria(CategoryEntity.class, alias);
    	}
    	return criteria;
    }

}
