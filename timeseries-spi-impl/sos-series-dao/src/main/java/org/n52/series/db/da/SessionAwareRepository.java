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
package org.n52.series.db.da;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ServiceOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ds.hibernate.SessionFactoryProvider;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SessionAwareRepository<DBQ extends AbstractDbQuery> {

    // TODO tackle issue #71
    private static final String DATASOURCE_PROPERTIES = "/datasource.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAwareRepository.class);

    private static final HibernateSessionHolder sessionHolder = createSessionHolderIfNeccessary();

    private final ServiceInfo serviceInfo;

    protected SessionAwareRepository(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public abstract Collection<SearchResult> searchFor(String queryString, String locale);

    protected abstract List<SearchResult> convertToSearchResults(List<? extends DescribableEntity<? extends I18nEntity>> found, String locale);

    protected abstract ServiceOutput getServiceOutput() throws DataAccessException;

    private static HibernateSessionHolder createSessionHolderIfNeccessary() {
        try (InputStream inputStream =  SessionAwareRepository.class.getResourceAsStream(DATASOURCE_PROPERTIES)){
            if (Configurator.getInstance() == null) {
                Properties connectionProviderConfig = new Properties();
                connectionProviderConfig.load(inputStream);
                SessionFactoryProvider provider = new SessionFactoryProvider();
                provider.initialize(connectionProviderConfig);

                // TODO configure hbm.xml mapping path

                return new HibernateSessionHolder(provider);
            } else {
                return new HibernateSessionHolder();
            }
        } catch (IOException e) {
            LOGGER.error("Could not establish database connection. Check {}", DATASOURCE_PROPERTIES, e);
            throw new RuntimeException("Could not establish database connection.");
        }
    }
    
    public void cleanup() {
        if (sessionHolder != null && Configurator.getInstance().getDataConnectionProvider() != null) {
            Configurator.getInstance().getDataConnectionProvider().cleanup();
        }
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    @Deprecated
    protected AbstractDbQuery createDefaultsWithLocale(String locale) {
//        if (locale == null) {
//            return DbQuery.createFrom(IoParameters.createDefaults());
//        }
//        Map<String, String> parameters = new HashMap<String, String>();
//        parameters.put("locale", locale);
//        return DbQuery.createFrom(createFromQuery(parameters));
        return getDbQuery(IoParameters.createDefaults(), locale);
    }
    
    protected abstract DBQ getDbQuery(IoParameters parameters);
    
    protected abstract DBQ getDbQuery(IoParameters parameters, String locale);

    protected Long parseId(String id) throws DataAccessException {
        try {
            return Long.parseLong(id);
        }
        catch (NumberFormatException e) {
            throw new DataAccessException("Illegal id: " + id);
        }
    }

    public void returnSession(Session session) {
        sessionHolder.returnSession(session);
    }

    public Session getSession() {
        if (sessionHolder == null) {
            createSessionHolderIfNeccessary();
        }
        try {
            return sessionHolder.getSession();
        }
        catch (OwsExceptionReport e) {
            throw new IllegalStateException("Could not get hibernate session.", e);
        }
    }

	protected String getLabelFrom(DescribableEntity<?> entity, String locale) {
		if (isi18nNameAvailable(entity, locale)) {
			return entity.getNameI18n(locale);
		} else if (isNameAvailable(entity)) {
			return entity.getName();
		} else {
			return entity.getCanonicalId();
		}
	}

	private boolean isNameAvailable(DescribableEntity<?> entity) {
		return entity.getName() != null && !entity.getName().isEmpty();
	}

	private boolean isi18nNameAvailable(DescribableEntity<?> entity, String locale) {
		return entity.getNameI18n(locale) != null && !entity.getNameI18n(locale).isEmpty();
	}
}
