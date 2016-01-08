/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.n52.io.format.TvpDataCollection;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.TimeseriesData;
import org.n52.io.response.v2.SeriesMetadataV2Output;
import org.n52.io.response.v2.SeriesOutputCollection;
import org.n52.sensorweb.spi.SeriesDataService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.srv.LifeCycledParameterService;
import org.n52.series.db.da.v2.DbQuery;
import org.n52.series.db.da.v2.SeriesRepository;
import org.n52.web.exception.InternalServerException;

public class SeriesAccessService extends LifeCycledParameterService<SeriesMetadataV2Output>
		implements SeriesDataService {
    
    private SeriesRepository repository;

    @Override
    public void init() {
        repository = new SeriesRepository(getServiceInfo());
    }
    
	private OutputCollection<SeriesMetadataV2Output> createOutputCollection(List<SeriesMetadataV2Output> results) {
		return new SeriesOutputCollection(results) {
			@Override
			protected Comparator<SeriesMetadataV2Output> getComparator() {
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

	private TimeseriesData getDataFor(String timeseriesId, RequestSimpleParameterSet parameters)
			throws DataAccessException {
		DbQuery dbQuery = DbQuery.createFrom(IoParameters.createFromQuery(parameters));
		if (parameters.isExpanded()) {
			return repository.getDataWithReferenceValues(timeseriesId, dbQuery);
		} else {
			return repository.getData(timeseriesId, dbQuery);
		}
	}

	@Override
	public OutputCollection<SeriesMetadataV2Output> getExpandedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			List<SeriesMetadataV2Output> results = repository.getAllExpanded(dbQuery);
			return createOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get series metadata from database.", e);
		}
	}

	@Override
	public OutputCollection<SeriesMetadataV2Output> getCondensedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			List<SeriesMetadataV2Output> results = repository.getAllCondensed(dbQuery);
			return createOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get series data.", e);
		}
	}

	@Override
	public OutputCollection<SeriesMetadataV2Output> getParameters(String[] items) {
		return getParameters(items, IoParameters.createDefaults());
	}

	@Override
	public OutputCollection<SeriesMetadataV2Output> getParameters(String[] items, IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			List<SeriesMetadataV2Output> results = new ArrayList<>();
			for (String seriesId : items) {
				results.add(repository.getInstance(seriesId, dbQuery));
			}
			return createOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get series data.", e);
		}
	}

	@Override
	public SeriesMetadataV2Output getParameter(String item) {
		return getParameter(item, IoParameters.createDefaults());
	}

	@Override
	public SeriesMetadataV2Output getParameter(String item, IoParameters query) {
		try {
			return repository.getInstance(item, DbQuery.createFrom(query));
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get series data for '" + item + "'.");
		}
	}

	@Override
    public void shutdown() {
        repository.cleanup();
    }
    
}
