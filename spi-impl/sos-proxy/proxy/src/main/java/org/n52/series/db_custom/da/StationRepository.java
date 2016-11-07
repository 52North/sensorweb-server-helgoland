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
package org.n52.series.db_custom.da;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.StationOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db_custom.SessionAwareRepository;
import org.n52.series.db_custom.beans.DescribableTEntity;
import org.n52.series.db_custom.beans.FeatureTEntity;
import org.n52.series.db_custom.beans.MeasurementDatasetTEntity;
import org.n52.series.db_custom.dao.DatasetDao;
import org.n52.series.db_custom.dao.DbQuery;
import org.n52.series.db_custom.dao.FeatureDao;
import org.n52.series.spi.search.SearchResult;
import org.n52.series.spi.search.StationSearchResult;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.ResourceNotFoundException;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @deprecated since 2.0.0.
 */
@Deprecated
public class StationRepository extends SessionAwareRepository implements OutputAssembler<StationOutput> {

    @Override
    public boolean exists(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            FeatureDao dao = createDao(session);
            return dao.hasInstance(parseId(id), parameters, FeatureTEntity.class);
        } finally {
            returnSession(session);
        }
    }

    private FeatureDao createDao(Session session) {
        return new FeatureDao(session);
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
        Session session = getSession();
        try {
            FeatureDao stationDao = createDao(session);
            DbQuery query = DbQuery.createFrom(parameters);
            List<FeatureTEntity> found = stationDao.find(query);
            return convertToSearchResults(found, query);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableTEntity> found,
            DbQuery query) {
        String locale = query.getLocale();
        List<SearchResult> results = new ArrayList<>();
        for (DescribableTEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = searchResult.getLabelFrom(locale);
            results.add(new StationSearchResult(pkid, label));
        }
        return results;
    }

    @Override
    public List<StationOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            parameters.setDatabaseAuthorityCode(getDatabaseSrid());
            List<FeatureTEntity> allFeatures = getAllInstances(parameters, session);

            List<StationOutput> results = new ArrayList<>();
            for (FeatureTEntity featureEntity : allFeatures) {
                results.add(createCondensed(featureEntity, parameters));
            }

            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<StationOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            parameters.setDatabaseAuthorityCode(getDatabaseSrid());
            List<FeatureTEntity> allFeatures = getAllInstances(parameters, session);

            List<StationOutput> results = new ArrayList<>();
            for (FeatureTEntity featureEntity : allFeatures) {
                results.add(createExpanded(featureEntity, parameters, session));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    private List<FeatureTEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
        FeatureDao featureDao = createDao(session);
        return featureDao.getAllInstances(parameters);
    }

    @Override
    public StationOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            FeatureTEntity result = getInstance(id, parameters, session);
            if (result == null) {
                throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
            }
            return createExpanded(result, parameters, session);
        } finally {
            returnSession(session);
        }
    }

    FeatureTEntity getInstance(String id, DbQuery parameters, Session session) throws DataAccessException, BadRequestException {
        parameters.setDatabaseAuthorityCode(getDatabaseSrid());
        FeatureDao featureDao = createDao(session);
        return featureDao.getInstance(parseId(id), parameters);
    }

    public StationOutput getCondensedInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            parameters.setDatabaseAuthorityCode(getDatabaseSrid());
            FeatureDao featureDao = createDao(session);
            FeatureTEntity result = featureDao.getInstance(parseId(id), DbQuery.createFrom(IoParameters.createDefaults()));
            return createCondensed(result, parameters);
        } finally {
            returnSession(session);
        }
    }

    private StationOutput createExpanded(FeatureTEntity feature, DbQuery parameters, Session session) throws DataAccessException {
        DatasetDao<MeasurementDatasetTEntity> seriesDao = new DatasetDao<>(session, MeasurementDatasetTEntity.class);
        List<MeasurementDatasetTEntity> series = seriesDao.getInstancesWith(feature);
        StationOutput stationOutput = createCondensed(feature, parameters);
        stationOutput.setTimeseries(createTimeseriesList(series, parameters));
        return stationOutput;
    }

    private StationOutput createCondensed(FeatureTEntity entity, DbQuery parameters) {
        StationOutput stationOutput = new StationOutput();
        stationOutput.setGeometry(createPoint(entity));
        stationOutput.setId(Long.toString(entity.getPkid()));
        stationOutput.setLabel(entity.getLabelFrom(parameters.getLocale()));
        return stationOutput;
    }

    private Geometry createPoint(FeatureTEntity featureEntity) {
        return featureEntity.isSetGeometry()
                ? featureEntity.getGeometry(getDatabaseSrid())
                : null;
    }

}
