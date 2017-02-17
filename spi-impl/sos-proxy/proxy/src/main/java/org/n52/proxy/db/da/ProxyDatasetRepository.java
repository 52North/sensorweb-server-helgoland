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
package org.n52.proxy.db.da;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.proxy.connector.AbstractSosConnector;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ProxyDatasetRepository<T extends Data> extends org.n52.series.db.da.DatasetRepository<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProxyDatasetRepository.class);

    private Map<String, AbstractSosConnector> connectorMap = new HashMap<>();

    @Autowired
    public void setConnectors(List<AbstractSosConnector> connectors) {
        connectors.forEach((connector) -> {
            connectorMap.put(connector.getConnectorName(), connector);
        });
    }

    @Override
    protected DatasetOutput createExpanded(DatasetEntity<?> series, DbQuery query, Session session) throws DataAccessException {
        if (series.getUnit() == null || Strings.isNullOrEmpty(series.getUnit().getName())) {
            String connectorName = ((ProxyServiceEntity) series.getService()).getConnector();
            AbstractSosConnector connector = connectorMap.get(connectorName);
            UnitEntity unit = connector.getUom(series);
            // TODO check first in database if a unit with the identifier exists
            series.setUnit(unit);
            session.save(unit);
            session.save(series);
            session.flush();
        }
        return super.createExpanded(series, query, session);
    }

}
