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

import org.n52.io.measurement.TvpDataCollection;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.series.MeasurementData;
import org.n52.io.response.series.SeriesDataCollection;
import org.n52.io.response.v1.ext.SeriesMetadataOutput;
import org.n52.sensorweb.spi.SeriesDataService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.v1.DbQuery;
import org.n52.series.db.da.v1.MeasurementDataRepository;
import org.n52.series.db.da.v1.SeriesRepository;
import org.n52.series.db.srv.v1.AccessService;
import org.n52.web.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class MeasurementSeriesAccessService extends AccessService<SeriesMetadataOutput> 
        implements SeriesDataService<MeasurementData> {

    @Autowired
    private MeasurementDataRepository dataRepository;
    
    public MeasurementSeriesAccessService(SeriesRepository repository) {
        super(repository);
    }
    
    @Override
    public SeriesDataCollection<MeasurementData> getSeriesData(RequestSimpleParameterSet parameters) {
        try {
            TvpDataCollection dataCollection = new TvpDataCollection();
            for (String seriesId : parameters.getSeriesIds()) {
                MeasurementData data = getDataFor(seriesId, parameters);
                if (data != null) {
                    dataCollection.addNewSeries(seriesId, data);
                }
            }
            return dataCollection;
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get series data from database.", e);
        }
    }

    private MeasurementData getDataFor(String seriesId, RequestSimpleParameterSet parameters)
            throws DataAccessException {
        DbQuery dbQuery = DbQuery.createFrom(IoParameters.createFromQuery(parameters));
        return dataRepository.getData(seriesId, dbQuery);
    }

}
