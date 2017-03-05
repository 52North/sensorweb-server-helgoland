/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.series.db.da;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.OfferingOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.OfferingDao;
import org.n52.series.spi.search.OfferingSearchResult;
import org.n52.series.spi.search.SearchResult;
import org.n52.web.exception.ResourceNotFoundException;

public class OfferingRepository extends HierarchicalParameterRepository<OfferingEntity, OfferingOutput>{

    @Override
    public boolean exists(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            OfferingDao dao = createDao(session);
            return dao.hasInstance(parseId(id), parameters, OfferingEntity.class);
        } finally {
            returnSession(session);
        }
    }

    private OfferingDao createDao(Session session) {
        return new OfferingDao(session);
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
        Session session = getSession();
        try {
            OfferingDao dao = createDao(session);
            DbQuery query = getDbQuery(parameters);
            List<OfferingEntity> found = dao.find(query);
            return convertToSearchResults(found, query);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, DbQuery query) {
        String locale = query.getLocale();
        String hrefBase = urHelper.getOfferingsHrefBaseUrl(query.getHrefBase());
        List<SearchResult> results = new ArrayList<>();
        for (DescribableEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = searchResult.getLabelFrom(locale);
            results.add(new OfferingSearchResult(pkid, label, hrefBase));
        }
        return results;
    }

    @Override
    public List<OfferingOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllCondensed(parameters, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<OfferingOutput> getAllCondensed(DbQuery parameters, Session session) throws DataAccessException {
        return createCondensed(getAllInstances(parameters, session), parameters);
    }

    @Override
    public List<OfferingOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllExpanded(parameters, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<OfferingOutput> getAllExpanded(DbQuery parameters, Session session) throws DataAccessException {
        return createExpanded(getAllInstances(parameters, session), parameters);
    }

    @Override
    public OfferingOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getInstance(id, parameters, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public OfferingOutput getInstance(String id, DbQuery parameters, Session session) throws DataAccessException {
        OfferingEntity result = getInstance(parseId(id), parameters, session);
        return createExpanded(result, parameters);
    }

    private List<OfferingEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
        return createDao(session).getAllInstances(parameters);
    }

    private OfferingEntity getInstance(Long id, DbQuery parameters, Session session) throws DataAccessException {
        OfferingDao dao = createDao(session);
        OfferingEntity result = dao.getInstance(id, parameters);
        if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
        return result;
    }

    @Override
    protected OfferingOutput createExpanded(OfferingEntity entity, DbQuery parameters) throws DataAccessException {
        OfferingOutput result = createCondensed(entity, parameters);
        if (parameters.getHrefBase() != null) {
            result.setService(getCondensedExtendedService(entity.getService(), parameters));
        } else {
            result.setService(getCondensedService(entity.getService(), parameters));
        }
        return result;
    }

    @Override
    protected OfferingOutput createCondensed(OfferingEntity entity, DbQuery parameters) {
        OfferingOutput result = new OfferingOutput();
        result.setLabel(entity.getLabelFrom(parameters.getLocale()));
        result.setId(Long.toString(entity.getPkid()));
        result.setDomainId(entity.getDomainId());
        checkForHref(result, parameters);
        return result;
    }

    private void checkForHref(OfferingOutput result, DbQuery parameters) {
        if (parameters.getHrefBase() != null) {
            result.setHrefBase(urHelper.getOfferingsHrefBaseUrl(parameters.getHrefBase()));
        }
    }
}
