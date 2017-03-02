/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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

import org.n52.io.DatasetFactoryException;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetType;
import org.n52.io.series.TvpDataCollection;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.da.DataRepository;
import org.n52.series.db.da.DatasetRepository;
import org.n52.series.db.da.IDataRepositoryFactory;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.spi.srv.DataService;
import org.n52.web.exception.InternalServerException;
import org.n52.web.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class DatasetAccessService extends AccessService<DatasetOutput> implements DataService<Data<?>> {

    @Autowired
    private IDataRepositoryFactory dataFactory;

    public DatasetAccessService(DatasetRepository<Data<?>> repository) {
        super(repository);
    }

    @Override
    public DataCollection<Data<?>> getData(RequestParameterSet parameters) {
        try {
            TvpDataCollection<Data<?>> dataCollection = new TvpDataCollection<>();
            for (String seriesId : parameters.getDatasets()) {
                Data<?> data = getDataFor(seriesId, parameters);
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

    private Data<?> getDataFor(String seriesId, RequestParameterSet parameters)
            throws DataAccessException {
        DbQuery dbQuery = dbQueryFactory.createFrom(IoParameters.createFromQuery(parameters));
        String handleAsDatasetFallback = parameters.getAsString(Parameters.HANDLE_AS_DATASET_TYPE);
        String datasetType = DatasetType.extractType(seriesId, handleAsDatasetFallback);
        DataRepository dataRepository = createRepository(datasetType);
        return dataRepository.getData(seriesId, dbQuery);
    }

    private DataRepository createRepository(String datasetType) throws DataAccessException {
        if ( !("all".equalsIgnoreCase(datasetType) || dataFactory.isKnown(datasetType))) {
            throw new ResourceNotFoundException("unknown dataset type: " + datasetType);
        }
        try {
            return dataFactory.create(datasetType);
        } catch (DatasetFactoryException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

}
