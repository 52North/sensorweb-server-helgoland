/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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
package org.n52.web.ctrl;


import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.series.spi.srv.ParameterService;
import org.n52.series.spi.srv.RawDataService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"EI_EXPOSE_REP2"})
public class ParameterBackwardsCompatibilityAdapter<T extends ParameterOutput> extends ParameterService<T> {

    private final ParameterService<T> service;

    public ParameterBackwardsCompatibilityAdapter(ParameterService<T> toCompose) {
        this.service = toCompose;
    }

    @Override
    public OutputCollection<T> getExpandedParameters(IoParameters parameters) {
        return service.getExpandedParameters(parameters.respectBackwardsCompatibility());
    }

    @Override
    public OutputCollection<T> getCondensedParameters(IoParameters parameters) {
        return service.getCondensedParameters(parameters.respectBackwardsCompatibility());
    }

    @Override
    public OutputCollection<T> getParameters(String[] items, IoParameters parameters) {
        return service.getParameters(items, parameters.respectBackwardsCompatibility());
    }

    @Override
    public T getParameter(String item, IoParameters parameters) {
        return service.getParameter(item, parameters.respectBackwardsCompatibility());
    }

    @Override
    public boolean exists(String id, IoParameters parameters) {
        return service.exists(id, parameters);
    }

    @Override
    public boolean supportsRawData() {
        return service.supportsRawData();
    }

    @Override
    public RawDataService getRawDataService() {
        return service.getRawDataService();
    }

    @Override
    public void setRawDataService(RawDataService rawDataService) {
        service.setRawDataService(rawDataService);
    }

}
