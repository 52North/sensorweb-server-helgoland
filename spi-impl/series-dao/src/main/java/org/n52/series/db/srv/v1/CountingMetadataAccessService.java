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

import org.n52.sensorweb.spi.v1.CountingMetadataService;
import org.n52.series.db.da.v1.EntityCounter;
import org.n52.series.db.da.DataAccessException;
import org.n52.web.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;

public class CountingMetadataAccessService implements CountingMetadataService {

    @Autowired
    private EntityCounter repository;

    @Override
    public int getServiceCount() {
        return 1; // we only provide 1 service
    }

    @Override
    public int getStationsCount() {
        try {
            return repository.countStations();
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Station entities.", e);
        }
    }

    @Override
    public int getSeriesCount() {
        try {
            return repository.countTimeseries();
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Timeseries entities.", e);
        }
    }

    @Override
    public int getOfferingsCount() {
        try {
            return repository.countOfferings();
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Offerings entities.", e);
        }
    }

    @Override
    public int getCategoriesCount() {
        try {
            return repository.countCategories();
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Categories entities.", e);
        }
    }

    @Override
    public int getFeaturesCount() {
        try {
            return repository.countFeatures();
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Feature entities.", e);
        }
    }

    @Override
    public int getProceduresCount() {
        try {
            return repository.countProcedures();
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Procedure entities.", e);
        }
    }

    @Override
    public int getPhenomenaCount() {
        try {
            return repository.countPhenomena();
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not count Phenomenon entities.", e);
        }
    }

}
