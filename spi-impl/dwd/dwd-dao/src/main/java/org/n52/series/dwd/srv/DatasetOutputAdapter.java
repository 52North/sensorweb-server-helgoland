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

import java.util.Arrays;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetType;
import org.n52.io.response.dataset.SeriesParameters;
import org.n52.io.response.dataset.dwd.DwdAlert;
import org.n52.io.response.dataset.dwd.DwdAlertDatasetOutput;
import org.n52.io.response.dataset.dwd.DwdAlertValue;
import org.n52.series.dwd.beans.ServiceInfo;
import org.n52.series.dwd.beans.WarnCell;
import org.n52.series.dwd.rest.Alert;
import org.n52.series.dwd.rest.AlertCollection;
import org.n52.series.dwd.rest.VorabInformationAlert;
import org.n52.series.dwd.rest.WarnungAlert;
import org.n52.series.dwd.store.AlertStore;
import org.n52.series.spi.srv.DataService;
import org.n52.web.ctrl.UrlHelper;

public class DatasetOutputAdapter extends AbstractOuputAdapter<DatasetOutput> implements DataService<Data<? extends AbstractValue<?>>> {

    private final AlertStore store;

    private final UrlHelper urlHelper = new UrlHelper();

    public DatasetOutputAdapter(AlertStore store, ServiceInfo serviceInfo) {
        super(serviceInfo);
        this.store = store;
    }

    @Override
    public OutputCollection<DatasetOutput> getExpandedParameters(IoParameters query) {
        OutputCollection<DatasetOutput> outputCollection = createOutputCollection();
        for (WarnCell warnCell : getFilteredWarnCells(query, store)) {
            for (Alert alert : getFilteredAlerts(warnCell, query, store)) {
                outputCollection.addItem(createExpanded(alert, warnCell, query));
            }
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<DatasetOutput> getCondensedParameters(IoParameters query) {
        OutputCollection<DatasetOutput> outputCollection = createOutputCollection();
        for (WarnCell warnCell : getFilteredWarnCells(query, store)) {
            for (Alert alert : getFilteredAlerts(warnCell, query, store)) {
                outputCollection.addItem(createCondensed(alert, warnCell, query));
            }
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<DatasetOutput> getParameters(String[] items, IoParameters query) {
        // TODO Auto-generated method stub
        return createOutputCollection();
    }

    @Override
    public DatasetOutput getParameter(String item, IoParameters query) {
        return createExpanded( getAlert(item), getWarnCell(item), query);
    }

    @Override
    public boolean exists(String id, IoParameters parameters) {
        return getAlert(id) != null ? true : false;
    }

    private Alert getAlert(String id) {
        List<String> parseId = parseId(DatasetType.extractId(id));
        AlertCollection currentAlerts = store.getCurrentAlerts();
        if (currentAlerts.hasWarning() && currentAlerts.getWarnings().containsKey(parseId.get(0))) {
            for (WarnungAlert alert : currentAlerts.getWarnings().get(parseId.get(0))) {
                if (alert.getEvent().equals(parseId.get(1))) {
                    return alert;
                }
            }
        }
        if (currentAlerts.hasVorabInformation() && currentAlerts.getVorabInformation().containsKey(parseId.get(0))) {
            for (VorabInformationAlert alert : currentAlerts.getVorabInformation().get(parseId.get(0))) {
                if (alert.getEvent().equals(parseId.get(1))) {
                    return alert;
                }
            }
        }
        return null;
    }

    private WarnCell getWarnCell(String id) {
        List<String> parseId = parseId(DatasetType.extractId(id));
        return super.getWarnCell(parseId.get(0), store);
    }

    private DwdAlertDatasetOutput createCondensed(Alert alert, WarnCell warnCell, IoParameters query) {
        DwdAlertDatasetOutput result = new DwdAlertDatasetOutput();
        result.setLabel(createSeriesLabel(alert));
        result.setId(createId(warnCell.getId(), alert.getEvent()));
        checkForHref(result, query);
        return result;
    }

    private DatasetOutput createExpanded(Alert alert, WarnCell warnCell, IoParameters query) {
        DwdAlertDatasetOutput result = createCondensed(alert, warnCell, query);
        result.setSeriesParameters(getSeriesParameters(alert, warnCell, query));
        DwdAlertValue value = createValue(alert);
        result.setFirstValue(value);
        result.setLastValue(value);
        return result;
    }

    private DwdAlertValue createValue(Alert alert) {
        DwdAlertValue value = new DwdAlertValue();
        value.setTimestamp(store.getLastKnownAlertTime().getMillis());
        value.setValue(createAlertValue(alert));
        return value;
    }

    private DwdAlert createAlertValue(Alert alert) {
        DwdAlert value = new DwdAlert();
        value.setAltitudeEnd(alert.getAltitudeEnd());
        value.setAltitudeStart(alert.getAltitudeStart());
        value.setDescription(alert.getDescription());
        value.setInstructions(alert.getInstruction());
        value.setLevel(alert.getLevel());
        value.setState(alert.getState());
        value.setStateShort(alert.getStateShort());
        value.setType(alert.getType());
        value.setValidFrom(alert.getStart());
        value.setValidUntil(alert.getEnd());
        value.setWarning(alert.getHeadline());
        return value;
    }

    private SeriesParameters getSeriesParameters(Alert alert, WarnCell warnCell, IoParameters query) {
        SeriesParameters seriesParameters = new SeriesParameters();
        seriesParameters.setCategory(createCondensedCategory(alert.getEvent(), query));
        seriesParameters.setFeature(createCondensedFeature(warnCell, alert, query));
        seriesParameters.setOffering(createCondensedOffering(alert.getType(), query));
        seriesParameters.setPhenomenon(createCondensedPhenomenon(alert.getEvent(), query));
        seriesParameters.setPlatform(createCondensedPlatform(warnCell, alert, query));
        seriesParameters.setProcedure(createCondensedProcedure(alert.getAlertType(), query));
        seriesParameters.setService(getServiceOutput());
        return seriesParameters;
    }

    private String createSeriesLabel(Alert alert) {
        StringBuilder sb = new StringBuilder();
        sb.append(alert.getEvent()).append(", ");
        sb.append(alert.getAlertType()).append(", ");
        return sb.append(alert.getRegionName()).toString();
    }

    private void checkForHref(DatasetOutput result, IoParameters parameters) {
        result.setHrefBase(urlHelper.getDatasetsHrefBaseUrl(parameters.getHrefBase()));
    }


    protected String createId(String warnCell, String phenomenon) {
        return warnCell + "-" + createPhenomenonId(phenomenon);
    }

    protected List<String> parseId(String id) {
        String[] split = id.split("-");
        split[1] = parsePhenomenonId(split[1]);
        return Arrays.asList(split);
    }

    @Override
    public DataCollection<Data<? extends AbstractValue<?>>> getData(RequestParameterSet parameters) {
        return null;
    }

}
