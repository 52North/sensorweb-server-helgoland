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

import java.util.ArrayList;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.da.OutputAssembler;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.DbQueryFactory;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;

public class AccessService<T extends ParameterOutput> extends ParameterService<T> {

    protected final OutputAssembler<T> repository;

    @Autowired
    protected DbQueryFactory dbQueryFactory;

    public AccessService(OutputAssembler<T> repository) {
        this.repository = repository;
    }

    @Override
    public OutputCollection<T> getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = dbQueryFactory.createFrom(query);
            List<T> results = repository.getAllExpanded(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get data.", e);
        }
    }

    @Override
    public OutputCollection<T> getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = dbQueryFactory.createFrom(query);
            List<T> results = repository.getAllCondensed(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get data.", e);
        }
    }

    @Override
    public OutputCollection<T> getParameters(String[] ids, IoParameters query) {
        try {
            DbQuery dbQuery = dbQueryFactory.createFrom(query);
            List<T> results = new ArrayList<>();
            for (String id : ids) {
                results.add(repository.getInstance(id, dbQuery));
            }
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get data.", e);
        }
    }

    @Override
    public T getParameter(String id, IoParameters query) {
        try {
            DbQuery dbQuery = dbQueryFactory.createFrom(query);
            return repository.getInstance(id, dbQuery);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get data.", e);
        }
    }

    @Override
    public boolean exists(String id, IoParameters parameters) {
        try {
            return repository.exists(id, dbQueryFactory.createFrom(parameters));
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not check if resource '" + id + "' does exist.");
        }
    }

}
