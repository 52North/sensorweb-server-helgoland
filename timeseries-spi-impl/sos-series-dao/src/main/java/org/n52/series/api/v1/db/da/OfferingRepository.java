/**
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
import org.n52.io.v1.data.OfferingOutput;
import org.n52.sensorweb.v1.spi.search.OfferingSearchResult;
import org.n52.sensorweb.v1.spi.search.SearchResult;
import org.n52.series.api.v1.db.da.beans.DescribableEntity;
import org.n52.series.api.v1.db.da.beans.I18nEntity;
import org.n52.series.api.v1.db.da.beans.OfferingEntity;
import org.n52.series.api.v1.db.da.beans.ServiceInfo;
import org.n52.series.api.v1.db.da.dao.OfferingDao;
import org.n52.web.ResourceNotFoundException;

public class OfferingRepository extends SessionAwareRepository implements OutputAssembler<OfferingOutput> {

    public OfferingRepository(ServiceInfo serviceInfo) {
        super(serviceInfo);
    }

    @Override
    public Collection<SearchResult> searchFor(String searchString, String locale) {
        Session session = getSession();
        try {
            OfferingDao offeringDao = new OfferingDao(session);
            DbQuery parameters = createDefaultsWithLocale(locale);
            List<OfferingEntity> found = offeringDao.find(searchString, parameters);
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
            results.add(new OfferingSearchResult(pkid, label));
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
        OfferingDao offeringDao = new OfferingDao(session);
        List<OfferingOutput> results = new ArrayList<OfferingOutput>();
        for (OfferingEntity procedureEntity : offeringDao.getAllInstances(parameters)) {
            results.add(createCondensed(procedureEntity, parameters));
        }
        return results;
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
        OfferingDao offeringDao = new OfferingDao(session);
        List<OfferingOutput> results = new ArrayList<OfferingOutput>();
        for (OfferingEntity procedureEntity : offeringDao.getAllInstances(parameters)) {
            results.add(createExpanded(procedureEntity, parameters));
        }
        return results;
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
        OfferingDao offeringDao = new OfferingDao(session);
        OfferingEntity result = offeringDao.getInstance(parseId(id), parameters);
        if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
        return createExpanded(result, parameters);
    }

    private OfferingOutput createExpanded(OfferingEntity entity, DbQuery parameters) throws DataAccessException {
        OfferingOutput result = createCondensed(entity, parameters);
        result.setService(getServiceOutput());
        return result;
    }

    private OfferingOutput createCondensed(OfferingEntity entity, DbQuery parameters) {
        OfferingOutput result = new OfferingOutput();
        result.setLabel(getLabelFrom(entity, parameters.getLocale()));
        result.setId(Long.toString(entity.getPkid()));
        return result;
    }
}
