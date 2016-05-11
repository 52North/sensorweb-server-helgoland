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
package org.n52.series.db.da.v1;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.series.db.da.dao.v1.CategoryDao;
import org.n52.series.db.da.dao.v1.FeatureDao;
import org.n52.series.db.da.dao.v1.PhenomenonDao;
import org.n52.series.db.da.dao.v1.ProcedureDao;
import org.n52.series.db.da.dao.v1.SeriesDao;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.da.beans.ext.MeasurementSeriesEntity;

public class EntityCounter {

    private final SessionAwareRepository<DbQuery> repository = new ExtendedSessionAwareRepository() {
        @Override
        protected DbQuery getDbQuery(IoParameters parameters) {
            return DbQuery.createFrom(parameters);
        }
    };

    public int countStations() throws DataAccessException {
        return countFeatures();
    }

    public int countFeatures() throws DataAccessException {
        Session session = repository.getSession();
        try {
            return new FeatureDao(session).getCount();
        } finally {
            repository.returnSession(session);
        }
    }

    public int countOfferings() throws DataAccessException {
        // offerings equals procedures in our case
        return countProcedures();
    }

    public int countProcedures() throws DataAccessException {
        Session session = repository.getSession();
        try {
            return new ProcedureDao(session).getCount();
        } finally {
            repository.returnSession(session);
        }
    }

    public int countPhenomena() throws DataAccessException {
        Session session = repository.getSession();
        try {
            return new PhenomenonDao(session).getCount();
        } finally {
            repository.returnSession(session);
        }
    }

    public int countCategories() throws DataAccessException {
        Session session = repository.getSession();
        try {
            return new CategoryDao(session).getCount();
        } finally {
            repository.returnSession(session);
        }
    }

    public int countTimeseries() throws DataAccessException {
        Session session = repository.getSession();
        try {
//            return new SeriesDao<>(session, MeasurementSeriesEntity.class).getCount();
            return new SeriesDao<MeasurementSeriesEntity>(session).getCount();
        } finally {
            repository.returnSession(session);
        }
    }
}
