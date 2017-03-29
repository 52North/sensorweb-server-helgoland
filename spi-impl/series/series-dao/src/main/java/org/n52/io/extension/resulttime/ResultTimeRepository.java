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
package org.n52.io.extension.resulttime;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.io.request.IoParameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.dao.DatasetDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResultTimeRepository extends SessionAwareRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultTimeRepository.class);

    Set<String> getExtras(String datasetId, IoParameters parameters) {
        Session session = getSession();
        try {
            DatasetDao<DatasetEntity<?>> dao = new DatasetDao<>(session);
            DatasetEntity<?> instance = dao.getInstance(Long.parseLong(datasetId), getDbQuery(parameters));
//            Set<String> resultTimes = instance.getResultTimes()
//                    .stream()
//                    .filter(i -> isParsableDateTime(i))
//                    .map(i -> parseToIso(i))
//                    .collect(Collectors.toSet());
//            return resultTimes;

            return instance.getResultTimes().stream().map(i -> new DateTime(i).toString()).collect(Collectors.toSet());

//            Hibernate.initialize(instance.getResultTimes());
//            return instance.getResultTimes();
        } catch (NumberFormatException e) {
            LOGGER.debug("Could not convert id '{}' to long.", datasetId, e);
        } catch (DataAccessException e) {
            LOGGER.error("Could not query result times for dataset with id '{}'", datasetId, e);
        } finally {
            returnSession(session);
        }
        return Collections.emptySet();
    }

    private boolean isParsableDateTime(String input) {
        try {
           parseToIso(input);
           return true;
        } catch(Throwable e) {
            LOGGER.debug("ignore non-parsable result time {}.", input);
            return false;
        }
    }

    protected String parseToIso(String input) {
        return DateTime.parse(input).toString();
    }
}
