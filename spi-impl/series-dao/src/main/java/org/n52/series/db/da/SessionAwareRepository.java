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

import org.hibernate.Session;
import org.n52.io.crs.CRSUtils;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ServiceOutput;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SessionAwareRepository<DBQ extends AbstractDbQuery> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAwareRepository.class);

    private final CRSUtils crsUtils = CRSUtils.createEpsgStrictAxisOrder();

    private String databaseSrid; // if null, database is expected to have srs set properly

    @Autowired
    private HibernateSessionStore sessionStore;

    @Autowired
    private ServiceInfo serviceInfo;

    protected abstract ServiceOutput getServiceOutput() throws DataAccessException;

    protected abstract DBQ getDbQuery(IoParameters parameters);

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
    
    protected CRSUtils getCrsUtils() {
        return crsUtils;
    }
    
    protected String getDatabaseSrid() {
        return databaseSrid;
    }

    protected Long parseId(String id) throws BadRequestException {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            LOGGER.debug("Unable to parse {} to Long.", e);
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
    }

    public void returnSession(Session session) {
        sessionStore.returnSession(session);
    }

    public Session getSession() {
        try {
            return sessionStore.getSession();
        } catch (Throwable e) {
            throw new IllegalStateException("Could not get hibernate session.", e);
        }
    }

    protected String getLabelFrom(DescribableEntity entity, String locale) {
        if (isi18nNameAvailable(entity, locale)) {
            return entity.getNameI18n(locale);
        } else if (isNameAvailable(entity)) {
            return entity.getName();
        } else {
            return entity.getDomainId();
        }
    }

    private boolean isNameAvailable(DescribableEntity entity) {
        return entity.getName() != null && !entity.getName().isEmpty();
    }

    private boolean isi18nNameAvailable(DescribableEntity entity, String locale) {
        return entity.getNameI18n(locale) != null && !entity.getNameI18n(locale).isEmpty();
    }

    public void setDatabaseSrid(String databaseSrid) {
        this.databaseSrid = databaseSrid;
    }

}
