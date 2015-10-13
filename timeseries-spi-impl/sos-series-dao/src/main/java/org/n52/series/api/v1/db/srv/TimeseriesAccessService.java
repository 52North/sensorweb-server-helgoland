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
package org.n52.series.api.v1.db.srv;

import java.util.ArrayList;
import java.util.List;

import org.n52.io.IoParameters;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.UndesignedParameterSet;
import org.n52.series.api.v1.db.da.DataAccessException;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.api.v1.db.da.TimeseriesRepository;
import org.n52.web.InternalServerException;
import org.n52.sensorweb.v1.spi.ParameterService;
import org.n52.sensorweb.v1.spi.TimeseriesDataService;

public class TimeseriesAccessService extends ServiceInfoAccess implements TimeseriesDataService, ParameterService<TimeseriesMetadataOutput> {

    @Override
    public TvpDataCollection getTimeseriesData(UndesignedParameterSet parameters) {
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

    private TimeseriesData getDataFor(String timeseriesId, UndesignedParameterSet parameters) throws DataAccessException {
        DbQuery dbQuery = DbQuery.createFrom(IoParameters.createFromQuery(parameters));
        TimeseriesRepository repository = createTimeseriesRepository();
        if (parameters.isExpanded()) {
            return repository.getDataWithReferenceValues(timeseriesId, dbQuery);
        } else {
            return repository.getData(timeseriesId, dbQuery);
        }
    }

    @Override
    public TimeseriesMetadataOutput[] getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            TimeseriesRepository repository = createTimeseriesRepository();
            List<TimeseriesMetadataOutput> results = repository.getAllExpanded(dbQuery);
            return results.toArray(new TimeseriesMetadataOutput[0]);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get timeseries metadata from database.", e);
        }
    }

    @Override
    public TimeseriesMetadataOutput[] getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            TimeseriesRepository repository = createTimeseriesRepository();
            List<TimeseriesMetadataOutput> results = repository.getAllCondensed(dbQuery);
            return results.toArray(new TimeseriesMetadataOutput[0]);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get series data.", e);
        }
    }

    @Override
    public TimeseriesMetadataOutput[] getParameters(String[] items) {
        return getParameters(items, IoParameters.createDefaults());
    }

    @Override
    public TimeseriesMetadataOutput[] getParameters(String[] items, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            TimeseriesRepository repository = createTimeseriesRepository();
            List<TimeseriesMetadataOutput> results = new ArrayList<TimeseriesMetadataOutput>();
            for (String timeseriesId : items) {
                results.add(repository.getInstance(timeseriesId, dbQuery));
            }
            return results.toArray(new TimeseriesMetadataOutput[0]);
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
            TimeseriesRepository repository = createTimeseriesRepository();
            return repository.getInstance(item, DbQuery.createFrom(query));
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get series data for '" + item + "'.", e);
        }

    }

    private TimeseriesRepository createTimeseriesRepository() {
        return new TimeseriesRepository(getServiceInfo());
    }

}
