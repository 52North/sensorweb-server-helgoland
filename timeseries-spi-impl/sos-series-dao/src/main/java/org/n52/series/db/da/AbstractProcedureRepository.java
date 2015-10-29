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
package org.n52.series.db.da;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.sensorweb.spi.search.ProcedureSearchResult;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.ProcedureEntity;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.series.db.da.dao.ProcedureDao;
import org.n52.web.exception.ResourceNotFoundException;

public abstract class AbstractProcedureRepository extends SessionAwareRepository  {

	protected AbstractProcedureRepository(ServiceInfo serviceInfo) {
		super(serviceInfo);
	}
	
    @Override
    public Collection<SearchResult> searchFor(String searchString, String locale) {
        Session session = getSession();
        try {
            ProcedureDao procedureDao = new ProcedureDao(session);
            DbQuery parameters = createDefaultsWithLocale(locale);
            List<ProcedureEntity> found = procedureDao.find(searchString, parameters);
            return convertToSearchResults(found, locale);
        }
        finally {
            returnSession(session);
        }
    }

    @Override
    protected List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity< ? extends I18nEntity>> found,
                                                        String locale) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        for (DescribableEntity< ? extends I18nEntity> searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = getLabelFrom(searchResult,locale);
            results.add(new ProcedureSearchResult(pkid, label));
        }
        return results;
    }
    
    protected List<ProcedureEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
		return new ProcedureDao(session).getAllInstances(parameters);
	}
	
	protected ProcedureEntity getInstance(Long id, DbQuery parameters, Session session) throws DataAccessException {
		ProcedureDao procedureDAO = new ProcedureDao(session);
		ProcedureEntity result = procedureDAO.getInstance(id, parameters);
        if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
        return result;
	}

}
