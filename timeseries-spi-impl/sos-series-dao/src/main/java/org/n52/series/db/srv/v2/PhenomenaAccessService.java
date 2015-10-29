/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.srv.v2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.v2.PhenomenonOutput;
import org.n52.io.response.v2.PhenomenonOutputCollection;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.v2.PhenomenonRepository;
import org.n52.series.db.srv.ServiceInfoAccess;
import org.n52.web.exception.InternalServerException;

public class PhenomenaAccessService extends ServiceInfoAccess implements ParameterService<PhenomenonOutput> {

	private PhenomenonOutputCollection createOutputCollection(List<PhenomenonOutput> results) {
        return new PhenomenonOutputCollection(results) {
                @Override
                protected Comparator<PhenomenonOutput> getComparator() {
                    return ParameterOutput.defaultComparator();
                }
            };
    }
    
    @Override
    public PhenomenonOutputCollection getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            PhenomenonRepository repository = createPhenomenonRepository();
            List<PhenomenonOutput> results = repository.getAllExpanded(dbQuery);
            return createOutputCollection(results);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get phenomenon data.", e);
        }
    }

    @Override
    public PhenomenonOutputCollection getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            PhenomenonRepository repository = createPhenomenonRepository();
            List<PhenomenonOutput> results = repository.getAllCondensed(dbQuery);
            return createOutputCollection(results);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get phenomenon data.", e);
        }
    }

    @Override
    public PhenomenonOutputCollection getParameters(String[] phenomenonIds) {
        return getParameters(phenomenonIds, IoParameters.createDefaults());
    }

    @Override
    public PhenomenonOutputCollection getParameters(String[] phenomenonIds, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            PhenomenonRepository repository = createPhenomenonRepository();
            List<PhenomenonOutput> results = new ArrayList<>();
            for (String phenomenonId : phenomenonIds) {
                results.add(repository.getInstance(phenomenonId, dbQuery));
            }
            return createOutputCollection(results);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get phenomenon data.", e);
        }
    }

    @Override
    public PhenomenonOutput getParameter(String phenomenonId) {
        return getParameter(phenomenonId, IoParameters.createDefaults());
    }

    @Override
    public PhenomenonOutput getParameter(String phenomenonId, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            PhenomenonRepository repository = createPhenomenonRepository();
            return repository.getInstance(phenomenonId, dbQuery);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get phenomenon data for '" + phenomenonId + "'.", e);
        }
    }

    private PhenomenonRepository createPhenomenonRepository() {
        return new PhenomenonRepository(getServiceInfo());
    }
}
