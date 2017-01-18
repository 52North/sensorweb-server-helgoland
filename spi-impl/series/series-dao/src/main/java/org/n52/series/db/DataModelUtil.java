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
package org.n52.series.db;

import org.hibernate.Criteria;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;

public final class DataModelUtil {

    public static String getSqlString(Criteria criteria) {
        CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
        SessionImplementor session = criteriaImpl.getSession();
        SessionFactoryImplementor factory = extractSessionFactory(criteria);
        CriteriaQueryTranslator translator
                = new CriteriaQueryTranslator(factory, criteriaImpl, criteriaImpl.getEntityOrClassName(),
                        CriteriaQueryTranslator.ROOT_SQL_ALIAS);
        String[] implementors = factory.getImplementors(criteriaImpl.getEntityOrClassName());

        CriteriaJoinWalker walker
                = new CriteriaJoinWalker((OuterJoinLoadable) factory.getEntityPersister(implementors[0]), translator,
                        factory, criteriaImpl, criteriaImpl.getEntityOrClassName(), session.getLoadQueryInfluencers());

        return walker.getSQLString();
    }

    public static boolean isEntitySupported(Class< ?> clazz, Criteria criteria) {
        SessionFactoryImplementor factory = extractSessionFactory(criteria);

        if (factory != null) {
            return factory.getAllClassMetadata().keySet().contains(clazz.getName());
        }
        return false;
    }

    public static SessionFactoryImplementor extractSessionFactory(Criteria criteria) {
        SessionImplementor session = getSessionImplementor(criteria);
        return session != null
                ? session.getFactory()
                : null;
    }

    private static SessionImplementor getSessionImplementor(Criteria criteria) {
        SessionImplementor session = null;
        if (criteria instanceof CriteriaImpl) {
            session = ((CriteriaImpl) criteria).getSession(); // ugly!
        } else if (criteria instanceof CriteriaImpl.Subcriteria) {
            CriteriaImpl temp = (CriteriaImpl) ((CriteriaImpl.Subcriteria) criteria).getParent();
            session = temp.getSession();
        }
        return session;
    }
}
