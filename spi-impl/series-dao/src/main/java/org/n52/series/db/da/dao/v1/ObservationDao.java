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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.n52.io.request.IoParameters;
import org.n52.series.db.da.v1.DbQuery;
import org.n52.series.db.da.beans.v1.ObservationEntity;
import org.n52.series.db.da.beans.v1.SeriesEntity;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.AbstractDbQuery;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ObservationDao extends AbstractDao<ObservationEntity> {

    private static final String COLUMN_SERIES_PKID = "seriesPkid";

    private static final String COLUMN_DELETED = "deleted";

    public ObservationDao(Session session) {
        super(session);
    }

    @Override
    public List<ObservationEntity> find(String search, DbQuery query) {
        return new ArrayList<>();
    }

    @Override
    public ObservationEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (ObservationEntity) session.get(ObservationEntity.class, key);
    }

    /**
     * Retrieves all available observations belonging to a particular series.
     *
     * @param series the entity to get all observations for.
     * @return all observation entities belonging to the series.
     * @throws org.n52.series.db.da.DataAccessException if accessing database
     * fails.
     */
    public List<ObservationEntity> getAllInstancesFor(SeriesEntity series) throws DataAccessException {
        return getAllInstancesFor(series, DbQuery.createFrom(IoParameters.createDefaults()));
    }

    /**
     * <p>
     * Retrieves all available observation instances.</p>
     *
     * @param parameters query parameters.
     * @return all instances matching the given query parameters.
     * @throws DataAccessException if accessing database fails.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ObservationEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq(COLUMN_DELETED, Boolean.FALSE));
        parameters.addTimespanTo(criteria);
        parameters.addPagingTo(criteria);
        return (List<ObservationEntity>) criteria.list();
    }

    /**
     * Retrieves all available observation instances belonging to a particular
     * series.
     *
     * @param series the series the observations belongs to.
     * @param parameters some query parameters to restrict result.
     * @return all observation entities belonging to the given series which
     * match the given query.
     * @throws DataAccessException if accessing database fails.
     */
    @SuppressWarnings("unchecked")
    public List<ObservationEntity> getAllInstancesFor(SeriesEntity series, AbstractDbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq(COLUMN_SERIES_PKID, series.getPkid()))
                .add(eq(COLUMN_DELETED, Boolean.FALSE));
        parameters.addTimespanTo(criteria);
        parameters.addPagingTo(criteria);
        return (List<ObservationEntity>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<ObservationEntity> getObservationsFor(SeriesEntity series, AbstractDbQuery query) {
        Criteria criteria = query.addTimespanTo(getDefaultCriteria())
                .add(eq(COLUMN_SERIES_PKID, series.getPkid()))
                .add(eq(COLUMN_DELETED, Boolean.FALSE));
        return criteria.list();
    }

    /**
     * Counts all observations not including deleted observations.
     *
     * @return amount of observations
     * @throws org.n52.series.db.da.DataAccessException if accessing database
     * fails.
     */
    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq(COLUMN_DELETED, Boolean.FALSE))
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return session.createCriteria(ObservationEntity.class);
    }

}
