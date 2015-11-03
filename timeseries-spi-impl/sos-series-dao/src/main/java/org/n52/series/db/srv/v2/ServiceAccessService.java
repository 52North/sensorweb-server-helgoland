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
package org.n52.series.db.srv.v2;

import org.n52.io.request.IoParameters;
import org.n52.io.response.v2.ServiceCollectionOutput;
import org.n52.io.response.v2.ServiceOutput;
import org.n52.sensorweb.spi.ServiceParameterService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.v2.DbQueryV2;
import org.n52.series.db.da.v2.ServiceRepository;
import org.n52.series.db.srv.ServiceInfoAccess;
import org.n52.web.exception.InternalServerException;

public class ServiceAccessService extends ServiceInfoAccess implements ServiceParameterService<ServiceOutput> {


    @Override
    public ServiceCollectionOutput getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQueryV2.createFrom(query);
            ServiceRepository serviceRepository = createServiceRepository();
            return new ServiceCollectionOutput(serviceRepository.getAllExpanded(dbQuery));
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get service data.", e);
        }
    }

    @Override
    public ServiceCollectionOutput getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQueryV2.createFrom(query);
            ServiceRepository serviceRepository = createServiceRepository();
            return new ServiceCollectionOutput(serviceRepository.getAllCondensed(dbQuery));
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get service data.", e);
        }
    }

    @Override
    public ServiceCollectionOutput getParameters(String[] items) {
        return getParameters(items, IoParameters.createDefaults());
    }

    @Override
    public ServiceCollectionOutput getParameters(String[] items, IoParameters query) {
        for (String serviceId : items) {
            ServiceOutput result = getParameter(serviceId, query);
            if (result != null) {
                return new ServiceCollectionOutput(result);
            }
        }
        return null;
    }

    @Override
    public ServiceOutput getParameter(String item) {
        return getParameter(item, IoParameters.createDefaults());
    }

    @Override
    public ServiceOutput getParameter(String item, IoParameters query) {
        try {
            ServiceRepository serviceRepository = createServiceRepository();
            String serviceId = serviceRepository.getServiceId();
            return serviceId.equals(item) ?
                serviceRepository.getInstance(serviceId, DbQueryV2.createFrom(query))
                : null;
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get service data.", e);
        }
    }

    @Override
    public boolean isKnownTimeseries(String timeseriesId) {
//        try {
//            DbQuery dbQuery = DbQuery.createFrom(IoParameters.createDefaults());
//            TimeseriesRepository timeseriesRepository = createTimeseriesRepository();
//            return timeseriesRepository.getInstance(timeseriesId, dbQuery) != null;
//        }
//        catch (DataAccessException e) {
//            throw new InternalServerException("Could not determine if timeseries '" + timeseriesId + "' is known.");
//        }
    	return false;
    }

//    private TimeseriesRepository createTimeseriesRepository() {
//        return new TimeseriesRepository(getServiceInfo());
//    }

    private ServiceRepository createServiceRepository() {
        return new ServiceRepository(getServiceInfo());
    }
}
