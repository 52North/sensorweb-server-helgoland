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
import org.n52.io.DefaultIoFactory;
import org.n52.io.request.FilterResolver;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ServiceOutput;
import org.n52.io.response.ServiceOutput.ParameterCount;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db_custom.SessionAwareRepository;
import org.n52.series.db_custom.beans.DescribableTEntity;
import org.n52.series.db_custom.dao.DbQuery;
import org.n52.series.db_custom.dao.ServiceDao;
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
    private DefaultIoFactory<Data<AbstractValue< ?>>, DatasetOutput<AbstractValue< ?>, ?>, AbstractValue< ?>> ioFactoryCreator;

    public String getServiceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(String id, DbQuery parameters) throws DataAccessException {
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
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableTEntity> found, DbQuery query) {
        List<SearchResult> results = new ArrayList<>();
        String locale = query.getLocale();
        for (DescribableTEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = searchResult.getLabelFrom(locale);
            String hrefBase = new UrlHelper().getFeaturesHrefBaseUrl(query.getHrefBase());
            results.add(new FeatureSearchResult(pkid, label, hrefBase));
        }
        return results;
    }

    @Override
    public List<ServiceOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<ServiceOutput> results = new ArrayList<>();
            for (ServiceEntity serviceEntity : getAllInstances(parameters, session)) {
                results.add(createCondensedService(serviceEntity));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<ServiceOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<ServiceOutput> results = new ArrayList<>();
            for (ServiceEntity serviceEntity : getAllInstances(parameters, session)) {
                results.add(createExpandedService(serviceEntity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public ServiceOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            ServiceEntity result = getInstance(parseId(id), parameters, session);
            return createExpandedService(result, parameters);
        } finally {
            returnSession(session);
        }
    }

    protected ServiceEntity getInstance(Long id, DbQuery parameters, Session session) throws DataAccessException {
        ServiceDao serviceDAO = createDao(session);
        ServiceEntity result = serviceDAO.getInstance(id, parameters);
        if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
        return result;
    }

    protected List<ServiceEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
        return createDao(session).getAllInstances(parameters);
    }

    private ServiceDao createDao(Session session) {
        return new ServiceDao(session);
    }

    /**
     * Gets a condensed view of the requested service, i.e. it avoids getting a full version of the requested service.
     * Getting a full version (like {@link #getInstance(String, DbQuery)}) would redundantly count all parameter values
     * available for the requested requested service.
     *
     * @param id the service id
     * @param parameters query parameters
     * @return a condensed view of the requested service.
     */
    public ServiceOutput getCondensedInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            ServiceEntity result = getInstance(parseId(id), parameters, session);
            return createCondensedService(result);
        } finally {
            returnSession(session);
        }
    }

    private ServiceOutput createExpandedService(ServiceEntity serviceEntity, DbQuery parameters) {
        ServiceOutput result = createCondensedService(serviceEntity);
        result.setType(serviceEntity.getType());
        result.setVersion(serviceEntity.getVersion());
        result.setSupportsFirstLatest(true);
        result.setQuantities(countParameters(result, parameters));
        return result;
    }

    private ParameterCount countParameters(ServiceOutput service, DbQuery query) {
        try {
            ParameterCount quantities = new ServiceOutput.ParameterCount();
            query.setServiceId(service.getId());
            // #procedures == #offerings
            quantities.setOfferingsSize(counter.countProcedures(query));
            quantities.setProceduresSize(counter.countProcedures(query));
            quantities.setCategoriesSize(counter.countCategories(query));
            quantities.setPhenomenaSize(counter.countPhenomena(query));
            quantities.setFeaturesSize(counter.countFeatures(query));
            quantities.setPlatformsSize(counter.countPlatforms(query));
            quantities.setDatasetsSize(counter.countDatasets(query));

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
