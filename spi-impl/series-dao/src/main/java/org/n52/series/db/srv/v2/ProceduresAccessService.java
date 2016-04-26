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
package org.n52.series.db.srv.v2;

import java.util.ArrayList;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.v2.ProcedureOutput;
import org.n52.io.response.v2.ProcedureOutputCollection;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.v2.DbQuery;
import org.n52.series.db.da.v2.ProcedureRepository;
import org.n52.web.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Autowired;

public class ProceduresAccessService extends ParameterService<ProcedureOutput> {

    @Autowired
    private ProcedureRepository repository;

    @Override
    public ProcedureOutputCollection getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<ProcedureOutput> results = repository.getAllExpanded(dbQuery);
            return new ProcedureOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get procedure data.");
        }
    }

    @Override
    public ProcedureOutputCollection getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<ProcedureOutput> results = repository.getAllCondensed(dbQuery);
            return new ProcedureOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get procedure data.");
        }
    }

    @Override
    public ProcedureOutputCollection getParameters(String[] procedureIds) {
        return getParameters(procedureIds, IoParameters.createDefaults());
    }

    @Override
    public ProcedureOutputCollection getParameters(String[] procedureIds, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            List<ProcedureOutput> results = new ArrayList<ProcedureOutput>();
            for (String procedureId : procedureIds) {
                results.add(repository.getInstance(procedureId, dbQuery));
            }
            return new ProcedureOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get procedure data.");
        }
    }

    @Override
    public ProcedureOutput getParameter(String procedureId) {
        return getParameter(procedureId, IoParameters.createDefaults());
    }

    @Override
    public ProcedureOutput getParameter(String procedureId, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            return repository.getInstance(procedureId, dbQuery);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get procedure data");
        }
    }

}
