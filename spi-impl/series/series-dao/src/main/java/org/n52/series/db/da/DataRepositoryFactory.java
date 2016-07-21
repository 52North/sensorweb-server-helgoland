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

import java.io.File;

import org.n52.io.ConfigTypedFactory;
import org.n52.series.db.HibernateSessionStore;
import org.n52.series.db.beans.ServiceInfo;
import org.springframework.beans.factory.annotation.Autowired;


public class DataRepositoryFactory extends ConfigTypedFactory<DataRepository> {

    private static final String DEFAULT_CONFIG_FILE = "dataset-repository-factory.properties";

    // TODO autowiring

    @Autowired
    private HibernateSessionStore sessionStore;

    @Autowired
    private ServiceInfo serviceInfo;

    public DataRepositoryFactory() {
        super(DEFAULT_CONFIG_FILE);
    }

    public DataRepositoryFactory(File configFile) {
        super(configFile);
    }

    @Override
    protected DataRepository initInstance(DataRepository instance) {
        instance.setServiceInfo(serviceInfo);
        instance.setSessionStore(sessionStore);
        return instance;
    }

    @Override
    protected String getFallbackConfigResource() {
        return DEFAULT_CONFIG_FILE;
    }

    @Override
    protected Class<DataRepository> getTargetType() {
        return DataRepository.class;
    }

    public HibernateSessionStore getSessionStore() {
        return sessionStore;
    }

    public void setSessionStore(HibernateSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

}
