/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.io.IoParameters;
import org.n52.series.api.v1.db.da.DataAccessException;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.api.v1.db.da.beans.FeatureEntity;
import org.n52.series.api.v1.db.da.beans.I18nFeatureEntity;

public class FeatureDao extends AbstractDao<FeatureEntity> {

    public FeatureDao(Session session) {
        super(session);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<FeatureEntity> find(String search, DbQuery query) {
        Criteria criteria = session.createCriteria(FeatureEntity.class);
        if (hasTranslation(query, I18nFeatureEntity.class)) {
            criteria = query.addLocaleTo(criteria, I18nFeatureEntity.class);
        }
        criteria.add(Restrictions.ilike("name", "%" + search + "%"));
        return criteria.list();
    }
    
    @Override
    public FeatureEntity getInstance(Long key) throws DataAccessException {
        return getInstance(key, DbQuery.createFrom(IoParameters.createDefaults()));
    }

    @Override
    public FeatureEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (FeatureEntity) session.get(FeatureEntity.class, key);
    }
    
    @Override
    public List<FeatureEntity> getAllInstances() throws DataAccessException {
        return getAllInstances(DbQuery.createFrom(IoParameters.createDefaults()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<FeatureEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = session.createCriteria(FeatureEntity.class, "f");
        if (hasTranslation(parameters, I18nFeatureEntity.class)) {
            parameters.addLocaleTo(criteria, I18nFeatureEntity.class);
        }
        
        DetachedCriteria filter = parameters.createDetachedFilterCriteria("feature");
        criteria.add(Subqueries.propertyIn("f.pkid", filter));
                
        parameters.addSpatialFilterTo(criteria, parameters);
        parameters.addPagingTo(criteria);
        return (List<FeatureEntity>) criteria.list();
    }
    
    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = session
                .createCriteria(FeatureEntity.class)
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

}
