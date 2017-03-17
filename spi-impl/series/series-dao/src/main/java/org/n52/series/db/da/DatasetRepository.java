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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.DatasetFactoryException;
import org.n52.io.request.FilterResolver;
import org.n52.io.request.IoParameters;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetType;
import org.n52.io.response.dataset.SeriesParameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.dao.DatasetDao;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.spi.search.DatasetSearchResult;
import org.n52.series.spi.search.SearchResult;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @param <T> the dataset's type this repository is responsible for.
 */
public class DatasetRepository<T extends Data>
        extends SessionAwareRepository
        implements OutputAssembler<DatasetOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetRepository.class);

    @Autowired
    private IDataRepositoryFactory factory;

    @Autowired
    private PlatformRepository platformRepository;

    @Override
    public boolean exists(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            String dbId = DatasetType.extractId(id);
            String handleAsFallback = parameters.getHandleAsDatasetTypeFallback();
            String datasetType = DatasetType.extractType(id, handleAsFallback);
            if ( !factory.isKnown(datasetType)) {
                return false;
            }
            DataRepository dataRepository = factory.create(datasetType);
            DatasetDao<? extends DatasetEntity> dao = getSeriesDao(datasetType, session);
            return parameters.getParameters().isMatchDomainIds()
                    ? dao.hasInstance(dbId, parameters, dataRepository.getEntityType())
                    : dao.hasInstance(parseId(dbId), parameters, dataRepository.getEntityType());
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
            return getAllCondensed(query, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<DatasetOutput> getAllCondensed(DbQuery query, Session session) throws DataAccessException {
        List<DatasetOutput> results = new ArrayList<>();
        FilterResolver filterResolver = query.getFilterResolver();
        if (query.getParameters().isMatchDomainIds()) {
            String datasetType = query.getHandleAsDatasetTypeFallback();
            addCondensedResults(getSeriesDao(datasetType, session), query, results);
            return results;
        }

        if (filterResolver.shallIncludeAllDatasetTypes()) {
            addCondensedResults(getSeriesDao(DatasetEntity.class, session), query, results);
        } else {
            for (String datasetType : query.getDatasetTypes()) {
                addCondensedResults(getSeriesDao(datasetType, session), query, results);
            }
        }
        return results;
    }

    private void addCondensedResults(DatasetDao<? extends DatasetEntity> dao, DbQuery query, List<DatasetOutput> results) throws DataAccessException {
        for (DatasetEntity series : dao.getAllInstances(query)) {
            results.add(createCondensed(series, query));
        }
    }

    private DatasetDao<? extends DatasetEntity> getSeriesDao(String datasetType, Session session) throws DataAccessException {
        if ( !("all".equalsIgnoreCase(datasetType) || factory.isKnown(datasetType))) {
            throw new ResourceNotFoundException("unknown dataset type: " + datasetType);
        }
        try {
            DataRepository dataRepository = factory.create(datasetType);
            return getSeriesDao(dataRepository.getEntityType(), session);
        } catch (DatasetFactoryException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private DatasetDao<? extends DatasetEntity> getSeriesDao(Class<? extends DatasetEntity> clazz, Session session) {
        return new DatasetDao<>(session, clazz);
    }

    @Override
    public List<DatasetOutput> getAllExpanded(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllExpanded(query, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<DatasetOutput> getAllExpanded(DbQuery query, Session session) throws DataAccessException {
        List<DatasetOutput> results = new ArrayList<>();
        FilterResolver filterResolver = query.getFilterResolver();
        if (query.getParameters().isMatchDomainIds()) {
            String datasetType = query.getHandleAsDatasetTypeFallback();
            addExpandedResults(getSeriesDao(datasetType, session), query, results, session);
            return results;
        }

        if (filterResolver.shallIncludeAllDatasetTypes()) {
            addExpandedResults(getSeriesDao(DatasetEntity.class, session), query, results, session);
        } else {
            for (String datasetType : query.getDatasetTypes()) {
                addExpandedResults(getSeriesDao(datasetType, session), query, results, session);
            }
        }
        return results;
    }

    private void addExpandedResults(DatasetDao<? extends DatasetEntity> dao, DbQuery query, List<DatasetOutput> results, Session session) throws DataAccessException {
        for (DatasetEntity series : dao.getAllInstances(query)) {
            results.add(createExpanded(series, query, session));
        }
    }

    @Override
    public DatasetOutput getInstance(String id, DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            return getInstance(id, query, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public DatasetOutput getInstance(String id, DbQuery query, Session session) throws DataAccessException {
        DatasetEntity< ? > instanceEntity = getInstanceEntity(id, query, session);
        return createExpanded(instanceEntity, query, session);
    }

    DatasetEntity<?> getInstanceEntity(String id, DbQuery query, Session session) throws DataAccessException {
        String seriesId = DatasetType.extractId(id);
        String handleAsFallback = query.getHandleAsDatasetTypeFallback();
        final String datasetType = DatasetType.extractType(id, handleAsFallback);
        DatasetDao<? extends DatasetEntity> dao = getSeriesDao(datasetType, session);
        DatasetEntity instance = dao.getInstance(Long.parseLong(seriesId), query);
        instance.setPlatform(platformRepository.getPlatformEntity(instance, query, session));
        return instance;
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters paramters) {
        Session session = getSession();
        try {
            DatasetDao< ? extends DatasetEntity> dao = getSeriesDao(DatasetEntity.class, session);
            DbQuery query = getDbQuery(paramters);
            List< ? extends DatasetEntity> found = dao.find(query);
            return convertToSearchResults(found, query);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, DbQuery query) {
        String locale = query.getLocale();
        String hrefBase = urHelper.getDatasetsHrefBaseUrl(query.getHrefBase());
        List<SearchResult> results = new ArrayList<>();
        for (DescribableEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = searchResult.getLabelFrom(locale);
            results.add(new DatasetSearchResult(pkid, label, hrefBase));
        }
        return results;
    }

    // XXX refactor generics
    protected DatasetOutput createCondensed(DatasetEntity<?> series, DbQuery query) throws DataAccessException {
        DatasetOutput output = new DatasetOutput(series.getDatasetType()) {};
        output.setLabel(createSeriesLabel(series, query.getLocale()));
        output.setId(series.getPkid().toString());
        output.setDomainId(series.getDomainId());
        output.setHrefBase(urHelper.getDatasetsHrefBaseUrl(query.getHrefBase()));
        return output;
    }

    // XXX refactor generics
    protected DatasetOutput createExpanded(DatasetEntity<?> series, DbQuery query, Session session) throws DataAccessException {
        try {
            DatasetOutput result = createCondensed(series, query);
            SeriesParameters seriesParameters = createSeriesParameters(series, query, session);
            seriesParameters.setPlatform(getCondensedPlatform(series, query, session));
            result.setSeriesParameters(seriesParameters);

            if (series.getService() == null) {
                series.setService(getStaticServiceEntity());
            }
            result.setUom(series.getUnitI18nName(query.getLocale()));
            DataRepository dataRepository = factory.create(series.getDatasetType());
            result.setFirstValue(dataRepository.getFirstValue(series, session, query));
            result.setLastValue(dataRepository.getLastValue(series, session, query));
            return result;
        } catch (DatasetFactoryException ex) {
            throw new DataAccessException("Could not determine if id exists.", ex);
        }
    }

    private PlatformOutput getCondensedPlatform(DatasetEntity<?> series, DbQuery query, Session session) throws DataAccessException {
        // platform has to be handled dynamically (see #309)
        return platformRepository.getCondensedInstance(series, query, session);
    }

    private String createSeriesLabel(DatasetEntity<?> series, String locale) {
        String station = series.getFeature().getLabelFrom(locale);
        String procedure = series.getProcedure().getLabelFrom(locale);
        String phenomenon = series.getPhenomenon().getLabelFrom(locale);
        String offering = series.getOffering().getLabelFrom(locale);
        StringBuilder sb = new StringBuilder();
        sb.append(phenomenon).append(" ");
        sb.append(procedure).append(", ");
        sb.append(station).append(", ");
        return sb.append(offering).toString();
    }

    public IDataRepositoryFactory getDataRepositoryFactory() {
        return factory;
    }

    public void setDataRepositoryFactory(IDataRepositoryFactory factory) {
        this.factory = factory;
    }

}
