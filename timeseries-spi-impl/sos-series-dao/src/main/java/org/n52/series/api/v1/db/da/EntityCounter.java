/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.series.api.v1.db.da;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.series.api.v1.db.da.beans.DescribableEntity;
import org.n52.series.api.v1.db.da.beans.I18nEntity;
import org.n52.series.api.v1.db.da.beans.ServiceInfo;
import org.n52.series.api.v1.db.da.dao.CategoryDao;
import org.n52.series.api.v1.db.da.dao.FeatureDao;
import org.n52.series.api.v1.db.da.dao.PhenomenonDao;
import org.n52.series.api.v1.db.da.dao.ProcedureDao;
import org.n52.series.api.v1.db.da.dao.SeriesDao;
import org.n52.sensorweb.v1.spi.search.SearchResult;

public class EntityCounter {

    private SessionAwareRepository repository = new SessionAwareRepository(new ServiceInfo()) {
        /**
         * Not for use in this context
         */
        @Override
        public Collection<SearchResult> searchFor(String searchString, String locale) {
            return null;
        }

        /**
         * Not for use in this context
         */
        @Override
        protected List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity< ? extends I18nEntity>> found,
                                                            String locale) {
            return null;
        }
    };

    public int countStations() throws DataAccessException {
        return countFeatures();
    }

    public int countFeatures() throws DataAccessException {
        Session session = repository.getSession();
        try {
            return new FeatureDao(session).getCount();
        }
        finally {
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
        }
        finally {
            repository.returnSession(session);
        }
    }

    public int countPhenomena() throws DataAccessException {
        Session session = repository.getSession();
        try {
            return new PhenomenonDao(session).getCount();
        }
        finally {
            repository.returnSession(session);
        }
    }

    public int countCategories() throws DataAccessException {
        Session session = repository.getSession();
        try {
            return new CategoryDao(session).getCount();
        }
        finally {
            repository.returnSession(session);
        }
    }

    public int countTimeseries() throws DataAccessException {
        Session session = repository.getSession();
        try {
            return new SeriesDao(session).getCount();
        }
        finally {
            repository.returnSession(session);
        }
    }
}
