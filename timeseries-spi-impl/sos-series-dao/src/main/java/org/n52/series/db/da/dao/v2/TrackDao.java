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
package org.n52.series.db.da.dao.v2;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.v2.I18nSiteEntity;
import org.n52.series.db.da.beans.v2.I18nTrackEntity;
import org.n52.series.db.da.beans.v2.ObservationEntityV2;
import org.n52.series.db.da.beans.v2.SeriesEntityV2;
import org.n52.series.db.da.beans.v2.TrackEntity;
import org.n52.series.db.da.v2.DbQuery;

import com.google.common.base.Strings;
import org.n52.io.request.IoParameters;
import org.n52.series.db.da.beans.v2.SiteEntity;

public class TrackDao extends AbstractDao<TrackEntity> {

	public TrackDao(Session session) {
		super(session);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TrackEntity> find(String search, DbQuery query) {
		Criteria criteria = getDefaultCriteria();
		if (hasTranslation(query, I18nTrackEntity.class)) {
			criteria = query.addLocaleTo(criteria, I18nTrackEntity.class);
		}
		criteria.add(Restrictions.ilike("name", "%" + search + "%"));
		return criteria.list();
	}

	@Override
	public TrackEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
		 return (TrackEntity) session.get(TrackEntity.class, key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TrackEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
		Criteria criteria = getDefaultCriteria("t");
		// TODO does this really work???
		Criteria trackLocationsCriteria = criteria.createCriteria("trackLocations");
		trackLocationsCriteria.add(Restrictions.isNotNull("geom"));
		criteria.add(Restrictions.isNotEmpty("trackLocations"));
        if (hasTranslation(parameters, I18nSiteEntity.class)) {
            parameters.addLocaleTo(criteria, I18nSiteEntity.class);
        }
        
        // TODO add DetachedCriteria to DbQuery, is this required?
        DetachedCriteria filter = parameters.createDetachedFilterCriteria("pkid");
        trackLocationsCriteria.add(Subqueries.propertyIn("seriesPkid", filter));
        
        parameters.addSpatialFilterTo(trackLocationsCriteria, parameters);
        parameters.addPagingTo(criteria);
        return (List<TrackEntity>) criteria.list();
	}
	
	@Override
	protected Criteria getDefaultCriteria() {
		return getDefaultCriteria(null);
	}

	private Criteria getDefaultCriteria(String alias) {
		Criteria criteria;
		if (Strings.isNullOrEmpty(alias)) {
			criteria = session.createCriteria(TrackEntity.class);
		} else {
			criteria = session.createCriteria(TrackEntity.class, alias);
		}
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return criteria;
	}

	@SuppressWarnings("unchecked")
	public List<Long> getRelatedPlatforms(Long pkid) { // XXX
		/*
		 * SELECT DISTINCT f.featureOfInterestId FROM observation o, series s,
		 * featureOfInterest f, offering of, observationHasOffering oo WHERE
		 * of.offeringId = 1 AND of.offeringId = oo.offeringId AND
		 * oo.observationId = o.observationId AND o.seriesId = s.seriesId AND
		 * s.featureOfInterestId = f.featureOfInterestId;
		 */
		DetachedCriteria dc = DetachedCriteria.forClass(ObservationEntityV2.class);
		dc.createCriteria("tracks").add(Restrictions.eq("pkid", pkid));
		dc.setProjection(Projections.distinct(Projections.property("seriesPkid")));
		
		Criteria c = session.createCriteria(SeriesEntityV2.class, "s");
		c.createCriteria("feature", "f").setProjection(Projections.distinct(Projections.property("f.pkid")));
		c.add(Subqueries.propertyIn("s.pkid", dc));
		
		return c.list();
	}
	
	@Override
    public int getCount() throws DataAccessException {
        Criteria criteria = getDefaultCriteria().setProjection(Projections.distinct(Projections.property("pkid")));
        return criteria != null ? criteria.list().size() : 0;
    }
	

}
