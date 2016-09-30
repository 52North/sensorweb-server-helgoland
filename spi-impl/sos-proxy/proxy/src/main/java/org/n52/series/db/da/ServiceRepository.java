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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.n52.series.db.beans.ServiceInfo;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.spi.search.FeatureSearchResult;
import org.n52.series.spi.search.SearchResult;
import org.n52.series.spi.search.ServiceSearchResult;
import org.n52.web.ctrl.UrlHelper;
import org.n52.web.exception.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceRepository implements OutputAssembler<ServiceOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRepository.class);

    @Autowired
    private ServiceInfo serviceInfo;

    @Autowired
    private EntityCounter counter;

    @Autowired
    private DefaultIoFactory<Data<AbstractValue< ? >>, DatasetOutput<AbstractValue< ? >, ? >, AbstractValue< ? >> ioFactoryCreator;

    public String getServiceId() {
        return serviceInfo.getServiceId();
    }

    @Override
    public boolean exists(String id, DbQuery parameters) throws DataAccessException {
        return getServiceId().equals(id);
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
        final ServiceSearchResult result = new ServiceSearchResult(serviceInfo.getServiceId(), serviceInfo.getServiceDescription());
        String queryString = DbQuery.createFrom(parameters).getSearchTerm();
        return serviceInfo.getServiceDescription().contains(queryString)
                ? Collections.<SearchResult>singletonList(result)
                : Collections.<SearchResult>emptyList();
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
        List<ServiceOutput> results = new ArrayList<>();
        results.add(getCondensedService(parameters));
        return results;
    }

    @Override
    public List<ServiceOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        List<ServiceOutput> results = new ArrayList<>();
        results.add(getExpandedService(parameters));
        return results;
    }

    @Override
    public ServiceOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        return getExpandedService(parameters);
    }

    /**
     * Gets a condensed view of the requested service, i.e. it avoids getting a
     * full version of the requested service. Getting a full version (like
     * {@link #getInstance(String, DbQuery)}) would redundantly count all
     * parameter values available for the requested requested service.
     *
     * @param id the service id
     * @param parameters query parameters
     * @return a condensed view of the requested service.
     */
    public ServiceOutput getCondensedInstance(String id, DbQuery parameters) {
        return getCondensedService(parameters);
    }

    private ServiceOutput getExpandedService(DbQuery parameters) {
        ServiceOutput service = getCondensedService(parameters);
        service.setQuantities(countParameters(service, parameters));
        service.setSupportsFirstLatest(true);

        FilterResolver filterResolver = parameters.getFilterResolver();
        if (filterResolver.shallBehaveBackwardsCompatible()) {
            // ensure backwards compatibility
            service.setVersion("1.0.0");
            service.setType("Thin DB access layer service.");
        } else {
            service.setType(serviceInfo.getType() == null
                    ? "Thin DB access layer service."
                    : serviceInfo.getType());
            service.setVersion(serviceInfo.getVersion() != null
                    ? serviceInfo.getVersion()
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
                IoFactory factory = ioFactoryCreator.create(datasetType);
                mimeTypesByDatasetTypes.put(datasetType, factory.getSupportedMimeTypes());
            } catch (DatasetFactoryException e) {
                LOGGER.error("IO Factory for dataset type '{}' couldn't be created.", datasetType);
            }
        }
        service.addSupportedDatasets(mimeTypesByDatasetTypes);
    }

    private ServiceOutput getCondensedService(DbQuery parameters) {
        ServiceOutput service = new ServiceOutput();
        service.setLabel(serviceInfo.getServiceDescription());
        service.setId(serviceInfo.getServiceId());
        checkForHref(service, parameters);
        return service;
    }

    private void checkForHref(ServiceOutput result, DbQuery parameters) {
        if (parameters != null && parameters.getHrefBase() != null) {
            result.setHrefBase(new UrlHelper().getServicesHrefBaseUrl(parameters.getHrefBase()));
        }
    }

    private ParameterCount countParameters(ServiceOutput service, DbQuery query) {
        try {
            ParameterCount quantities = new ServiceOutput.ParameterCount();
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
