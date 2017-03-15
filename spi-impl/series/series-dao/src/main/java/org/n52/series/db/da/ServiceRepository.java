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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.n52.io.DatasetFactoryException;
import org.n52.io.DefaultIoFactory;
import org.n52.io.IoFactory;
import org.n52.io.request.FilterResolver;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ServiceOutput;
import org.n52.io.response.ServiceOutput.ParameterCount;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.ServiceDao;
import org.n52.series.spi.search.FeatureSearchResult;
import org.n52.series.spi.search.SearchResult;
import org.n52.web.ctrl.UrlHelper;
import org.n52.web.exception.InternalServerException;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceRepository extends SessionAwareRepository implements OutputAssembler<ServiceOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRepository.class);

    @Autowired
    private EntityCounter counter;

    @Autowired
    private DefaultIoFactory<Data<AbstractValue< ? >>, DatasetOutput<AbstractValue< ?>, ?>, AbstractValue< ?>> ioFactoryCreator;

    @Override
    public boolean exists(String id, DbQuery parameters) throws DataAccessException {
        if (serviceEntity != null) {
            return String.valueOf(serviceEntity.getPkid()).equalsIgnoreCase(id);
        }

        Session session = getSession();
        try {
            ServiceDao dao = createDao(session);
            return dao.hasInstance(parseId(id), parameters, ServiceEntity.class);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
//        final ServiceSearchResult result = new ServiceSearchResult(serviceInfo.getServiceId(), serviceInfo.getServiceDescription());
//        String queryString = DbQuery.createFrom(parameters).getSearchTerm();
//        return serviceInfo.getServiceDescription().contains(queryString)
//                ? Collections.<SearchResult>singletonList(result)
//                : Collections.<SearchResult>emptyList();
//        Session session = getSession();
//        try {
//            ServiceDao serviceDao = createDao(session);
//            DbQuery query = getDbQuery(parameters);
//            List<ServiceEntity> found = serviceDao.find(query);
//            return convertToSearchResults(found, query);
//        } finally {
//            returnSession(session);
//        }
        // TODO implement search
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, DbQuery query) {
        List<SearchResult> results = new ArrayList<>();
        String locale = query.getLocale();
        for (DescribableEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = searchResult.getLabelFrom(locale);
            String hrefBase = new UrlHelper().getFeaturesHrefBaseUrl(query.getHrefBase());
            results.add(new FeatureSearchResult(pkid, label, hrefBase));
        }
        return results;
    }

    @Override
    public List<ServiceOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        if (serviceEntity != null) {
            return Collections.singletonList(getCondensedService(serviceEntity, parameters));
        }
        Session session = getSession();
        try {
            return getAllCondensed(parameters, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<ServiceOutput> getAllCondensed(DbQuery parameters, Session session) throws DataAccessException {
        if (serviceEntity != null) {
            return Collections.singletonList(getCondensedService(serviceEntity, parameters));
        }
        List<ServiceOutput> results = new ArrayList<>();
        for (ServiceEntity entity : getAllInstances(parameters, session)) {
            results.add(getCondensedService(entity, parameters));
        }
        return results;
    }

    @Override
    public List<ServiceOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        if (serviceEntity != null) {
            return Collections.singletonList(createExpandedService(serviceEntity, parameters));
        }
        Session session = getSession();
        try {
            return getAllExpanded(parameters, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<ServiceOutput> getAllExpanded(DbQuery parameters, Session session) throws DataAccessException {
        if (serviceEntity != null) {
            return Collections.singletonList(createExpandedService(serviceEntity, parameters));
        }
        List<ServiceOutput> results = new ArrayList<>();
        for (ServiceEntity entity : getAllInstances(parameters, session)) {
            results.add(createExpandedService(entity, parameters));
        }
        return results;
    }

    @Override
    public ServiceOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        if (serviceEntity != null) {
            return createExpandedService(serviceEntity, parameters);
        }
        Session session = getSession();
        try {
            return getInstance(id, parameters, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public ServiceOutput getInstance(String id, DbQuery parameters, Session session) throws DataAccessException {
        if (serviceEntity != null) {
            return createExpandedService(serviceEntity, parameters);
        }
        ServiceEntity result = getInstance(parseId(id), parameters, session);
        return createExpandedService(result, parameters);
    }

    private ServiceEntity getInstance(Long id, DbQuery parameters, Session session) throws DataAccessException {
        ServiceDao serviceDAO = createDao(session);
        ServiceEntity result = serviceDAO.getInstance(id, parameters);
        if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
        return result;
    }

    private List<ServiceEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
        return createDao(session).getAllInstances(parameters);
    }

    private ServiceDao createDao(Session session) {
        return new ServiceDao(session);
    }

    private ServiceOutput createExpandedService(ServiceEntity entity, DbQuery parameters) {
        ServiceOutput service = getCondensedService(entity, parameters);
        service.setQuantities(countParameters(service, parameters));
        service.setServiceUrl(entity.getUrl());
        service.setSupportsFirstLatest(true);

        FilterResolver filterResolver = parameters.getFilterResolver();
        if (filterResolver.shallBehaveBackwardsCompatible()) {
            // ensure backwards compatibility
            service.setVersion("1.0.0");
            service.setType("Restful series access layer.");
        } else {
            service.setType(entity.getType() == null
                    ? "Restful series access layer."
                    : entity.getType());
            service.setVersion(entity.getVersion() != null
                    ? entity.getVersion()
                    : "2.0");
            addSupportedDatasetsTo(service);

            // TODO add features
            // TODO different counts

        }
        return service;
    }

    private void addSupportedDatasetsTo(ServiceOutput service) {
        Map<String, Set<String>> mimeTypesByDatasetTypes = new HashMap<>();
        for (String datasetType : ioFactoryCreator.getKnownTypes()) {
            try {
                IoFactory<?, ? ,?> factory = ioFactoryCreator.create(datasetType);
                mimeTypesByDatasetTypes.put(datasetType, factory.getSupportedMimeTypes());
            } catch (DatasetFactoryException e) {
                LOGGER.error("IO Factory for dataset type '{}' couldn't be created.", datasetType);
            }
        }
        service.addSupportedDatasets(mimeTypesByDatasetTypes);
    }

    private ParameterCount countParameters(ServiceOutput service, DbQuery query) {
        try {
            ParameterCount quantities = new ServiceOutput.ParameterCount();
            DbQuery serviceQuery = dbQueryFactory.createFrom(query.getParameters().extendWith(IoParameters.SERVICES, service.getId()));
            quantities.setOfferingsSize(counter.countOfferings(serviceQuery));
            quantities.setProceduresSize(counter.countProcedures(serviceQuery));
            quantities.setCategoriesSize(counter.countCategories(serviceQuery));
            quantities.setPhenomenaSize(counter.countPhenomena(serviceQuery));
            quantities.setFeaturesSize(counter.countFeatures(serviceQuery));
            quantities.setPlatformsSize(counter.countPlatforms(serviceQuery));
            quantities.setDatasetsSize(counter.countDatasets(serviceQuery));

            FilterResolver filterResolver = query.getFilterResolver();
            if (filterResolver.shallBehaveBackwardsCompatible()) {
                quantities.setTimeseriesSize(counter.countTimeseries());
                quantities.setStationsSize(counter.countStations());
            }
            return quantities;
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count parameter entities.", e);
        }
    }

}
