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
import javax.annotation.PostConstruct;

import org.n52.io.request.IoParameters;
import org.n52.io.response.v2.PlatformOutput;
import org.n52.io.response.v2.PlatformOutputCollection;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.ShutdownParameterService;
import org.n52.series.db.da.v2.DbQuery;
import org.n52.series.db.da.v2.PlatformRepository;
import org.n52.series.db.srv.ServiceInfoAccess;
import org.n52.web.exception.InternalServerException;

public class PlatformsAccessService extends ServiceInfoAccess implements ShutdownParameterService<PlatformOutput> {

    private PlatformRepository repository;
	
    @PostConstruct
    public void init() {
        repository = new PlatformRepository(getServiceInfo());
    }
    
    @Override
	public PlatformOutputCollection getExpandedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			List<PlatformOutput> results = repository.getAllExpanded(dbQuery);
			return new PlatformOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data.");
		}
	}

	@Override
	public PlatformOutputCollection getCondensedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
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
			DbQuery dbQuery = DbQuery.createFrom(query);
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
			DbQuery dbQuery = DbQuery.createFrom(query);
			return repository.getInstance(platformId, dbQuery);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data");
		}
	}

    @Override
    public void shutdown() {
        repository.cleanup();
    }
    
}
