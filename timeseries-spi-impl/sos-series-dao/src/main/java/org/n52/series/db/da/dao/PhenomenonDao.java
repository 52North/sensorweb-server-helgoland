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
package org.n52.series.db.da.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.io.request.IoParameters;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.beans.I18nPhenomenonEntity;
import org.n52.series.db.da.beans.PhenomenonEntity;

import com.google.common.base.Strings;

public class PhenomenonDao extends AbstractDao<PhenomenonEntity> {

    public PhenomenonDao(Session session) {
        super(session);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<PhenomenonEntity> find(String search, DbQuery query) {
        Criteria criteria = getDefaultCriteria();
        if (hasTranslation(query, I18nPhenomenonEntity.class)) {
            criteria = query.addLocaleTo(criteria, I18nPhenomenonEntity.class);
        }
        criteria.add(Restrictions.ilike("name", "%" + search + "%"));
        return criteria.list();
    }
    
    @Override
    public PhenomenonEntity getInstance(Long key) throws DataAccessException {
        return getInstance(key, DbQuery.createFrom(IoParameters.createDefaults()));
    }

    @Override
    public PhenomenonEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (PhenomenonEntity) session.get(PhenomenonEntity.class, key);
    }
    
    @Override
    public List<PhenomenonEntity> getAllInstances() throws DataAccessException {
        return getAllInstances(DbQuery.createFrom(IoParameters.createDefaults()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PhenomenonEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria("p");
        if (hasTranslation(parameters, I18nPhenomenonEntity.class)) {
            parameters.addLocaleTo(criteria, I18nPhenomenonEntity.class);
        }
        
        DetachedCriteria filter = parameters.createDetachedFilterCriteria("phenomenon");
        criteria.add(Subqueries.propertyIn("p.pkid", filter));
        
        parameters.addPagingTo(criteria);
        return (List<PhenomenonEntity>) criteria.list();
    }

    @Override
    protected Criteria getDefaultCriteria() {
    	return getDefaultCriteria(null);
    }
    
    private Criteria getDefaultCriteria(String alias) {
    	Criteria criteria;
    	if (Strings.isNullOrEmpty(alias)) {
    		criteria = session.createCriteria(PhenomenonEntity.class);
    	} else {
    		criteria = session.createCriteria(PhenomenonEntity.class, alias);
    	}
    	return criteria;
    }

}
