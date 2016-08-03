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
package org.n52.io.dwd;

import java.util.HashSet;
import java.util.Set;

import org.n52.io.IoFactory;
import org.n52.io.IoHandler;
import org.n52.io.MimeType;
import org.n52.io.response.dataset.dwd.DwdAlertData;
import org.n52.io.response.dataset.dwd.DwdAlertDatasetOutput;
import org.n52.io.response.dataset.dwd.DwdAlertValue;

public class DwdAlertIoFactory extends IoFactory<DwdAlertData, DwdAlertDatasetOutput, DwdAlertValue>{

    @Override
    public boolean isAbleToCreateHandlerFor(String outputMimeType) {
        return MimeType.isKnownMimeType(outputMimeType);
    }

    @Override
    public Set<String> getSupportedMimeTypes() {
        return new HashSet<>();
    }

    @Override
    public IoHandler<DwdAlertData> createHandler(String outputMimeType) {
        String msg = "The requested media type '" + outputMimeType + "' is not supported.";
        IllegalArgumentException exception = new IllegalArgumentException(msg);
        throw exception;
    }

}
