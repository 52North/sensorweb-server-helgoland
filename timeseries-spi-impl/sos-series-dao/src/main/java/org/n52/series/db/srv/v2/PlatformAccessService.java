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
import org.n52.io.response.v2.PlatformOutput;
import org.n52.io.response.v2.PlatformOutputCollection;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.v2.DbQueryV2;
import org.n52.series.db.da.v2.PlatformRepository;
import org.n52.series.db.srv.ServiceInfoAccess;
import org.n52.web.exception.InternalServerException;

public class PlatformAccessService extends ServiceInfoAccess implements ParameterService<PlatformOutput> {

	@Override
	public PlatformOutputCollection getExpandedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQueryV2.createFrom(query);
			PlatformRepository repository = createPlatformRepository();
			List<PlatformOutput> results = repository.getAllExpanded(dbQuery);
			return new PlatformOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data.");
		}
	}

	@Override
	public PlatformOutputCollection getCondensedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQueryV2.createFrom(query);
			PlatformRepository repository = createPlatformRepository();
			List<PlatformOutput> results = repository.getAllCondensed(dbQuery);
			return new PlatformOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data.");
		}
	}

	@Override
	public PlatformOutputCollection getParameters(String[] ids) {
		return getParameters(ids, IoParameters.createDefaults());
	}

	@Override
	public PlatformOutputCollection getParameters(String[] ids, IoParameters query) {
		try {
			DbQuery dbQuery = DbQueryV2.createFrom(query);
			PlatformRepository repository = createPlatformRepository();
			List<PlatformOutput> results = new ArrayList<PlatformOutput>();
			for (String id : ids) {
				results.add(repository.getInstance(id, dbQuery));
			}
			return new PlatformOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data.");
		}
	}

	@Override
	public PlatformOutput getParameter(String platformId) {
		return getParameter(platformId, IoParameters.createDefaults());
	}

	@Override
	public PlatformOutput getParameter(String platformId, IoParameters query) {
		try {
			DbQuery dbQuery = DbQueryV2.createFrom(query);
			PlatformRepository repository = createPlatformRepository();
			return repository.getInstance(platformId, dbQuery);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data");
		}
	}

	private PlatformRepository createPlatformRepository() {
		return new PlatformRepository(getServiceInfo());
	}
}
