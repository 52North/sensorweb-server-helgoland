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
package org.n52.series.db.da;

import org.hibernate.Session;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.HibernateSessionStore;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.dao.CategoryDao;
import org.n52.series.db.dao.FeatureDao;
import org.n52.series.db.dao.PhenomenonDao;
import org.n52.series.db.dao.ProcedureDao;
import org.n52.series.db.dao.SeriesDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityCounter {

    @Autowired
    private HibernateSessionStore sessionStore;

    public int countStations() throws DataAccessException {
        return countFeatures();
    }

    public int countFeatures() throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new FeatureDao(session).getCount();
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public int countOfferings() throws DataAccessException {
        // offerings equals procedures in our case
        return countProcedures();
    }

    public int countProcedures() throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new ProcedureDao(session).getCount();
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public int countPhenomena() throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new PhenomenonDao(session).getCount();
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public int countCategories() throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new CategoryDao(session).getCount();
        } finally {
            sessionStore.returnSession(session);
        }
    }

    public int countTimeseries() throws DataAccessException {
        Session session = sessionStore.getSession();
        try {
            return new SeriesDao<MeasurementDatasetEntity>(session, MeasurementDatasetEntity.class).getCount();
        } finally {
            sessionStore.returnSession(session);
        }
    }
}
