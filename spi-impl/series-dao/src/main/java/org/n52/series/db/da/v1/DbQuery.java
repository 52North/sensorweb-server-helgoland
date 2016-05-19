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
package org.n52.series.db.da.v1;

import static org.hibernate.criterion.Projections.projectionList;
import static org.hibernate.criterion.Projections.property;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.n52.io.request.IoParameters;
import org.n52.series.db.da.AbstractDbQuery;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;

public class DbQuery extends AbstractDbQuery {

    private DbQuery(IoParameters parameters) {
        super(parameters);
    }

    @Override
    public DetachedCriteria createDetachedFilterCriteria(String propertyName) {
        DetachedCriteria filter = DetachedCriteria.forClass(AbstractSeriesEntity.class);

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

        if (getParameters().getPlatforms() != null) {
            filter.createCriteria("platform")
                    .add(Restrictions.in(COLUMN_KEY, parseToIds(getParameters().getPlatforms())));
        }

        return filter.setProjection(projectionList().add(property(propertyName)));
    }



    public static DbQuery createFrom(IoParameters parameters) {
        return new DbQuery(parameters);
    }

}
