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

import static org.hibernate.criterion.Projections.projectionList;
import static org.hibernate.criterion.Projections.property;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.Criteria;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v2.PlatformOutput;
import org.n52.series.db.da.AbstractDbQuery;
import org.n52.series.db.da.beans.v2.ObservationEntityV2;
import org.n52.series.db.da.beans.v2.SeriesEntityV2;
import org.n52.series.db.da.beans.v2.SiteEntity;
import org.n52.series.db.da.v2.FeatureRepository.FeatureType;

public class DbQuery extends AbstractDbQuery {

	private DbQuery(IoParameters parameters) {
		super(parameters);
	}

    @Override
	public DetachedCriteria createDetachedFilterCriteria(String propertyName) {
	    
		DetachedCriteria filter = DetachedCriteria.forClass(SeriesEntityV2.class, "series");

		if (getParameters().getPhenomenon() != null) {
			filter.createCriteria("phenomenon")
					.add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getPhenomenon())));
		}
		if (getParameters().getProcedure() != null) {
			filter.createCriteria("procedure")
					.add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getProcedure())));
		}
		if (getParameters().getFeature() != null) {
		    
			FeatureType type = FeatureRepository.getTypeFor(getParameters().getFeature());
			Long id = preParse(getParameters().getFeature());
			if (FeatureType.SITE.equals(type) || FeatureType.TRACK_FROM_FEATURE.equals(type)) {
				filter.createCriteria("feature").add(Restrictions.eq(COLUMN_KEY, id));
			} else if (FeatureType.TRACK_FROM_OFFERING.equals(type)) {
			    DetachedCriteria trackFilter = DetachedCriteria.forClass(ObservationEntityV2.class, "o");
			    trackFilter.createCriteria("tracks").add(Restrictions.eq("pkid", id));
			    trackFilter.setProjection(Projections.distinct(Projections.property("seriesPkid")));
	            filter.add(Subqueries.propertyIn("pkid", trackFilter));
			}
		}
        if (getParameters().getType() != null) {
            final String type = getParameters().getType();
            if (type.equalsIgnoreCase(PlatformOutput.PlatformType.STATIONARY.getType())) {
                filter.createCriteria("feature")
                    .add(Restrictions.isNotNull("geom"));
            } else if (type.equalsIgnoreCase(PlatformOutput.PlatformType.MOBILE.getType())) {
                filter.createCriteria("feature")
                    .add(Restrictions.isNull("geom"));
            }
        }
		if (getParameters().getOther("platform") != null) {
			filter.createCriteria("feature")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getOther("platform"))));
		}
		if (getParameters().getCategory() != null) {
			filter.createCriteria("category")
					.add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getCategory())));
		}
		return filter.setProjection(Projections.property(propertyName));
	}

	public Long preParse(String feature) {
		if (feature.contains("_")) {
			return parseToId(feature.substring(feature.lastIndexOf("_") + 1));
		}
		return parseToId(feature);
	}

	public static DbQuery createFrom(IoParameters parameters) {
		return new DbQuery(parameters);
	}

	public static DbQuery createFrom(IoParameters parameters, String locale) {
		if (locale == null) {
			return new DbQuery(parameters);
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("locale", locale);
		return new DbQuery(IoParameters.createFromQuery(params));
	}

}
