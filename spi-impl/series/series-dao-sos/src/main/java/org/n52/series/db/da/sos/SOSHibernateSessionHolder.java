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
package org.n52.series.db.da.sos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.hibernate.Session;
import org.n52.series.db.HibernateSessionStore;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ds.hibernate.SessionFactoryProvider;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateSessionHolder implements HibernateSessionStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSessionHolder.class);

    private static final String DATASOURCE_PROPERTIES = "/datasource.properties";

    private HibernateSessionHolder sessionHolder;

    private static SessionFactoryProvider provider;

    public static HibernateSessionHolder createSessionHolder() {
        if (Configurator.getInstance() == null) {
            try (InputStream inputStream = SOSHibernateSessionHolder.class.getResourceAsStream(DATASOURCE_PROPERTIES)) {
                LOGGER.debug("SOS Configurator not present, trying to load DB config from '{}'", DATASOURCE_PROPERTIES);
                if (inputStream == null) {
                    LOGGER.error("DB config '{}' is missing!", DATASOURCE_PROPERTIES);
                    throw new RuntimeException("Could not establish database connection.");
                }
                Properties connectionProviderConfig = new Properties();
                connectionProviderConfig.load(inputStream);
                provider = new SessionFactoryProvider();
                provider.initialize(connectionProviderConfig);
                return new HibernateSessionHolder(provider);
            } catch (IOException e) {
                LOGGER.error("Could not establish database connection. Check '{}'", DATASOURCE_PROPERTIES, e);
                throw new RuntimeException("Could not establish database connection.");
            }
        } else {
            return new HibernateSessionHolder();
        }

    }

    @Override
    public void returnSession(Session session) {
        sessionHolder.returnSession(session);
    }

    @Override
    public Session getSession() {
        if (sessionHolder == null) {
            sessionHolder = createSessionHolder();
        }
        try {
            return sessionHolder.getSession();
        } catch (OwsExceptionReport e) {
            throw new IllegalStateException("Could not get hibernate session.", e);
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("shutdown '{}'", getClass().getSimpleName());
        if (provider != null) {
            provider.cleanup();
        }
    }

}
