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
import org.n52.series.db.DataAccessException;
import org.n52.series.db.da.EntityCounter;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.spi.srv.CountingMetadataService;
import org.n52.web.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;

public class CountingMetadataAccessService implements CountingMetadataService {

    @Autowired
    private EntityCounter counter;

    @Override
    public Integer getServiceCount(IoParameters parameters) {
        return 1; // we only provide 1 service
    }

    @Override
    public Integer getOfferingCount(IoParameters parameters) {
        try {
            DbQuery query = DbQuery.createFrom(parameters);
            return counter.countOfferings(query);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Offerings entities.", e);
        }
    }

    @Override
    public Integer getCategoryCount(IoParameters parameters) {
        try {
            DbQuery query = DbQuery.createFrom(parameters);
            return counter.countCategories(query);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Categories entities.", e);
        }
    }

    @Override
    public Integer getFeatureCount(IoParameters parameters) {
        try {
            DbQuery query = DbQuery.createFrom(parameters);
            return counter.countFeatures(query);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Feature entities.", e);
        }
    }

    @Override
    public Integer getProcedureCount(IoParameters parameters) {
        try {
            DbQuery query = DbQuery.createFrom(parameters);
            return counter.countProcedures(query);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Procedure entities.", e);
        }
    }

    @Override
    public Integer getPhenomenaCount(IoParameters parameters) {
        try {
            DbQuery query = DbQuery.createFrom(parameters);
            return counter.countPhenomena(query);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Phenomenon entities.", e);
        }
    }

    @Override
    public Integer getPlatformCount(IoParameters parameters) {
        try {
            DbQuery query = DbQuery.createFrom(parameters);
            return counter.countPlatforms(query);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Platform entities.", e);
        }
    }

    @Override
    public Integer getDatasetCount(IoParameters parameters) {
        try {
            DbQuery query = DbQuery.createFrom(parameters);
            return counter.countDatasets(query);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Dataset entities.", e);
        }
    }


    @Override
    public Integer getGeometryCount(IoParameters parameters) {
        try {
            DbQuery query = DbQuery.createFrom(parameters);
            return counter.countGeometries(query);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Geometry entities.", e);
        }
    }

    @Override
    @Deprecated
    public Integer getStationCount() {
        try {
            return counter.countStations();
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Station entities.", e);
        }
    }

    @Override
    @Deprecated
    public Integer getTimeseriesCount() {
        try {
            return counter.countTimeseries();
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Timeseries entities.", e);
        }
    }

}
