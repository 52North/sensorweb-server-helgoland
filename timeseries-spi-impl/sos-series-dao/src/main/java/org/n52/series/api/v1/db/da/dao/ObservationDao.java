/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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

import static org.hibernate.criterion.Restrictions.eq;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.io.IoParameters;
import org.n52.series.api.v1.db.da.DataAccessException;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.api.v1.db.da.beans.ObservationEntity;
import org.n52.series.api.v1.db.da.beans.SeriesEntity;

public class ObservationDao extends AbstractDao<ObservationEntity> {

    private static final String COLUMN_SERIES_PKID = "seriesPkid";

    public ObservationDao(Session session) {
        super(session);
    }

    @Override
    public List<ObservationEntity> find(String search, DbQuery query) {
        return new ArrayList<ObservationEntity>();
    }

    @Override
    public ObservationEntity getInstance(Long key) throws DataAccessException {
        return getInstance(key, DbQuery.createFrom(IoParameters.createDefaults()));
    }

    @Override
    public ObservationEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (ObservationEntity) session.get(ObservationEntity.class, key);
    }

    /**
     * Retrieves all available observation instances.<br/>
     * <br/>
     * Do NOT use this method if you want observations belonging to a particular series. To gain only those
     * observation you have to use {@link #getAllInstancesFor(SeriesEntity)}.
     */
    @Override
    public List<ObservationEntity> getAllInstances() throws DataAccessException {
        return getAllInstances(DbQuery.createFrom(IoParameters.createDefaults()));
    }

    /**
     * Retrieves all available observation instances.<br/>
     * <br/>
     * Do NOT use this method if you want observations belonging to a particular series. To gain only those
     * observation you have to use {@link #getAllInstancesFor(SeriesEntity)}.
     */
    public List<ObservationEntity> getAllInstancesFor(SeriesEntity series) throws DataAccessException {
        return getAllInstancesFor(series, DbQuery.createFrom(IoParameters.createDefaults()));
    }

    /**
     * Retrieves all available observation instances.<br/>
     * <br/>
     * Do NOT use this method if you want observations belonging to a particular series. To gain only those
     * observation you have to use {@link #getAllInstancesFor(SeriesEntity, DbQuery)}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<ObservationEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = session.createCriteria(ObservationEntity.class);
        parameters.addTimespanTo(criteria);
        parameters.addPagingTo(criteria);
        return (List<ObservationEntity>) criteria.list();
    }

    /**
     * Retrieves all available observation instances belonging to a particular series.
     * 
     * @param series
     *        the series the observations belongs to.
     * @param parameters
     *        some query parameters to restrict result.
     * @return all observation entities belonging to the given series which match the given query.
     * @throws DataAccessException
     *         if accessing data from DB failed.
     */
    @SuppressWarnings("unchecked")
    public List<ObservationEntity> getAllInstancesFor(SeriesEntity series, DbQuery parameters) throws DataAccessException {
        Criteria criteria = session.createCriteria(ObservationEntity.class)
                .add(Restrictions.eq(COLUMN_SERIES_PKID, series.getPkid()));
        parameters.addTimespanTo(criteria);
        parameters.addPagingTo(criteria);
        return (List<ObservationEntity>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<ObservationEntity> getObservationsFor(SeriesEntity series, DbQuery query) {
        Criteria criteria = query.addTimespanTo(session
                .createCriteria(ObservationEntity.class))
                .add(eq(COLUMN_SERIES_PKID, series.getPkid()));
        return criteria.list();
    }

    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = session
                .createCriteria(ObservationEntity.class)
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

}
