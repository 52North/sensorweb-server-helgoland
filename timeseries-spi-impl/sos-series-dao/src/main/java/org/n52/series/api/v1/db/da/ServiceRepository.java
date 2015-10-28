/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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

import java.util.ArrayList;
import java.util.List;

import org.n52.io.response.v1.ServiceOutput;
import org.n52.io.response.v1.ServiceOutput.ParameterCount;
import org.n52.series.api.v1.db.da.beans.ServiceInfo;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.web.exception.InternalServerException;

public class ServiceRepository implements OutputAssembler<ServiceOutput> {

    private ServiceInfo serviceInfo;
    
    private EntityCounter counter = new EntityCounter();
    
    public ServiceRepository(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public String getServiceId() {
        return serviceInfo.getServiceId();
    }

    // TODO search?!

    @Override
    public List<ServiceOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        List<ServiceOutput> results = new ArrayList<ServiceOutput>();
        results.add(getCondensedService());
        return results;
    }

    @Override
    public List<ServiceOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        List<ServiceOutput> results = new ArrayList<ServiceOutput>();
        results.add(getExpandedService());
        return results;
    }

    @Override
    public ServiceOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        return getExpandedService();
    }

    /**
     * Gets a condensed view of the requested service, i.e. it avoids getting a full version of the requested
     * service. Getting a full version (like {@link #getInstance(String, DbQuery)}) would redundantly count
     * all parameter values available for the requested requested service.
     * 
     * @param id
     *        the service id
     * @return a condensed view of the requested service.
     */
    public ServiceOutput getCondensedInstance(String id) {
        return getCondensedService();
    }

    private ServiceOutput getExpandedService() {
        ServiceOutput service = getCondensedService();
        service.setSupportsFirstLatest(true);
        service.setQuantities(countParameters(service));
        service.setType("Thin DB access layer service.");
        service.setVersion("1.0.0");
        // service.setServiceUrl("/");
        return service;
    }

    private ServiceOutput getCondensedService() {
        ServiceOutput service = new ServiceOutput();
        service.setLabel(serviceInfo.getServiceDescription());
        service.setId(serviceInfo.getServiceId());
        return service;
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
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not count parameter entities.", e);
        }
    }

}
