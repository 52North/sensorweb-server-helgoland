/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
import org.n52.io.response.FeatureOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.SessionAwareRepository;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.FeatureDao;
import org.n52.series.spi.search.FeatureSearchResult;
import org.n52.series.spi.search.SearchResult;
import org.n52.web.exception.ResourceNotFoundException;

public class FeatureRepository extends SessionAwareRepository implements OutputAssembler<FeatureOutput> {

    @Override
    public boolean exists(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            FeatureDao dao = createDao(session);
            return dao.hasInstance(parseId(id), parameters, FeatureEntity.class);
        } finally {
            returnSession(session);
        }
    }

    private FeatureDao createDao(Session session) {
        return new FeatureDao(session);
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
        Session session = getSession();
        try {
            FeatureDao featureDao = createDao(session);
            DbQuery query = DbQuery.createFrom(parameters);
            List<FeatureEntity> found = featureDao.find(query);
            return convertToSearchResults(found, query);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity> found, DbQuery query) {
        String locale = query.getLocale();
        String hrefBase = urHelper.getFeaturesHrefBaseUrl(query.getHrefBase());
        List<SearchResult> results = new ArrayList<>();
        for (DescribableEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = searchResult.getLabelFrom(locale);
            results.add(new FeatureSearchResult(pkid, label, hrefBase));
        }
        return results;
    }

    @Override
    public List<FeatureOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            FeatureDao featureDao = createDao(session);
            List<FeatureOutput> results = new ArrayList<>();
            for (FeatureEntity featureEntity : featureDao.getAllInstances(parameters)) {
                results.add(createCondensed(featureEntity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<FeatureOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            FeatureDao featureDao = createDao(session);
            List<FeatureOutput> results = new ArrayList<>();
            for (FeatureEntity featureEntity : featureDao.getAllInstances(parameters)) {
                results.add(createExpanded(featureEntity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public FeatureOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            FeatureDao featureDao = createDao(session);
            FeatureEntity result = featureDao.getInstance(parseId(id), parameters);
            if (result == null) {
                throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
            }
            return createExpanded(result, parameters);
        } finally {
            returnSession(session);
        }
    }

    private FeatureOutput createExpanded(FeatureEntity entity, DbQuery parameters) throws DataAccessException {
        FeatureOutput result = createCondensed(entity, parameters);
        result.setService(getServiceOutput());
        return result;
    }

    private FeatureOutput createCondensed(FeatureEntity entity, DbQuery parameters) {
        FeatureOutput result = new FeatureOutput();
        result.setId(Long.toString(entity.getPkid()));
        result.setLabel(entity.getLabelFrom(parameters.getLocale()));
        result.setDomainId(entity.getDomainId());
        checkForHref(result, parameters);
        return result;
    }

    private void checkForHref(FeatureOutput result, DbQuery parameters) {
        if (parameters.getHrefBase() != null) {
            result.setHrefBase(urHelper.getFeaturesHrefBaseUrl(parameters.getHrefBase()));
        }
    }

}
