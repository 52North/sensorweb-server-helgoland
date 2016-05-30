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
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.ext.GeometryEntity;
import org.n52.series.db.da.beans.ext.SamplingGeometryEntity;
import org.n52.series.db.da.dao.v1.AbstractDao;
import org.n52.series.db.da.v1.DbQuery;

public class SamplingGeometriesDao extends AbstractDao<GeometryEntity> {

    private static final String COLUMN_SERIES_PKID = "seriesPkid";
    private static final String COLUMN_DELETED = "deleted";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_GEOMETRY = "geometry";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_LON = "lon";

    public SamplingGeometriesDao(Session session) {
        super(session);
    }

    @Override
    public GeometryEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<GeometryEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        return get(parameters);
    }

    @Override
    public List<GeometryEntity> find(DbQuery query) {
        return get(query);
    }

    protected List<GeometryEntity> get(DbQuery parameters) {
        Criteria criteria = getDefaultCriteria();
        DetachedCriteria filter = parameters.createDetachedFilterCriteria("pkid");
        criteria.add(Subqueries.propertyIn(COLUMN_SERIES_PKID, filter));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc(COLUMN_TIMESTAMP));
        return (List<GeometryEntity>)criteria.list();
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return session.createCriteria(SamplingGeometryEntity.class);
    }

}
