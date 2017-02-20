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
package org.n52.io.extension.metadata;

import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class DatabaseMetadataDao {

    private final Session session;

    public DatabaseMetadataDao(Session session) {
        this.session = session;
    }

    public MetadataEntity<?> getInstance(Long key) {
        return (MetadataEntity<?>) session.get(MetadataEntity.class, key);
    }

    @SuppressWarnings("unchecked") // Hibernate
    public List<MetadataEntity<?>> getAllFor(Long id) {
        Criteria criteria = session.createCriteria(MetadataEntity.class)
                .add(Restrictions.eq("seriesId", id));
        return (List<MetadataEntity<?>>) criteria.list();
    }

    @SuppressWarnings("unchecked") // Hibernate
    List<MetadataEntity<?>> getSelected(Long id, Set<String> fields) {
        Criteria criteria = session.createCriteria(MetadataEntity.class)
                .add(Restrictions.eq("seriesId", id));
        addCaseInsensitivePropertyMatch(criteria, fields);
//        criteria.add(Restrictions.in("name", fields)); // not case insensitive
        return (List<MetadataEntity<?>>) criteria.list();
    }

    private void addCaseInsensitivePropertyMatch(Criteria criteria, Set<String> fields) {
        Disjunction disjunction = Restrictions.disjunction();
        for (String field : fields) {
            disjunction.add(Restrictions.eq("name", field).ignoreCase());
        }
        criteria.add(disjunction);
    }

    @SuppressWarnings("unchecked") // Hibernate
    List<String> getMetadataNames(Long id) {
        Criteria criteria = session.createCriteria(MetadataEntity.class)
                .add(Restrictions.eq("seriesId", id))
                .setProjection(Projections.property("name"));
        return (List<String>) criteria.list();
    }

}
