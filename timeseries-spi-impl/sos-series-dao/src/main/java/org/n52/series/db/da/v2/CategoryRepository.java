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
package org.n52.series.db.da.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v2.CategoryOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.sensorweb.spi.search.CategorySearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.CategoryEntity;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.series.db.da.dao.v2.CategoryDao;
import org.n52.web.exception.ResourceNotFoundException;

public class CategoryRepository extends ExtendedSessionAwareRepository implements OutputAssembler<CategoryOutput> {

	public CategoryRepository(ServiceInfo serviceInfo) {
		super(serviceInfo);
	}
	
	@Override
    public Collection<SearchResult> searchFor(String searchString, String locale) {
        Session session = getSession();
        try {
            CategoryDao categoryDao = new CategoryDao(session);
            DbQuery parameters = getDbQuery(IoParameters.createDefaults(), locale);
            List<CategoryEntity> found = categoryDao.find(searchString, parameters);
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
            String label = getLabelFrom(searchResult, locale);
            results.add(new CategorySearchResult(pkid, label));
        }
        return results;
    }
	
	@Override
    public List<CategoryOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<CategoryOutput> results = new ArrayList<CategoryOutput>();
            for (CategoryEntity categoryEntity : getAllInstances(parameters, session)) {
                results.add(createCondensed(categoryEntity, parameters));
            }
            return results;
        }
        finally {
            returnSession(session);
        }
    }

    @Override
    public List<CategoryOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<CategoryOutput> results = new ArrayList<CategoryOutput>();
            for (CategoryEntity categoryEntity : getAllInstances(parameters, session)) {
                results.add(createExpanded(categoryEntity, parameters));
            }
            return results;
        }
        finally {
            returnSession(session);
        }
    }

    @Override
    public CategoryOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            CategoryEntity entity = getInstance(parseId(id), parameters, session);
            if (entity != null) {
                return createExpanded(entity, parameters);
            }
            return null;
        }
        finally {
            returnSession(session);
        }
    }
    
    protected List<CategoryEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
		return new CategoryDao(session).getAllInstances(parameters);
	}
	
	protected CategoryEntity getInstance(Long id, DbQuery parameters, Session session) throws DataAccessException {
		CategoryDao categoryDao = new CategoryDao(session);
		CategoryEntity result = categoryDao.getInstance(id, parameters);
        if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
        return result;
	}

    private CategoryOutput createExpanded(CategoryEntity entity, DbQuery parameters) throws DataAccessException {
        CategoryOutput result = createCondensed(entity, parameters);
        result.setService(getServiceOutput());
        return result;
    }

    private CategoryOutput createCondensed(CategoryEntity entity, DbQuery parameters) {
        CategoryOutput result = new CategoryOutput();
        result.setId(Long.toString(entity.getPkid()));
        result.setLabel(getLabelFrom(entity, parameters.getLocale()));
        return result;
    }

}
