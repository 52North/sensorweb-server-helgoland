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
package org.n52.series.dwd.srv;

import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.GeometryInfo;
import org.n52.io.response.GeometryType;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.PlatformOutput;
import org.n52.series.dwd.beans.ServiceInfo;
import org.n52.series.dwd.beans.WarnCell;
import org.n52.series.dwd.store.AlertStore;
import org.n52.web.ctrl.UrlHelper;
import org.springframework.beans.factory.annotation.Autowired;

public class GeometryOutputAdapter extends AbstractOuputAdapter<GeometryInfo> {

    private final AlertStore store;

    private final UrlHelper urlHelper = new UrlHelper();

    @Autowired
    private PlatformOutputAdapter platformOutputAdapter;

    public GeometryOutputAdapter(AlertStore store, ServiceInfo serviceInfo) {
        super(serviceInfo);
        this.store = store;
    }

    @Override
    public OutputCollection<GeometryInfo> getExpandedParameters(IoParameters query) {
        OutputCollection<GeometryInfo> outputCollection = createOutputCollection();
        for (WarnCell warnCell : getFilteredWarnCells(query, store)) {
            outputCollection.addItem(createExpanded(warnCell, query));
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<GeometryInfo> getCondensedParameters(IoParameters query) {
        OutputCollection<GeometryInfo> outputCollection = createOutputCollection();
        for (WarnCell warnCell : getFilteredWarnCells(query, store)) {
            outputCollection.addItem(createCondensed(warnCell, query));
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<GeometryInfo> getParameters(String[] items, IoParameters query) {
        OutputCollection<GeometryInfo> outputCollection = createOutputCollection();
        for (String id : items) {
            WarnCell warnCell = getWarnCell(id);
            if (warnCell != null) {
                if (query.isExpanded()) {
                    outputCollection.addItem(createExpanded(warnCell, query));
                } else {
                    outputCollection.addItem(createCondensed(warnCell, query));
                }
            }
        }
        return outputCollection;
    }

    @Override
    public GeometryInfo getParameter(String item, IoParameters query) {
        WarnCell warnCell = getWarnCell(item);
        if (warnCell != null) {
            return createExpanded(warnCell, query);
        }
        return null;
    }

    private WarnCell getWarnCell(String id) {
        return super.getWarnCell(GeometryType.extractId(id), store);
    }

    private GeometryInfo createCondensed(WarnCell item, IoParameters query) {
        GeometryInfo result = new GeometryInfo(GeometryType.PLATFORM_SITE);
        result.setId(item.getId());
        RequestSimpleParameterSet simpleParameterSet = query.toSimpleParameterSet();
        simpleParameterSet.setParameter(Parameters.PLATFORMS, IoParameters.getJsonNodeFrom(item.getId()));
        OutputCollection<PlatformOutput> platforms = platformOutputAdapter.getCondensedParameters(IoParameters.createFromQuery(simpleParameterSet));
        result.setPlatform(platforms.iterator().next());
        checkForHref(result, query);
        return result;
    }

    private GeometryInfo createExpanded(WarnCell item, IoParameters query) {
        GeometryInfo result = createCondensed(item, query);
        result.setService(getServiceOutput());
        result.setGeometry(item.getGeometry());
        return result;
    }

    @Override
    public boolean exists(String id, IoParameters parameters) {
        WarnCell warnCell = getWarnCell(id);
        if (warnCell != null) {
            return true;
        }
        return false;
    }

    private void checkForHref(GeometryInfo result, IoParameters parameters) {
        result.setHrefBase(urlHelper.getGeometriesHrefBaseUrl(parameters.getHrefBase()));
    }

    public PlatformOutputAdapter getPlatformOutputAdapter() {
        return platformOutputAdapter;
    }

    public void setPlatformOutputAdapter(PlatformOutputAdapter platformOutputAdapter) {
        this.platformOutputAdapter = platformOutputAdapter;
    }

}
