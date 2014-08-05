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

import static org.n52.io.QueryParameters.createFromQuery;
import static org.n52.web.v1.ctrl.RestfulUrls.COLLECTION_TIMESERIES;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.n52.io.ConfigApplier;
import org.n52.io.IoParameters;
import org.n52.io.v1.data.ParameterOutput;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = COLLECTION_TIMESERIES)
public class TimeseriesMetadataController extends ParameterController {

    private List<ConfigApplier<TimeseriesMetadataOutput>> configAppliers = new ArrayList<ConfigApplier<TimeseriesMetadataOutput>>();

    private List<MetadataExtension<TimeseriesMetadataOutput>> metadataExtensions = new ArrayList<MetadataExtension<TimeseriesMetadataOutput>>();

    @RequestMapping(value = "/{item}/extras", method = GET)
    public Map<String, Object> getExtras(@PathVariable("item") String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters queryMap = createFromQuery(query);
        for (MetadataExtension<TimeseriesMetadataOutput> extension : metadataExtensions) {
            return extension.getData(queryMap, timeseriesId);
        }
        return null;
    }

    @Override
    protected ParameterOutput[] doPostProcessOn(ParameterOutput[] toBeProcessed) {

        for (ParameterOutput parameterOutput : toBeProcessed) {
            TimeseriesMetadataOutput output = (TimeseriesMetadataOutput) parameterOutput;
            for (ConfigApplier<TimeseriesMetadataOutput> applier : configAppliers) {
                applier.applyConfigOn(output);
            }
        }

        return toBeProcessed;
    }

    @Override
    protected ParameterOutput doPostProcessOn(ParameterOutput toBeProcessed) {

        TimeseriesMetadataOutput output = (TimeseriesMetadataOutput) toBeProcessed;
        for (ConfigApplier<TimeseriesMetadataOutput> applier : configAppliers) {
            applier.applyConfigOn(output);
        }

        for (MetadataExtension<TimeseriesMetadataOutput> extension : metadataExtensions) {
            extension.applyExtensionOn(output);
        }
        return toBeProcessed;
    }

    public List<ConfigApplier<TimeseriesMetadataOutput>> getConfigAppliers() {
        return configAppliers;
    }

    public void setConfigAppliers(List<ConfigApplier<TimeseriesMetadataOutput>> configAppliers) {
        this.configAppliers = configAppliers;
    }

    public List<MetadataExtension<TimeseriesMetadataOutput>> getMetadataExtensions() {
        return metadataExtensions;
    }

    public void setMetadataExtensions(List<MetadataExtension<TimeseriesMetadataOutput>> metadataExtensions) {
        this.metadataExtensions = metadataExtensions;
    }

}
