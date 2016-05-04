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
package org.n52.series.db.srv.v1;

import java.util.Comparator;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.io.response.v1.ServiceOutput;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.sensorweb.spi.ServiceParameterService;
import org.n52.series.db.da.v1.DbQuery;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.series.db.da.v1.OutputAssembler;
import org.n52.web.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceAccessService extends ParameterService<ServiceOutput> implements ServiceParameterService {

    @Autowired
    private ServiceInfo serviceInfo;

    private final OutputAssembler<ServiceOutput> serviceRepository;

    private final OutputAssembler<TimeseriesMetadataOutput> timeseriesRepository;

    public ServiceAccessService(OutputAssembler<ServiceOutput> serviceRepository, OutputAssembler<TimeseriesMetadataOutput> timeseriesRepository) {
        this.serviceRepository = serviceRepository;
        this.timeseriesRepository = timeseriesRepository;
    }

    private OutputCollection<ServiceOutput> createOutputCollection(ServiceOutput result) {
        return new OutputCollection<ServiceOutput>(result) {
            @Override
            protected Comparator<ServiceOutput> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    private OutputCollection<ServiceOutput> createOutputCollection(List<ServiceOutput> results) {
        return new OutputCollection<ServiceOutput>(results) {
            @Override
            protected Comparator<ServiceOutput> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    @Override
    public OutputCollection<ServiceOutput> getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<ServiceOutput> results = serviceRepository.getAllExpanded(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get service data.", e);
        }
    }

    @Override
    public OutputCollection<ServiceOutput> getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<ServiceOutput> results = serviceRepository.getAllCondensed(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get service data.", e);
        }
    }

    @Override
    public OutputCollection<ServiceOutput> getParameters(String[] items) {
        return getParameters(items, IoParameters.createDefaults());
    }

    @Override
    public OutputCollection<ServiceOutput> getParameters(String[] items, IoParameters query) {
        for (String serviceId : items) {
            ServiceOutput result = getParameter(serviceId, query);
            if (result != null) {
                return createOutputCollection(result);
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
            String serviceId = serviceInfo.getServiceId();
            return serviceId.equals(item)
                    ? serviceRepository.getInstance(serviceId, DbQuery.createFrom(query))
                    : null;
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get service data.", e);
        }
    }

    @Override
    public boolean isKnownTimeseries(String timeseriesId) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(IoParameters.createDefaults());
            return timeseriesRepository.getInstance(timeseriesId, dbQuery) != null;
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not determine if timeseries '" + timeseriesId + "' is known.");
        }
    }

}
