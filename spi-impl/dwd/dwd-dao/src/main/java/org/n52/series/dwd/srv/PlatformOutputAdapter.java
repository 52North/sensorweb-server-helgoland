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

import java.util.Comparator;

import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.v1.ext.PlatformOutput;
import org.n52.io.response.v1.ext.PlatformType;
import org.n52.series.dwd.beans.ServiceInfo;
import org.n52.series.dwd.beans.WarnCell;
import org.n52.series.dwd.rest.AlertCollection;
import org.n52.series.dwd.store.AlertStore;
import org.springframework.beans.factory.annotation.Autowired;

public class PlatformOutputAdapter extends AbstractOuputAdapter<PlatformOutput> {

    private final AlertStore store;

    @Autowired
    private DatasetOutputAdapter seriesOutputAdapter;

    public PlatformOutputAdapter(AlertStore store, ServiceInfo serviceInfo) {
        super(serviceInfo);
        this.store = store;
    }

    private OutputCollection<PlatformOutput> createOutputCollection() {
        return new OutputCollection<PlatformOutput>() {
            @Override
            protected Comparator<PlatformOutput> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    @Override
    public OutputCollection<PlatformOutput> getExpandedParameters(IoParameters query) {
        OutputCollection<PlatformOutput> outputCollection = createOutputCollection();
        for (WarnCell warnCell : getFilteredWarnCells(query, store)) {
            outputCollection.addItem(createExpanded(warnCell, query));
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<PlatformOutput> getCondensedParameters(IoParameters query) {
        OutputCollection<PlatformOutput> outputCollection = createOutputCollection();
        for (WarnCell warnCell : getFilteredWarnCells(query, store)) {
            outputCollection.addItem(createCondensed(warnCell, query));
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<PlatformOutput> getParameters(String[] items, IoParameters query) {
        OutputCollection<PlatformOutput> outputCollection = createOutputCollection();
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
    public PlatformOutput getParameter(String item, IoParameters query) {
        WarnCell warnCell = getWarnCell(item);
        if (warnCell != null) {
            return createExpanded(warnCell, query);
        }
        return null;
    }

    private WarnCell getWarnCell(String id) {
        return super.getWarnCell(PlatformType.extractId(id), store);
    }

    private PlatformOutput createCondensed(WarnCell item, IoParameters query) {
        PlatformOutput result = new PlatformOutput(PlatformType.STATIONARY_INSITU);
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

    private PlatformOutput createExpanded(WarnCell item, IoParameters query) {
        PlatformOutput result = createCondensed(item, query);
        result.setService(getServiceOutput());
        RequestSimpleParameterSet simpleParameterSet = query.toSimpleParameterSet();
        simpleParameterSet.addParameter(Parameters.PLATFORMS, IoParameters.getJsonNodeFrom(item.getId()));
        result.setSeries(seriesOutputAdapter.getCondensedParameters(IoParameters.createFromQuery(simpleParameterSet)).getItems());
        return result;
    }

    @Override
    public boolean exists(String id) {
        WarnCell warnCell = getWarnCell(id);
        if (warnCell != null) {
            return true;
        }
        return false;
    }

    private void checkForHref(PlatformOutput result, IoParameters parameters) {
        result.setHrefBase(urlHelper.getPlatformsHrefBaseUrl(parameters.getHrefBase()));
    }

}
