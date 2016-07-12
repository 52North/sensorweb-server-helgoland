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
package org.n52.series.db.da.dao.v1;

import static org.hibernate.criterion.Projections.projectionList;
import static org.hibernate.criterion.Projections.property;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v1.ext.PlatformType;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;

public class DbQuery extends AbstractDbQuery {

    private DbQuery(IoParameters parameters) {
        super(parameters);
    }

    @Override
    public DetachedCriteria createDetachedFilterCriteria(String propertyName) {
        DetachedCriteria filter = DetachedCriteria.forClass(AbstractSeriesEntity.class);

        filterWithSingularParmameters(filter); // stay backwards compatible

        if (hasValues(getParameters().getPhenomena())) {
            filter.createCriteria("phenomenon")
            .add(Restrictions.in(COLUMN_KEY, parseToIds(getParameters().getPhenomena())));
        }
        if (hasValues(getParameters().getProcedures())) {
            filter.createCriteria("procedure")
            .add(Restrictions.in(COLUMN_KEY, parseToIds(getParameters().getProcedures())));
        }
        if (hasValues(getParameters().getOfferings())) {
            // here procedure == offering
            filter.createCriteria("procedure")
            .add(Restrictions.in(COLUMN_KEY, parseToIds(getParameters().getOfferings())));
        }
        if (hasValues(getParameters().getFeatures())) {
            filter.createCriteria("feature")
                    .add(Restrictions.in(COLUMN_KEY, parseToIds(getParameters().getFeatures())));
        }
        if (hasValues(getParameters().getCategories())) {
            filter.createCriteria("category")
                    .add(Restrictions.in(COLUMN_KEY, parseToIds(getParameters().getCategories())));
        }
        if (hasValues(getParameters().getPlatforms())) {
            Set<String> stationaryIds = getStationary(getParameters().getPlatforms());
            Set<String> platformIds = getNonStationary(getParameters().getPlatforms());
            if (!stationaryIds.isEmpty()) {
                filter.createCriteria("feature").add(Restrictions.in(COLUMN_KEY, parseToIds(stationaryIds)));
            }
            if (!platformIds.isEmpty()) {
                filter.createCriteria("platform").add(Restrictions.in(COLUMN_KEY, parseToIds(platformIds)));
            }
        }
        if (hasValues(getParameters().getSeries())) {
            filter.add(Restrictions.in(COLUMN_KEY, parseToIds(getParameters().getSeries())));
        }

        return filter.setProjection(projectionList().add(property(propertyName)));
    }

    private boolean hasValues(Set<String> values) {
        return values != null && !values.isEmpty();
    }

    private Set<String> getStationary(Set<String> platforms) {
        Set<String> set = new HashSet<>();
        for (String platform : platforms) {
            if (PlatformType.isStationaryId(platform)) {
                set.add(PlatformType.extractId(platform));
            }
        }
        return set;
    }

    private Set<String> getNonStationary(Set<String> platforms) {
        Set<String> set = new HashSet<>();
        for (String platform : platforms) {
            if (!PlatformType.isStationaryId(platform)) {
                set.add(PlatformType.extractId(platform));
            }
        }
        return set;
    }

    @Deprecated
    private void filterWithSingularParmameters(DetachedCriteria filter) {
        // old query parameter to stay backward compatible
        if (getParameters().getPhenomenon() != null) {
            filter.createCriteria("phenomenon")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getPhenomenon())));
        }
        if (getParameters().getProcedure() != null) {
            filter.createCriteria("procedure")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getProcedure())));
        }
        if (getParameters().getOffering() != null) {
            // here procedure == offering
            filter.createCriteria("procedure")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getOffering())));
        }
        if (getParameters().getFeature() != null) {
            filter.createCriteria("feature")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getFeature())));
        }
        if (getParameters().getStation() != null) {
            // here feature == station
            filter.createCriteria("feature")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getStation())));
        }
        if (getParameters().getCategory() != null) {
            filter.createCriteria("category")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getCategory())));
        }
    }

    public static DbQuery createFrom(IoParameters parameters) {
        return new DbQuery(parameters);
    }

    @Override
    public String toString() {
        return "DbQuery{ parameters=" + getParameters().toString() + "'}'";
    }



}
