/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.web.v1.ctrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.n52.io.IoParameters;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.TimeseriesOutput;
import org.n52.sensorweb.v1.spi.ResultTimeService;

public class ResultTimeExtension implements MetadataExtension<TimeseriesMetadataOutput> {

    private static final String TYPE = "resultTime";

    private ResultTimeService resultTimeService;

    @Override
    public String getExtensionName() {
        return TYPE;
    }

    @Override
    public Map<String, Object> getData(IoParameters parameters,
            String timeseriesId) {
        Map<String, Object> data = new HashMap<String, Object>();
        if (parameters.getOther("request") != null && parameters.getOther("request").equals(TYPE)) {
            ArrayList<String> timestamps = resultTimeService.getResultTimeList(parameters, timeseriesId);
            data.put("resultTime", timestamps);
            return data;
        }
        return null;
    }

    @Override
    public void applyExtensionOn(TimeseriesMetadataOutput output) {
        if (enabled(output.getParameters())) {
            output.addExtra(getExtensionName());
        }
    }

    private boolean enabled(TimeseriesOutput timeseriesOutput) {
        return timeseriesOutput.getService().getId().equals("srv_5447754640fd3a7f04c094b54a9e9bf6"); // TODO zu config
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    public ResultTimeService getResultTimeService() {
        return resultTimeService;
    }

    public void setResultTimeService(ResultTimeService resultTimeService) {
        this.resultTimeService = resultTimeService;
    }

}
