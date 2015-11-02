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
package org.n52.series.api.v1.db.srv;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.v1.FeatureOutput;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.series.api.v1.db.da.DbQueryV1;
import org.n52.series.api.v1.db.da.FeatureRepository;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.srv.ServiceInfoAccess;
import org.n52.web.exception.InternalServerException;

public class FeaturesAccessService extends ServiceInfoAccess implements ParameterService<FeatureOutput> {

    private OutputCollection<FeatureOutput> createOutputCollection(List<FeatureOutput> results) {
        return new OutputCollection<FeatureOutput>(results) {
                @Override
                protected Comparator<FeatureOutput> getComparator() {
                    return ParameterOutput.defaultComparator();
                }
            };
    }
    
    @Override
    public OutputCollection<FeatureOutput> getExpandedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQueryV1.createFrom(query);
            FeatureRepository repository = createFeatureRepository();
            List<FeatureOutput> results = repository.getAllExpanded(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get feature data.", e);
        }
    }

    @Override
    public OutputCollection<FeatureOutput> getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQueryV1.createFrom(query);
            FeatureRepository repository = createFeatureRepository();
            List<FeatureOutput> results = repository.getAllCondensed(dbQuery);
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get feature data.", e);
        }
    }

    @Override
    public OutputCollection<FeatureOutput> getParameters(String[] featureIds) {
        return getParameters(featureIds, IoParameters.createDefaults());
    }

    @Override
    public OutputCollection<FeatureOutput> getParameters(String[] featureIds, IoParameters query) {
        try {
            DbQuery dbQuery = DbQueryV1.createFrom(query);
            FeatureRepository repository = createFeatureRepository();
            List<FeatureOutput> results = new ArrayList<>();
            for (String categoryId : featureIds) {
                results.add(repository.getInstance(categoryId, dbQuery));
            }
            return createOutputCollection(results);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get feature data.", e);
        }
    }

    @Override
    public FeatureOutput getParameter(String featureid) {
        return getParameter(featureid, IoParameters.createDefaults());
    }

    @Override
    public FeatureOutput getParameter(String featureId, IoParameters query) {
        try {
            DbQuery dbQuery = DbQueryV1.createFrom(query);
            FeatureRepository repository = createFeatureRepository();
            return repository.getInstance(featureId, dbQuery);
        } catch (DataAccessException e) {
            throw new InternalServerException("Could not get feature data.", e);
        }
    }

    private FeatureRepository createFeatureRepository() {
        return new FeatureRepository(getServiceInfo());
    }

}
