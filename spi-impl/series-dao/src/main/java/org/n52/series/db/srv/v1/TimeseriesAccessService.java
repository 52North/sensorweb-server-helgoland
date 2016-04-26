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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.n52.io.format.TvpDataCollection;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.TimeseriesData;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.sensorweb.spi.SeriesDataService;
import org.n52.series.db.da.v1.DbQuery;
import org.n52.series.db.da.v1.TimeseriesRepository;
import org.n52.series.db.da.DataAccessException;
import org.n52.web.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;

public class TimeseriesAccessService extends ParameterService<TimeseriesMetadataOutput> implements SeriesDataService {

    @Autowired
    private TimeseriesRepository repository;

    private OutputCollection<TimeseriesMetadataOutput> createOutputCollection(List<TimeseriesMetadataOutput> results) {
        return new OutputCollection<TimeseriesMetadataOutput>(results) {
            @Override
            protected Comparator<TimeseriesMetadataOutput> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    @Override
    public TvpDataCollection getSeriesData(RequestSimpleParameterSet parameters) {
        try {
            TvpDataCollection dataCollection = new TvpDataCollection();
            for (String timeseriesId : parameters.getTimeseries()) {
                TimeseriesData data = getDataFor(timeseriesId, parameters);
                if (data != null) {
                    dataCollection.addNewTimeseries(timeseriesId, data);
                }
            }
            return dataCollection;
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get series data from database.", e);
        }
    }

    private TimeseriesData getDataFor(String timeseriesId, RequestSimpleParameterSet parameters) throws DataAccessException {
        DbQuery dbQuery = DbQuery.createFrom(IoParameters.createFromQuery(parameters));
        if (parameters.isExpanded()) {
            return repository.getDataWithReferenceValues(timeseriesId, dbQuery);
        } else {
            return repository.getData(timeseriesId, dbQuery);
        }
    }

    @Override
    public OutputCollection<TimeseriesMetadataOutput> getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<TimeseriesMetadataOutput> results = repository.getAllExpanded(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get timeseries metadata from database.", e);
        }
    }

    @Override
    public OutputCollection<TimeseriesMetadataOutput> getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<TimeseriesMetadataOutput> results = repository.getAllCondensed(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get series data.", e);
        }
    }

    @Override
    public OutputCollection<TimeseriesMetadataOutput> getParameters(String[] items) {
        return getParameters(items, IoParameters.createDefaults());
    }

    @Override
    public OutputCollection<TimeseriesMetadataOutput> getParameters(String[] items, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<TimeseriesMetadataOutput> results = new ArrayList<>();
            for (String timeseriesId : items) {
                results.add(repository.getInstance(timeseriesId, dbQuery));
            }
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get series data.", e);
        }
    }

    @Override
    public TimeseriesMetadataOutput getParameter(String item) {
        return getParameter(item, IoParameters.createDefaults());
    }

    @Override
    public TimeseriesMetadataOutput getParameter(String item, IoParameters query) {
        try {
            return repository.getInstance(item, DbQuery.createFrom(query));
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get series data for '" + item + "'.", e);
        }

    }

}
