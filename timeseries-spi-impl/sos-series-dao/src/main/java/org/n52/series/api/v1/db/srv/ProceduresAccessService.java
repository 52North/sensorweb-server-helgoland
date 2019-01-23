/**
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.series.api.v1.db.srv;

import java.util.ArrayList;
import java.util.List;

import org.n52.io.IoParameters;
import org.n52.io.v1.data.ProcedureOutput;
import org.n52.series.api.v1.db.da.DataAccessException;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.api.v1.db.da.ProcedureRepository;
import org.n52.web.InternalServerException;
import org.n52.sensorweb.v1.spi.ParameterService;

public class ProceduresAccessService extends ServiceInfoAccess implements ParameterService<ProcedureOutput> {

    @Override
    public ProcedureOutput[] getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            ProcedureRepository repository = createProcedureRepository();
            List<ProcedureOutput> results = repository.getAllExpanded(dbQuery);
            return results.toArray(new ProcedureOutput[0]);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get procedure data.");
        }
    }

    @Override
    public ProcedureOutput[] getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            ProcedureRepository repository = createProcedureRepository();
            List<ProcedureOutput> results = repository.getAllCondensed(dbQuery);
            return results.toArray(new ProcedureOutput[0]);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get procedure data.");
        }
    }

    @Override
    public ProcedureOutput[] getParameters(String[] procedureIds) {
        return getParameters(procedureIds, IoParameters.createDefaults());
    }

    @Override
    public ProcedureOutput[] getParameters(String[] procedureIds, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            ProcedureRepository repository = createProcedureRepository();
            List<ProcedureOutput> results = new ArrayList<ProcedureOutput>();
            for (String procedureId : procedureIds) {
                results.add(repository.getInstance(procedureId, dbQuery));
            }
            return results.toArray(new ProcedureOutput[0]);
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
            ProcedureRepository repository = createProcedureRepository();
            return repository.getInstance(procedureId, dbQuery);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get procedure data");
        }
    }

    private ProcedureRepository createProcedureRepository() {
        return new ProcedureRepository(getServiceInfo());
    }

}
