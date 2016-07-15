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
package org.n52.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import org.n52.io.response.dataset.Data;

public class DefaultIoHandlerFactory extends DatasetFactory<IoHandler> implements IoHandlerFactory {

    private static final String DEFAULT_CONFIG_FILE = "dataset-io-factory.properties";

    public DefaultIoHandlerFactory() {
        super(DEFAULT_CONFIG_FILE);
    }

    public DefaultIoHandlerFactory(File configFile) {
        super(configFile);
    }

    @Override
    public IoHandler<? extends Data> createHandler(String datasetType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected InputStream getFallbackConfigResource() {
         final String jarResource = "/" + DEFAULT_CONFIG_FILE;
        Class<?> clazz = DefaultIoHandlerFactory.class;
        return new BufferedInputStream(clazz.getResourceAsStream(jarResource));
    }

    @Override
    protected IoHandler initInstance(IoHandler instance) {
        return instance;
    }

    @Override
    protected Class<IoHandler> getTargetType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
