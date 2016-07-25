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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.DatasetFactoryException;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetType;
import org.n52.io.response.dataset.SeriesParameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.SessionAwareRepository;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.SeriesDao;
import org.n52.series.spi.search.SearchResult;
import org.n52.web.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @param <T> the dataset's type this repository is responsible for.
 */
public class DatasetRepository<T extends Data>
        extends SessionAwareRepository
        implements OutputAssembler<DatasetOutput>{

    @Autowired
    private DataRepositoryFactory factory;

    @Override
    public boolean exists(String id) throws DataAccessException {
        Session session = getSession();
        try {
            String dbId = DatasetType.extractId(id);
            final String datasetType = DatasetType.extractType(id);
            DataRepository dataRepository = factory.create(datasetType);
            SeriesDao<? extends DatasetEntity> dao = getSeriesDao(datasetType, session);
            return dao.hasInstance(parseId(dbId), dataRepository.getEntityType());
        } catch (DatasetFactoryException ex) {
            throw new DataAccessException("Could not determine if id exists.", ex);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<DatasetOutput> getAllCondensed(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            List<DatasetOutput> results = new ArrayList<>();
            if (query.isSetDatasetTypeFilter()) {
                for (String datasetType : query.getDatasetTypes()) {
                    addCondensedResults(getSeriesDao(datasetType, session), query, results);
                }
            } else {
                // XXX filter on configured types
                addCondensedResults(getSeriesDao(DatasetEntity.class, session), query, results);
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    private void addCondensedResults(SeriesDao<? extends DatasetEntity> dao, DbQuery query, List<DatasetOutput> results) throws DataAccessException {
        for (DatasetEntity series : dao.getAllInstances(query)) {
            results.add(createCondensed(series, query));
        }
    }

    private SeriesDao<? extends DatasetEntity> getSeriesDao(String datasetType, Session session) throws DataAccessException {
        if ( !factory.isKnown(datasetType)) {
            throw new ResourceNotFoundException("unknown dataset type: " + datasetType);
        }
        try {
            DataRepository dataRepository = factory.create(datasetType);
            return getSeriesDao(dataRepository.getEntityType(), session);
        } catch (DatasetFactoryException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private SeriesDao<? extends DatasetEntity> getSeriesDao(Class<? extends DatasetEntity> clazz, Session session) {
        return new SeriesDao<>(session, clazz);
    }

    @Override
    public List<DatasetOutput> getAllExpanded(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            List<DatasetOutput> results = new ArrayList<>();
            if (query.isSetDatasetTypeFilter()) {
                for (String datasetType : query.getDatasetTypes()) {
                    addExpandedResults(getSeriesDao(datasetType, session), query, results, session);
                }
            } else {
                // XXX filter on configured types
                addExpandedResults(getSeriesDao(DatasetEntity.class, session), query, results, session);
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    private void addExpandedResults(SeriesDao<? extends DatasetEntity> dao, DbQuery query, List<DatasetOutput> results, Session session) throws DataAccessException {
        for (DatasetEntity series : dao.getAllInstances(query)) {
            results.add(createExpanded(series, query, session));
        }
    }

    @Override
    public DatasetOutput getInstance(String id, DbQuery query) throws DataAccessException {
            Session session = getSession();
        try {
            DatasetEntity< ? > instanceEntity = getInstanceEntity(id, query, session);
            return createExpanded(instanceEntity, query, session);
        } finally {
            returnSession(session);
        }
    }

    DatasetEntity<?> getInstanceEntity(String id, DbQuery query, Session session) throws DataAccessException {
        String seriesId = DatasetType.extractId(id);
        final String datasetType = DatasetType.extractType(id);
        SeriesDao<? extends DatasetEntity> dao = getSeriesDao(datasetType, session);
        return dao.getInstance(Long.parseLong(seriesId), query);
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters paramters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, String locale) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // XXX refactor generics
    private DatasetOutput createCondensed(DatasetEntity<?> series, DbQuery query) throws DataAccessException {
        DatasetOutput output = new DatasetOutput(series.getDatasetType()) {};
        output.setLabel(createSeriesLabel(series, query.getLocale()));
        output.setId(series.getPkid().toString());
        output.setHrefBase(urHelper.getSeriesHrefBaseUrl(query.getHrefBase()));
        return output;
    }

    // XXX refactor generics
    private DatasetOutput createExpanded(DatasetEntity<?> series, DbQuery query, Session session) throws DataAccessException {
        try {
            DatasetOutput result = createCondensed(series, query);
            result.setSeriesParameters(getParameters(series, query));
            result.setUom(series.getUnitI18nName(query.getLocale()));
            DataRepository dataRepository = factory.create(series.getDatasetType());
            result.setFirstValue(dataRepository.getFirstValue(series, session));
            result.setLastValue(dataRepository.getLastValue(series, session));
            return result;
        } catch (DatasetFactoryException ex) {
            throw new DataAccessException("Could not determine if id exists.", ex);
        }
    }

    private SeriesParameters getParameters(DatasetEntity<?> series, DbQuery query) throws DataAccessException {
        return createSeriesParameters(series, query);
    }

    private String createSeriesLabel(DatasetEntity<?> series, String locale) {
        String station = getLabelFrom(series.getFeature(), locale);
        String procedure = getLabelFrom(series.getProcedure(), locale);
        String phenomenon = getLabelFrom(series.getPhenomenon(), locale);
        StringBuilder sb = new StringBuilder();
        sb.append(phenomenon).append(" ");
        sb.append(procedure).append(", ");
        return sb.append(station).toString();
    }

    public DataRepositoryFactory getDataRepositoryFactory() {
        return factory;
    }

    public void setDataRepositoryFactory(DataRepositoryFactory factory) {
        this.factory = factory;
    }

}
