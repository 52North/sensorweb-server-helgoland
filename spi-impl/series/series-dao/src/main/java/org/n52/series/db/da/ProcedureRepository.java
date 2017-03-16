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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ProcedureOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.ProcedureDao;
import org.n52.series.spi.search.ProcedureSearchResult;
import org.n52.series.spi.search.SearchResult;
import org.n52.web.exception.ResourceNotFoundException;

public class ProcedureRepository extends HierarchicalParameterRepository<ProcedureEntity, ProcedureOutput> {

    @Override
    public boolean exists(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            ProcedureDao dao = createDao(session);
            return dao.hasInstance(parseId(id), parameters, ProcedureEntity.class);
        } finally {
            returnSession(session);
        }
    }

    private ProcedureDao createDao(Session session) {
        return new ProcedureDao(session);
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
        Session session = getSession();
        try {
            ProcedureDao procedureDao = createDao(session);
            DbQuery query = getDbQuery(parameters);
            List<ProcedureEntity> found = procedureDao.find(query);
            return convertToSearchResults(found, query);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity> found,
                                                     DbQuery query) {
        List<SearchResult> results = new ArrayList<>();
        String locale = query.getLocale();
        String hrefBase = urHelper.getProceduresHrefBaseUrl(query.getHrefBase());
        for (DescribableEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = searchResult.getLabelFrom(locale);
            results.add(new ProcedureSearchResult(pkid, label, hrefBase));
        }
        return results;
    }

    @Override
    public List<ProcedureOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllCondensed(parameters, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<ProcedureOutput> getAllCondensed(DbQuery parameters, Session session) throws DataAccessException {
        return createCondensed(getAllInstances(parameters, session), parameters);
    }

    @Override
    public List<ProcedureOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllExpanded(parameters, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<ProcedureOutput> getAllExpanded(DbQuery parameters, Session session) throws DataAccessException {
        return createExpanded(getAllInstances(parameters, session), parameters);
    }

    @Override
    public ProcedureOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getInstance(id, parameters, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public ProcedureOutput getInstance(String id, DbQuery parameters, Session session) throws DataAccessException {
        ProcedureEntity result = getInstance(parseId(id), parameters, session);
        return createExpanded(result, parameters);
    }

    private List<ProcedureEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
        return createDao(session).getAllInstances(parameters);
    }

    private ProcedureEntity getInstance(Long id, DbQuery parameters, Session session) throws DataAccessException {
        ProcedureDao procedureDAO = createDao(session);
        ProcedureEntity result = procedureDAO.getInstance(id, parameters);
        if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
        return result;
    }

    @Override
    protected ProcedureOutput createCondensed(ProcedureEntity entity, DbQuery parameters) {
        ProcedureOutput result = new ProcedureOutput();
        result.setLabel(entity.getLabelFrom(parameters.getLocale()));
        result.setId(Long.toString(entity.getPkid()));
        result.setDomainId(entity.getDomainId());
        checkForHref(result, parameters);
        return result;
    }

    @Override
    protected ProcedureOutput createExpanded(ProcedureEntity entity, DbQuery parameters) {
        ProcedureOutput result = createCondensed(entity, parameters);
        if (parameters.getHrefBase() != null) {
            result.setService(getCondensedExtendedService(entity.getService(), parameters));
        } else {
            result.setService(getCondensedService(entity.getService(), parameters));
        }
        result.setParents(createCondensed(entity.getParents(), parameters));
        result.setChildren(createCondensed(entity.getChildren(), parameters));
        return result;
    }

    protected List<ProcedureOutput> createCondensedHierarchyMembers(Set<ProcedureEntity> members, DbQuery parameters) {
        return members == null
                ? Collections.emptyList()
                : members.stream()
                    .map(e -> createCondensed(e, parameters))
                    .collect(Collectors.toList());
    }

    private void checkForHref(ProcedureOutput result, DbQuery parameters) {
        if (parameters.getHrefBase() != null) {
            result.setHrefBase(urHelper.getProceduresHrefBaseUrl(parameters.getHrefBase()));
        }
    }

}
