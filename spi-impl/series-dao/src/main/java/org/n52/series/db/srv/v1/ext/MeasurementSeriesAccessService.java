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
package org.n52.series.db.srv.v1.ext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.n52.io.format.TvpDataCollection;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.v1.ext.SeriesMetadataOutput;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.sensorweb.spi.SeriesDataService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.v1.DbQuery;
import org.n52.series.db.da.v1.SeriesRepository;
import org.n52.web.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class SeriesAccessService extends ParameterService<SeriesMetadataOutput> implements SeriesDataService {

    @Autowired
    private SeriesRepository seriesRepository;

    private OutputCollection<SeriesMetadataOutput> createOutputCollection(List<SeriesMetadataOutput> results) {
        return new OutputCollection<SeriesMetadataOutput>(results) {
            @Override
            protected Comparator<SeriesMetadataOutput> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    @Override
    public OutputCollection<SeriesMetadataOutput> getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<SeriesMetadataOutput> results = seriesRepository.getAllExpanded(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get timeseries metadata from database.", e);
        }
    }

    @Override
    public OutputCollection<SeriesMetadataOutput> getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<SeriesMetadataOutput> results = seriesRepository.getAllCondensed(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get timeseries metadata from database.", e);
        }
    }

    @Override
    public OutputCollection<SeriesMetadataOutput> getParameters(String[] items) {
        return getParameters(items, IoParameters.createDefaults());
    }

    @Override
    public OutputCollection<SeriesMetadataOutput> getParameters(String[] items, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<SeriesMetadataOutput> results = new ArrayList<>();
            for (String timeseriesId : items) {
                results.add(seriesRepository.getInstance(timeseriesId, dbQuery));
            }
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get series data.", e);
        }
    }

    @Override
    public SeriesMetadataOutput getParameter(String item) {
        return getParameter(item, IoParameters.createDefaults());
    }

    @Override
    public SeriesMetadataOutput getParameter(String item, IoParameters query) {
        try {
            return seriesRepository.getInstance(item, DbQuery.createFrom(query));
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get series data for '" + item + "'.", e);
        }
    }

    @Override
    public TvpDataCollection getSeriesData(RequestSimpleParameterSet parameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean exists(String id) {
        try {
            return seriesRepository.exists(id);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not check if resource '" + id + "' does exist.");
        }
    }

}
