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
package org.n52.series.db.da.dao.v2;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.beans.v2.I18nSiteEntity;
import org.n52.series.db.da.beans.v2.SiteEntity;
import org.n52.series.db.da.dao.AbstractDao;

import com.google.common.base.Strings;

public class SiteDao extends AbstractDao<SiteEntity> {

	public SiteDao(Session session) {
		super(session);
	}

	@Override
    @SuppressWarnings("unchecked")
    public List<SiteEntity> find(String search, DbQuery query) {
        Criteria criteria = getDefaultCriteria();
        if (hasTranslation(query, I18nSiteEntity.class)) {
            criteria = query.addLocaleTo(criteria, I18nSiteEntity.class);
        }
        criteria.add(Restrictions.ilike("name", "%" + search + "%"));
        return criteria.list();
    }
    
//    @Override
//    public SiteEntity getInstance(Long key) throws DataAccessException {
//        return getInstance(key, DbQueryV2.createFrom(IoParameters.createDefaults()));
//    }

    @Override
    public SiteEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (SiteEntity) session.get(SiteEntity.class, key);
    }
    
//    @Override
//    public List<SiteEntity> getAllInstances() throws DataAccessException {
//        return getAllInstances(DbQueryV2.createFrom(IoParameters.createDefaults()));
//    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SiteEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria("s");
        if (hasTranslation(parameters, I18nSiteEntity.class)) {
            parameters.addLocaleTo(criteria, I18nSiteEntity.class);
        }
        
        // TODO add DetachedCriteria to DbQuery, is this required?
        DetachedCriteria filter = parameters.createDetachedFilterCriteria("feature");
        criteria.add(Subqueries.propertyIn("s.pkid", filter));
                
        parameters.addSpatialFilterTo(criteria, parameters);
        parameters.addPagingTo(criteria);
        return (List<SiteEntity>) criteria.list();
    }
    
    @Override
    protected Criteria getDefaultCriteria() {
    	return getDefaultCriteria(null);
    }
    
    private Criteria getDefaultCriteria(String alias) {
    	Criteria criteria;
    	if (Strings.isNullOrEmpty(alias)) {
    		criteria = session.createCriteria(SiteEntity.class);
    	} else {
    		criteria = session.createCriteria(SiteEntity.class, alias);
    	}
    	criteria.add(Restrictions.isNotNull("geom"));
    	return criteria;
    }

}
