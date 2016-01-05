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
package org.n52.series.api.v1.db.da;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v1.PhenomenonOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.sensorweb.spi.search.PhenomenonSearchResult;
import org.n52.series.api.v1.db.da.dao.PhenomenonDao;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.PhenomenonEntity;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.web.exception.ResourceNotFoundException;

public class PhenomenonRepository extends ExtendedSessionAwareRepository implements OutputAssembler<PhenomenonOutput> {

    public PhenomenonRepository(ServiceInfo serviceInfo) {
        super(serviceInfo);
    }
    
    @Override
	public Collection<SearchResult> searchFor(String searchString, String locale) {
		Session session = getSession();
		try {
			PhenomenonDao phenomenonDao = new PhenomenonDao(session);
			DbQuery parameters = getDbQuery(IoParameters.createDefaults(), locale);
			List<PhenomenonEntity> found = phenomenonDao.find(searchString, parameters);
			return convertToSearchResults(found, locale);
		} finally {
			returnSession(session);
		}
	}

	@Override
	protected List<SearchResult> convertToSearchResults(List<? extends DescribableEntity<? extends I18nEntity>> found,
			String locale) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		for (DescribableEntity<? extends I18nEntity> searchResult : found) {
			String pkid = searchResult.getPkid().toString();
			String label = getLabelFrom(searchResult, locale);
			results.add(new PhenomenonSearchResult(pkid, label));
		}
		return results;
	}

    @Override
    public List<PhenomenonOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<PhenomenonOutput> results = new ArrayList<PhenomenonOutput>();
            for (PhenomenonEntity phenomenonEntity : getAllInstances(parameters, session)) {
                results.add(createCondensed(phenomenonEntity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<PhenomenonOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<PhenomenonOutput> results = new ArrayList<PhenomenonOutput>();
            for (PhenomenonEntity phenomenonEntity : getAllInstances(parameters, session)) {
                results.add(createExpanded(phenomenonEntity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

	@Override
    public PhenomenonOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
        	PhenomenonEntity result = getInstance(parseId(id), parameters, session);
            return createExpanded(result, parameters);
        } finally {
            returnSession(session);
        }
    }
	
	protected List<PhenomenonEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
		return new PhenomenonDao(session).getAllInstances(parameters);
	}
	
	protected PhenomenonEntity getInstance(Long id, DbQuery parameters, Session session) throws DataAccessException {
		PhenomenonDao phenomenonDao = new PhenomenonDao(session);
        PhenomenonEntity result = phenomenonDao.getInstance(id, parameters);
        if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
        return result;
	}

    private PhenomenonOutput createExpanded(PhenomenonEntity entity, DbQuery parameters) throws DataAccessException {
        PhenomenonOutput result = createCondensed(entity, parameters);
        result.setService(getServiceOutput());
        return result;
    }

    private PhenomenonOutput createCondensed(PhenomenonEntity entity, DbQuery parameters) {
        PhenomenonOutput result = new PhenomenonOutput();
        result.setLabel(getLabelFrom(entity, parameters.getLocale()));
        result.setId(Long.toString(entity.getPkid()));
        result.setDomainId(entity.getDomainId());
        return result;
    }
}
