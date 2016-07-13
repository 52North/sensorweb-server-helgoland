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
package org.n52.series.db.da.dao.v1;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.I18nPhenomenonEntity;
import org.n52.series.db.da.beans.PhenomenonEntity;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class PhenomenonDao extends AbstractDao<PhenomenonEntity> {

    public PhenomenonDao(Session session) {
        super(session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PhenomenonEntity> find(DbQuery query) {
        Criteria criteria = getDefaultCriteria();
        if (hasTranslation(query, I18nPhenomenonEntity.class)) {
            criteria = query.addLocaleTo(criteria, I18nPhenomenonEntity.class);
        }
        criteria.add(Restrictions.ilike("name", "%" + query.getSearchTerm() + "%"));
        return addFiltersTo(criteria, query).list();
    }

    @Override
    public PhenomenonEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (PhenomenonEntity) session.get(PhenomenonEntity.class, key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PhenomenonEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria("phenomenon", PhenomenonEntity.class);
        if (hasTranslation(parameters, I18nPhenomenonEntity.class)) {
            parameters.addLocaleTo(criteria, I18nPhenomenonEntity.class);
        }
        return (List<PhenomenonEntity>) addFiltersTo(criteria, parameters).list();
    }

    private Criteria addFiltersTo(Criteria criteria, DbQuery parameters) {
        DetachedCriteria filter = parameters.createDetachedFilterCriteria("phenomenon");
        return parameters.addPlatformTypesFilter("phenomenon", criteria)
                .add(Subqueries.propertyIn("phenomenon.pkid", filter));
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return getDefaultCriteria(null, PhenomenonEntity.class);
    }

}
