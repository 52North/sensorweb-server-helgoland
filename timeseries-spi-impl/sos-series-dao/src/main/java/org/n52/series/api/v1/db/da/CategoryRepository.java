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
import java.util.List;

import org.hibernate.Session;
import org.n52.io.response.v1.CategoryOutput;
import org.n52.series.db.da.AbstractCategoryRepository;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.beans.CategoryEntity;
import org.n52.series.db.da.beans.ServiceInfo;

public class CategoryRepository extends AbstractCategoryRepository implements OutputAssembler<CategoryOutput> {

    public CategoryRepository(ServiceInfo serviceInfo) {
        super(serviceInfo);
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
