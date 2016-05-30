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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.io.request.IoParameters;
import org.n52.series.db.da.AbstractDbQuery;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.ext.AbstractObservationEntity;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.v1.DbQuery;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @param <T>
 */
@Transactional
public class ObservationDao<T extends AbstractObservationEntity> extends AbstractDao<T> {

    private static final String COLUMN_SERIES_PKID = "seriesPkid";

    private static final String COLUMN_DELETED = "deleted";

    private static final String COLUMN_TIMESTAMP = "timestamp";

    private final Class<T> entityType;

    public ObservationDao(Session session) {
        super(session);
        this.entityType = (Class<T>) AbstractObservationEntity.class;
    }

    @Override
    public List<T> find(DbQuery query) {
        return Collections.emptyList();
    }

    @Override
    public T getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (T) session.get(entityType, key);
    }

    /**
     * Retrieves all available observations belonging to a particular series.
     *
     * @param series the entity to get all observations for.
     * @return all observation entities belonging to the series.
     * @throws org.n52.series.db.da.DataAccessException if accessing database
     * fails.
     */
    public List<T> getAllInstancesFor(AbstractSeriesEntity<T> series) throws DataAccessException {
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
    public List<T> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria();
        parameters.addTimespanTo(criteria);
        parameters.addPagingTo(criteria);
        return (List<T>) criteria.list();
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
    public List<T> getAllInstancesFor(AbstractSeriesEntity<?> series, AbstractDbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq(COLUMN_SERIES_PKID, series.getPkid()));
        parameters.addTimespanTo(criteria);
        parameters.addPagingTo(criteria);
        return (List<T>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> getObservationsFor(AbstractSeriesEntity<T> series, AbstractDbQuery query) {
        Criteria criteria = query.addTimespanTo(getDefaultCriteria())
                .add(eq(COLUMN_SERIES_PKID, series.getPkid()));
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
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return session.createCriteria(entityType).add(eq(COLUMN_DELETED, Boolean.FALSE));
    }

    @SuppressWarnings("unchecked")
    public List<AbstractObservationEntity> getInstancesFor(Date timestamp, AbstractSeriesEntity series, DbQuery parameters) {
        Criteria criteria = getDefaultCriteria()
                .add(Restrictions.eq(COLUMN_SERIES_PKID, series.getPkid()))
                .add(Restrictions.eq(COLUMN_TIMESTAMP, timestamp));

        DetachedCriteria filter = parameters.createDetachedFilterCriteria("pkid");
        criteria.add(Subqueries.propertyIn(COLUMN_SERIES_PKID, filter));
        return criteria.list();
    }

}
