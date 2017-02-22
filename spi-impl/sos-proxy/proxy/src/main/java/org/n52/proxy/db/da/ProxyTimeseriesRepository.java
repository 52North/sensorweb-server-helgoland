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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.proxy.db.da;

import com.google.common.base.Strings;
import org.hibernate.Session;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.da.TimeseriesRepository;
import org.n52.series.db.dao.DbQuery;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jansch
 */
public class ProxyTimeseriesRepository extends TimeseriesRepository {

    @Autowired
    private ProxyDatasetRepository datasetRepository;

    @Override
    protected TimeseriesMetadataOutput createExpanded(Session session, MeasurementDatasetEntity series, DbQuery query) throws DataAccessException {
        TimeseriesMetadataOutput output = super.createExpanded(session, series, query);
        if (Strings.isNullOrEmpty(output.getUom())) {
            DatasetOutput datasetOutput = datasetRepository.createExpanded(series, query, session);
            output.setUom(datasetOutput.getUom());
        }
        return output;
    }
}
