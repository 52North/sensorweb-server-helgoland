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
package org.n52.series.dwd.srv.v1;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.v1.PhenomenonOutput;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.series.dwd.rest.VorabInformationAlert;
import org.n52.series.dwd.rest.WarnungAlert;
import org.n52.series.dwd.store.AlertStore;
import org.n52.web.ctrl.v1.ext.ExtUrlSettings;
import org.n52.web.ctrl.v1.ext.UrlHelper;

public class PhenomenonOutputAdapter extends ParameterService<PhenomenonOutput> {

    private final AlertStore store;

    private final UrlHelper urlHelper = new UrlHelper();

    public PhenomenonOutputAdapter(AlertStore store) {
        this.store = store;
    }

    private OutputCollection<PhenomenonOutput> createOutputCollection() {
        return new OutputCollection<PhenomenonOutput>() {
            @Override
            protected Comparator<PhenomenonOutput> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    @Override
    public OutputCollection<PhenomenonOutput> getExpandedParameters(IoParameters query) {
        OutputCollection<PhenomenonOutput> outputCollection = createOutputCollection();
        for (String alertType : store.getAlertTypes()) {
            outputCollection.addItem(createExpanded(alertType, query));
        }
        return outputCollection;
    }

    @Override
    public OutputCollection<PhenomenonOutput> getCondensedParameters(IoParameters query) {
        OutputCollection<PhenomenonOutput> outputCollection = createOutputCollection();
        for (String phenomenon : getPhenonmenon()) {
            outputCollection.addItem(createCondensed(phenomenon, query));
        }
        return outputCollection;
    }

    private Set<String> getPhenonmenon() {
        Set<String> offerings = new HashSet<String>();
        if (store.getCurrentAlerts().hasWarning()) {
            Map<String, List<WarnungAlert>> warnings = store.getCurrentAlerts().getWarnings();
            for (List<WarnungAlert> list : warnings.values()) {
                for (WarnungAlert warnungAlert : list) {
                    offerings.add(warnungAlert.getEvent());
                }
            }
        }
        if (store.getCurrentAlerts().hasVorabInformation()) {
            Map<String, List<VorabInformationAlert>> vorabInformation = store.getCurrentAlerts().getVorabInformation();
            for (List<VorabInformationAlert> list : vorabInformation.values()) {
                for (VorabInformationAlert vorabInformationAlert : list) {
                    offerings.add(vorabInformationAlert.getEvent());
                }
            }
        }
        return offerings;
    }

    @Override
    public OutputCollection<PhenomenonOutput> getParameters(String[] items, IoParameters query) {
        OutputCollection<PhenomenonOutput> outputCollection = createOutputCollection();
        for (String alertType : items) {
            if (query.isExpanded()) {
                outputCollection.addItem(createExpanded(alertType, query));
            } else {
                outputCollection.addItem(createExpanded(alertType, query));
            }
        }
        return outputCollection;
    }

    @Override
    public PhenomenonOutput getParameter(String item, IoParameters query) {
        return createExpanded(item, query);
    }

    private PhenomenonOutput createCondensed(String item, IoParameters query) {
        PhenomenonOutput result = new PhenomenonOutput();
        result.setLabel(item);
        result.setId(item);
        result.setDomainId(item);
        checkForHref(result, query);
        return result;
    }

    private PhenomenonOutput createExpanded(String item, IoParameters query) {
        PhenomenonOutput result = createCondensed(item, query);
//        result.setService(getServiceOutput());
        return result;
    }

    @Override
    public boolean exists(String id) {
        return getPhenonmenon().contains(id);
    }

    private void checkForHref(PhenomenonOutput result, IoParameters parameters) {
        if (parameters.getHrefBase() != null && parameters.getHrefBase().contains(ExtUrlSettings.EXT)) {
            result.setHrefBase(urlHelper.getOfferingsHrefBaseUrl(parameters.getHrefBase()));
        }
    }

}
