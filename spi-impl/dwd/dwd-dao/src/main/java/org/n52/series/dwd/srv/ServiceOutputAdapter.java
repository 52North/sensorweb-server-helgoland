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
package org.n52.series.dwd.srv;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ServiceOutput;
import org.n52.series.dwd.beans.ServiceInfo;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.ctrl.UrlHelper;

public class ServiceOutputAdapter extends ParameterService<ServiceOutput> {

    private ServiceInfo serviceInfo;

    private final UrlHelper urlHelper = new UrlHelper();

    public ServiceOutputAdapter(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Override
    public OutputCollection<ServiceOutput> getExpandedParameters(IoParameters query) {
        OutputCollection<ServiceOutput> outputCollection = createOutputCollection();
        outputCollection.addItem(createExpanded(serviceInfo.getServiceId(), query));
        return outputCollection;
    }

    @Override
    public OutputCollection<ServiceOutput> getCondensedParameters(IoParameters query) {
        OutputCollection<ServiceOutput> outputCollection = createOutputCollection();
        outputCollection.addItem(createCondensed(serviceInfo.getServiceId(), query));
        return outputCollection;
    }

    @Override
    public OutputCollection<ServiceOutput> getParameters(String[] items, IoParameters query) {
        OutputCollection<ServiceOutput> outputCollection = createOutputCollection();
        outputCollection.addItem(createCondensed(serviceInfo.getServiceId(), query));
        return outputCollection;
    }

    @Override
    public ServiceOutput getParameter(String item, IoParameters query) {
            String serviceId = serviceInfo.getServiceId();
            return serviceId.equals(item)
                    ? createExpanded(item, query)
                    : null;
    }

    private ServiceOutput createCondensed(String item, IoParameters query) {
        ServiceOutput result = new ServiceOutput();
        result.setLabel(serviceInfo.getServiceDescription());
        result.setId(serviceInfo.getServiceId());
        checkForHref(result, query);
        return result;
    }

    private ServiceOutput createExpanded(String item, IoParameters query) {
        ServiceOutput result = createCondensed(item, query);
        return result;
    }

    @Override
    public boolean exists(String id) {
        return serviceInfo.getServiceId().equals(id);
    }

    private void checkForHref(ServiceOutput result, IoParameters parameters) {
        result.setHrefBase(urlHelper.getServicesHrefBaseUrl(parameters.getHrefBase()));
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

}
