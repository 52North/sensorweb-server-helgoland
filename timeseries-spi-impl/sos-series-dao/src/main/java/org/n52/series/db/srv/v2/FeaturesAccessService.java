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
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.v2.FeatureOutput;
import org.n52.io.response.v2.FeatureOutputCollection;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.v2.DbQueryV2;
import org.n52.series.db.da.v2.FeatureRepository;
import org.n52.series.db.da.v2.PlatformRepository;
import org.n52.series.db.srv.ServiceInfoAccess;
import org.n52.web.exception.InternalServerException;

public class FeaturesAccessService extends ServiceInfoAccess implements ParameterService<FeatureOutput> {
	
	public FeaturesAccessService(String dbSrid) {
        if (dbSrid != null) {
            FeatureRepository repository = createFeatureRepository();
            repository.setDatabaseSrid(dbSrid);
        }
    }

	@Override
	public FeatureOutputCollection getExpandedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQueryV2.createFrom(query);
			FeatureRepository repository = createFeatureRepository();
			List<FeatureOutput> results = repository.getAllExpanded(dbQuery);
			return new FeatureOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get feature data.");
		}
	}

	@Override
	public FeatureOutputCollection getCondensedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQueryV2.createFrom(query);
			FeatureRepository repository = createFeatureRepository();
			List<FeatureOutput> results = repository.getAllCondensed(dbQuery);
			return new FeatureOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get feature data.");
		}
	}

	@Override
	public FeatureOutputCollection getParameters(String[] featureIds) {
		return getParameters(featureIds, IoParameters.createDefaults());
	}

	@Override
	public FeatureOutputCollection getParameters(String[] featureIds, IoParameters query) {
		try {
			DbQuery dbQuery = DbQueryV2.createFrom(query);
			FeatureRepository repository = createFeatureRepository();
			List<FeatureOutput> results = new ArrayList<FeatureOutput>();
			for (String featureId : featureIds) {
				results.add(repository.getInstance(featureId, dbQuery));
			}
			return new FeatureOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get feature data.");
		}
	}

	@Override
	public FeatureOutput getParameter(String featureId) {
		return getParameter(featureId, IoParameters.createDefaults());
	}

	@Override
	public FeatureOutput getParameter(String featureId, IoParameters query) {
		try {
			DbQuery dbQuery = DbQueryV2.createFrom(query);
			FeatureRepository repository = createFeatureRepository();
			return repository.getInstance(featureId, dbQuery);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get feature data");
		}
	}

	private FeatureRepository createFeatureRepository() {
		return new FeatureRepository(getServiceInfo());
	}

}
