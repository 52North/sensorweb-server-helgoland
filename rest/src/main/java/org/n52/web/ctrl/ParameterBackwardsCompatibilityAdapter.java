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
package org.n52.web.ctrl;


import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.series.spi.srv.ParameterService;
import org.n52.series.spi.srv.RawDataService;


public class ParameterBackwardsCompatibilityAdapter extends ParameterService<ParameterOutput> {

    private final ParameterService<ParameterOutput> service;

    public ParameterBackwardsCompatibilityAdapter(ParameterService<ParameterOutput> toCompose) {
        this.service = toCompose;
    }

    @Override
    public OutputCollection<ParameterOutput> getExpandedParameters(IoParameters query) {
        query = ensureBackwardsCompatibility(query);
        return service.getExpandedParameters(query);
    }

    @Override
    public OutputCollection<ParameterOutput> getCondensedParameters(IoParameters query) {
        query = ensureBackwardsCompatibility(query);
        return service.getCondensedParameters(query);
    }

    @Override
    public OutputCollection<ParameterOutput> getParameters(String[] items, IoParameters query) {
        query = ensureBackwardsCompatibility(query);
        return service.getParameters(items, query);
    }

    @Override
    public ParameterOutput getParameter(String item, IoParameters query) {
        query = ensureBackwardsCompatibility(query);
        return service.getParameter(item, query);
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

    private IoParameters ensureBackwardsCompatibility(IoParameters query) {
        return IoParameters.ensureBackwardsCompatibility(query);
    }

}
