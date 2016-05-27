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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.request.QueryParameters;
import org.n52.io.response.v1.ServiceOutput;
import org.n52.io.response.v1.ServiceOutput.ParameterCount;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.sensorweb.spi.search.ServiceSearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.web.ctrl.v1.ext.ExtUrlSettings;
import org.n52.web.ctrl.v1.ext.UrlHelper;
import org.n52.web.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceRepository implements OutputAssembler<ServiceOutput> {

    @Autowired
    private ServiceInfo serviceInfo;

    @Autowired
    private EntityCounter counter;

    public String getServiceId() {
        return serviceInfo.getServiceId();
    }

    @Override
    public boolean exists(String id) throws DataAccessException {
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
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, String locale) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // TODO search?!
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
     * @return a condensed view of the requested service.
     */
    public ServiceOutput getCondensedInstance(String id, DbQuery parameters) {
        return getCondensedService(parameters);
    }

    private ServiceOutput getExpandedService(DbQuery parameters) {
        ServiceOutput service = getCondensedService(parameters);
        service.setSupportsFirstLatest(true);
        service.setQuantities(countParameters(service));
        service.setType("Thin DB access layer service.");
        service.setVersion("1.0.0");
        // service.setServiceUrl("/");
        return service;
    }

    private ServiceOutput getCondensedService(DbQuery parameters) {
        ServiceOutput service = new ServiceOutput();
        service.setLabel(serviceInfo.getServiceDescription());
        service.setId(serviceInfo.getServiceId());
        checkForHref(service, parameters);
        return service;
    }

    private void checkForHref(ServiceOutput result, DbQuery parameters) {
        if (parameters == null) {
            parameters = DbQuery.createFrom(QueryParameters.createFromQuery(new HashMap<String, String>()));
        }
        if (parameters.getHrefBase() != null && parameters.getHrefBase().contains(ExtUrlSettings.EXT)) {
            result.setHrefBase(new UrlHelper().getServicesHrefBaseUrl(parameters.getHrefBase()));
        }
    }

    private ParameterCount countParameters(ServiceOutput service) {
        return countEntities(service);
    }

    private ParameterCount countEntities(ServiceOutput service) {
        try {
            ParameterCount quantities = new ServiceOutput.ParameterCount();
            // #procedures == #offerings
            quantities.setOfferingsSize(counter.countProcedures());
            quantities.setProceduresSize(counter.countProcedures());
            quantities.setCategoriesSize(counter.countCategories());
            quantities.setTimeseriesSize(counter.countTimeseries());
            quantities.setPhenomenaSize(counter.countPhenomena());
            quantities.setFeaturesSize(counter.countFeatures());
            quantities.setStationsSize(counter.countStations());
            return quantities;
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count parameter entities.", e);
        }
    }

}
