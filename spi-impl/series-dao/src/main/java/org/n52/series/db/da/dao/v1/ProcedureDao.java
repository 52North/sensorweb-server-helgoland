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

import static org.hibernate.criterion.Restrictions.eq;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.series.db.da.v1.DbQuery;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.I18nProcedureEntity;
import org.n52.series.db.da.beans.ProcedureEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ProcedureDao extends AbstractDao<ProcedureEntity> {

    private static final String COLUMN_REFERENCE = "reference";

    public ProcedureDao(Session session) {
        super(session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProcedureEntity> find(String search, DbQuery query) {
        Criteria criteria = getDefaultCriteria();
        if (hasTranslation(query, I18nProcedureEntity.class)) {
            criteria = query.addLocaleTo(criteria, I18nProcedureEntity.class);
        }
        criteria.add(Restrictions.ilike("name", "%" + search + "%"));
        return criteria.list();
    }

//    @Override
//    public ProcedureEntity getInstance(Long key) throws DataAccessException {
//        return getInstance(key, DbQuery.createFrom(IoParameters.createDefaults()));
//    }
    @Override
    public ProcedureEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (ProcedureEntity) session.get(ProcedureEntity.class, key);
    }

//    @Override
//    public List<ProcedureEntity> getAllInstances() throws DataAccessException {
//        return getAllInstances(DbQuery.createFrom(IoParameters.createDefaults()));
//    }
    @Override
    @SuppressWarnings("unchecked")
    public List<ProcedureEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria("p", ProcedureEntity.class)
                .add(eq(COLUMN_REFERENCE, Boolean.FALSE));
        if (hasTranslation(parameters, I18nProcedureEntity.class)) {
            parameters.addLocaleTo(criteria, I18nProcedureEntity.class);
        }

        DetachedCriteria filter = parameters.createDetachedFilterCriteria("procedure");
        criteria.add(Subqueries.propertyIn("p.pkid", filter));

        parameters.addPagingTo(criteria);
        return (List<ProcedureEntity>) criteria.list();
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return getDefaultCriteria(null, ProcedureEntity.class);
    }

}
