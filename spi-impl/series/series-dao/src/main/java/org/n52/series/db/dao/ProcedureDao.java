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
package org.n52.series.db.dao;

import static org.hibernate.criterion.Restrictions.eq;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.I18nProcedureEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ProcedureDao extends AbstractDao<ProcedureEntity> {

    private static final String SERIES_PROPERTY = "procedure";

    private static final String COLUMN_REFERENCE = "reference";

    public ProcedureDao(Session session) {
        super(session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProcedureEntity> find(DbQuery query) {
        Criteria criteria = translate(I18nProcedureEntity.class, getDefaultCriteria(), query)
                .add(Restrictions.ilike("name", "%" + query.getSearchTerm() + "%"));
        return query.addFilters(criteria, getSeriesProperty()).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProcedureEntity> getAllInstances(DbQuery query) throws DataAccessException {
        Criteria criteria = translate(I18nProcedureEntity.class, getDefaultCriteria(), query);
        return (List<ProcedureEntity>) query.addFilters(criteria, getSeriesProperty()).list();
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return super.getDefaultCriteria()
                .add(eq(COLUMN_REFERENCE, Boolean.FALSE));
    }

    @Override
    protected String getSeriesProperty() {
        return SERIES_PROPERTY;
    }

    @Override
    protected Class<ProcedureEntity> getEntityClass() {
        return ProcedureEntity.class;
    }

}
