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
import org.n52.io.response.v1.FeatureOutput;
import org.n52.series.api.v1.db.da.beans.DescribableEntity;
import org.n52.series.api.v1.db.da.beans.FeatureEntity;
import org.n52.series.api.v1.db.da.beans.I18nEntity;
import org.n52.series.api.v1.db.da.beans.ServiceInfo;
import org.n52.series.api.v1.db.da.dao.FeatureDao;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.sensorweb.spi.search.v1.FeatureSearchResult;
import org.n52.sensorweb.spi.SearchResult;

public class FeatureRepository extends SessionAwareRepository implements OutputAssembler<FeatureOutput>  {

    public FeatureRepository(ServiceInfo serviceInfo) {
        super(serviceInfo);
    }

    @Override
    public Collection<SearchResult> searchFor(String searchString, String locale) {
        Session session = getSession();
        try {
            FeatureDao featureDao = new FeatureDao(session);
            DbQuery parameters = createDefaultsWithLocale(locale);
            List<FeatureEntity> found = featureDao.find(searchString, parameters);
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
            results.add(new FeatureSearchResult(pkid, label));
        }
        return results;
    }

    @Override
    public List<FeatureOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            FeatureDao featureDao = new FeatureDao(session);
            List<FeatureOutput> results = new ArrayList<FeatureOutput>();
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
            FeatureDao featureDao = new FeatureDao(session);
            List<FeatureOutput> results = new ArrayList<FeatureOutput>();
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
            FeatureDao featureDao = new FeatureDao(session);
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
        result.setLabel(getLabelFrom(entity, parameters.getLocale()));
        return result;
    }
}
