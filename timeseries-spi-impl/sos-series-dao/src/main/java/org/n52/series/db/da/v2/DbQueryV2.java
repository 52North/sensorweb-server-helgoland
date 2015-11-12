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

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.n52.io.request.IoParameters;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.beans.v2.SeriesEntityV2;
import org.n52.series.db.da.beans.v2.SiteEntity;
import org.n52.series.db.da.dao.v2.SiteDao;
import org.n52.series.db.da.v2.FeatureRepository.FeatureType;

public class DbQueryV2 extends DbQuery {

	private DbQueryV2(IoParameters parameters) {
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
			if (FeatureType.SITE.equals(type) || FeatureType.TRACK_FEATURE.equals(type)) {
				Long id = preParse(getParameters().getFeature());
				filter.createCriteria("feature").add(Restrictions.eq(COLUMN_KEY, id));
			} else if (FeatureType.TRACK_OFFERING.equals(type)) {
				Long id = preParse(getParameters().getFeature());
				filter.createCriteria("observations").createCriteria("tracks").add(Restrictions.eq(COLUMN_KEY, id));
			}
		}
		if (getParameters().getOther("platform") != null) {
			filter.createCriteria("feature").add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getOther("platform"))));
		}
		if (getParameters().getCategory() != null) {
			filter.createCriteria("category")
					.add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getCategory())));
		}

		return filter.setProjection(projectionList().add(property(propertyName)));
	}

	private Long preParse(String feature) {
		if (feature.contains("_")) {
			return parseToId(feature.substring(feature.lastIndexOf("_")));
		}
		return parseToId(feature);
	}

	public static DbQuery createFrom(IoParameters parameters) {
		return new DbQueryV2(parameters);
	}

	public static DbQuery createFrom(IoParameters parameters, String locale) {
		if (locale == null) {
			new DbQueryV2(parameters);
		}
		Map<String, String> params = new HashMap<String, String>();
		params.put("locale", locale);
		return new DbQueryV2(IoParameters.createFromQuery(params));
	}

}
