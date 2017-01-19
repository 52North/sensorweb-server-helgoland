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
package org.n52.series.db.da;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.HibernateSessionStore;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.dao.CategoryDao;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.FeatureDao;
import org.n52.series.db.dao.PhenomenonDao;
import org.n52.series.db.dao.PlatformDao;
import org.n52.series.db.dao.ProcedureDao;
import org.n52.series.db.dao.DatasetDao;
import org.n52.series.db.dao.DbQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityCounter {

    @Autowired
    private HibernateSessionStore sessionStore;

    @Autowired
    private DbQueryFactory dbQueryFactory;

    public Integer countFeatures(DbQuery query) throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new FeatureDao(session).getCount(query);
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public Integer countOfferings(DbQuery query) throws DataAccessException {
        // offerings equals procedures in our case
        return countProcedures(query);
    }

    public Integer countProcedures(DbQuery query) throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new ProcedureDao(session).getCount(query);
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public Integer countPhenomena(DbQuery query) throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new PhenomenonDao(session).getCount(query);
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public Integer countCategories(DbQuery query) throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new CategoryDao(session).getCount(query);
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public Integer countPlatforms(DbQuery query) throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new PlatformDao(session).getCount(query);
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public Integer countDatasets(DbQuery query) throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new DatasetDao<DatasetEntity>(session, DatasetEntity.class).getCount(query);
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public Integer countStations() throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            DbQuery query = createBackwardsCompatibleQuery();
            return countFeatures(query);
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public Integer countTimeseries() throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            DbQuery query = createBackwardsCompatibleQuery();
            return countDatasets(query);
        } finally {
            sessionStore.returnSession(session);
        }
    }

    private DbQuery createBackwardsCompatibleQuery() {
        return dbQueryFactory.createFrom(IoParameters.createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "stationary", "insitu")
                .extendWith(Parameters.FILTER_DATASET_TYPES, "measurement"));
    }

}
