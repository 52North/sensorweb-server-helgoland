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
package org.n52.series.db.da.dao.v1.ext;

import static org.hibernate.criterion.Restrictions.eq;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.io.request.Parameters;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.ext.AbstractObservationEntity;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.dao.v1.AbstractDao;
import org.n52.series.db.da.v1.DbQuery;

public class SeriesDao extends AbstractDao<AbstractSeriesEntity<AbstractObservationEntity>> {

    private static final String COLUMN_KEY = "pkid";

    public SeriesDao(Session session) {
        super(session);
    }

    @Override
    public List<AbstractSeriesEntity<AbstractObservationEntity>> find(DbQuery query) {
        throw new UnsupportedOperationException("not implemented yet.");
    }

    @Override
    public AbstractSeriesEntity getInstance(Long key, DbQuery parameters)
            throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq("pkid", key));
        return (AbstractSeriesEntity) criteria.uniqueResult();
    }

    @Override
    public List<AbstractSeriesEntity<AbstractObservationEntity>> getAllInstances(DbQuery parameters)
            throws DataAccessException {
        Criteria criteria = getDefaultCriteria("series"); // TODO filter
        addRestrictions(criteria, parameters);
        parameters.addPagingTo(criteria);
        return (List<AbstractSeriesEntity<AbstractObservationEntity>>) criteria.list();
    }

    private void addRestrictions(Criteria criteria, DbQuery parameters) {
        if (parameters.getParameters().containsParameter(Parameters.OBSERVATION_TYPE)) {
            criteria.add(Restrictions.eq(AbstractSeriesEntity.OBSERVATION_TYPE,
                    parameters.getParameters().getAsString(Parameters.OBSERVATION_TYPE)));
        }
        if (parameters.getParameters().containsParameter(Parameters.PLATFORMS)) {
            criteria.createCriteria(AbstractSeriesEntity.PLATFORM).add(
                    Restrictions.in(COLUMN_KEY, parameters.parseToIds(parameters.getParameters().getPlatforms())));
        }
        if (parameters.getParameters().containsParameter(Parameters.PROCEDURE)) {
            criteria.createCriteria(AbstractSeriesEntity.PROCEDURE)
                    .add(Restrictions.eq(COLUMN_KEY, parameters.parseToId(parameters.getParameters().getProcedure())));
        }
        if (parameters.getParameters().containsParameter(Parameters.PHENOMENON)) {
            criteria.createCriteria(AbstractSeriesEntity.PHENOMENON).add(
                    Restrictions.eq(COLUMN_KEY, parameters.parseToId(parameters.getParameters().getPhenomenon())));
        }
        if (parameters.getParameters().containsParameter(Parameters.FEATURE)) {
            criteria.createCriteria(AbstractSeriesEntity.FEATURE)
                    .add(Restrictions.eq(COLUMN_KEY, parameters.parseToId(parameters.getParameters().getFeature())));
        }
        if (parameters.getParameters().containsParameter(Parameters.CATEGORY)) {
            criteria.createCriteria(AbstractSeriesEntity.CATEGORY)
                    .add(Restrictions.eq(COLUMN_KEY, parameters.parseToId(parameters.getParameters().getCategory())));
        }
    }

    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return getDefaultCriteria(null);
    }

    private Criteria getDefaultCriteria(String alias) {
        return session.createCriteria(AbstractSeriesEntity.class, alias);
    }


}
