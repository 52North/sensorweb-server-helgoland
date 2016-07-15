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
package org.n52.series.srv;

import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.v1.ext.DatasetType;
import org.n52.io.response.v1.ext.DatasetOutput;
import org.n52.io.series.TvpDataCollection;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.da.DatasetRepository;
import org.n52.series.db.da.DataRepositoryFactory;
import org.n52.web.exception.InternalServerException;
import org.n52.sensorweb.spi.DataService;
import org.n52.series.db.da.DataRepository;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class DatasetAccessService extends AccessService<DatasetOutput>
        implements DataService<Data> {

    private final DataRepositoryFactory factory;
    
    public DatasetAccessService(DatasetRepository repository) {
        super(repository);
        factory = repository.getDataRepositoryFactory();
    }

    @Override
    public DataCollection<Data> getData(RequestSimpleParameterSet parameters) {
        try {
            TvpDataCollection<Data> dataCollection = new TvpDataCollection<>();
            for (String seriesId : parameters.getSeriesIds()) {
                Data data = getDataFor(seriesId, parameters);
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

    private Data getDataFor(String seriesId, RequestSimpleParameterSet parameters)
            throws DataAccessException {
        DbQuery dbQuery = DbQuery.createFrom(IoParameters.createFromQuery(parameters));
        String datasetType = DatasetType.extractType(seriesId);
        DataRepository dataRepository = factory.createRepository(datasetType);
        return dataRepository.getData(seriesId, dbQuery);
    }

}
