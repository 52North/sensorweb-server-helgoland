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

import org.n52.sensorweb.spi.v1.ExtCountingMetadataService;
import org.n52.series.db.da.v1.EntityCounter;
import org.springframework.beans.factory.annotation.Autowired;

public class ExtCountingMetadataAccessService extends CountingMetadataAccessService implements ExtCountingMetadataService {

    @Autowired
    private EntityCounter counter;

    @Override
    public int getPlatformCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSeriesCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getExtOfferingsCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getExtCategoriesCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getExtFeaturesCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getExtProceduresCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getExtPhenomenaCount() {
        // TODO Auto-generated method stub
        return 0;
    }

}
