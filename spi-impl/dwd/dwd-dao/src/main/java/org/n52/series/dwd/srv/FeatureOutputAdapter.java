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

import org.n52.io.request.FilterResolver;
import org.n52.io.request.IoParameters;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.OutputCollection;
import org.n52.series.dwd.beans.ServiceInfo;
import org.n52.series.dwd.beans.WarnCell;
import org.n52.series.dwd.rest.AlertCollection;
import org.n52.series.dwd.store.AlertStore;
import org.n52.web.ctrl.UrlHelper;

public class FeatureOutputAdapter extends AbstractOuputAdapter<FeatureOutput> {

    private final AlertStore store;

    private final UrlHelper urlHelper = new UrlHelper();

    public FeatureOutputAdapter(AlertStore store, ServiceInfo serviceInfo) {
        super(serviceInfo);
        this.store = store;
    }

    @Override
    public OutputCollection<FeatureOutput> getExpandedParameters(IoParameters query) {
        OutputCollection<FeatureOutput> outputCollection = createOutputCollection();
        FilterResolver filterResolver = query.getFilterResolver();
        if ( !filterResolver.shallBehaveBackwardsCompatible()) {
            for (WarnCell warnCell : getFilteredWarnCells(query, store)) {
                outputCollection.addItem(createExpanded(warnCell, query));
            }
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<FeatureOutput> getCondensedParameters(IoParameters query) {
        OutputCollection<FeatureOutput> outputCollection = createOutputCollection();
        FilterResolver filterResolver = query.getFilterResolver();
        if ( !filterResolver.shallBehaveBackwardsCompatible()) {
            for (WarnCell warnCell : getFilteredWarnCells(query, store)) {
                outputCollection.addItem(createCondensed(warnCell, query));
            }
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<FeatureOutput> getParameters(String[] items, IoParameters query) {
        OutputCollection<FeatureOutput> outputCollection = createOutputCollection();
        FilterResolver filterResolver = query.getFilterResolver();
        if ( !filterResolver.shallBehaveBackwardsCompatible()) {
            for (String id : items) {
                WarnCell warnCell = getWarnCell(id, store);
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
    public FeatureOutput getParameter(String item, IoParameters query) {
        WarnCell warnCell = getWarnCell(item, store);
        if (warnCell != null) {
            return createExpanded(warnCell, query);
        }
        return null;
    }

    private FeatureOutput createCondensed(WarnCell item, IoParameters query) {
        FeatureOutput result = new FeatureOutput();
        result.setLabel(getLabel(item.getId()));
        result.setId(item.getId());
        result.setDomainId(item.getId());
        checkForHref(result, query);
        return result;
    }

    private String getLabel(String id) {
        AlertCollection currentAlerts = store.getCurrentAlerts();
        if (currentAlerts.hasWarning() && currentAlerts.getWarnings().containsKey(id)) {
            return currentAlerts.getWarnings().get(id).get(0).getRegionName();
        }
        if (currentAlerts.hasVorabInformation() && currentAlerts.getVorabInformation().containsKey(id)) {
            return currentAlerts.getVorabInformation().get(id).get(0).getRegionName();
        }
        return "";
    }

    private FeatureOutput createExpanded(WarnCell item, IoParameters query) {
        FeatureOutput result = createCondensed(item, query);
        result.setService(getServiceOutput());
        return result;
    }

    @Override
    public boolean exists(String id, IoParameters parameters) {
        return getWarnCell(id, store) != null ? true : false;
    }

    private void checkForHref(FeatureOutput result, IoParameters parameters) {
        result.setHrefBase(urlHelper.getFeaturesHrefBaseUrl(parameters.getHrefBase()));
    }

}
