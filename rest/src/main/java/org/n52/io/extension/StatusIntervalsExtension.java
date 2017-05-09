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
package org.n52.io.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.n52.io.extension.StatusIntervalsExtensionConfig.ConfigInterval;
import org.n52.io.request.IoParameters;
import org.n52.io.response.StatusInterval;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.TimeseriesMetadataOutput;
import org.n52.io.response.extension.MetadataExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusIntervalsExtension extends MetadataExtension<DatasetOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusIntervalsExtension.class);

    private static final String CONFIG_FILE = "/config-status-intervals.json";

    private static final String EXTENSION_NAME = "statusIntervals";

    private final StatusIntervalsExtensionConfig intervalConfig = readConfig();

    private StatusIntervalsExtensionConfig readConfig() {
        try (InputStream config = getClass().getResourceAsStream(CONFIG_FILE);) {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(config, StatusIntervalsExtensionConfig.class);
        } catch (Exception e) {
            LOGGER.error("Could not load {). Using empty config.", CONFIG_FILE, e);
            return new StatusIntervalsExtensionConfig();
        }
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public void addExtraMetadataFieldNames(DatasetOutput output) {
        if (hasStatusIntervals(output)) {
            output.addExtra(EXTENSION_NAME);
        }
    }

    private boolean hasStatusIntervals(DatasetOutput output) {
        return hasSeriesConfiguration(output) || hasPhenomenonConfiguration(output);
    }

    private boolean hasSeriesConfiguration(DatasetOutput output) {
        String id = output.getId();
        return intervalConfig.getTimeseriesIntervals().containsKey(id);
    }

    private boolean hasPhenomenonConfiguration(DatasetOutput output) {
        String id = output.getId();
        return intervalConfig.getPhenomenonIntervals().containsKey(id);
    }

    @Override
    public Map<String, Object> getExtras(DatasetOutput output, IoParameters parameters) {
        if (!hasExtrasToReturn(output, parameters)) {
            return Collections.emptyMap();
        }

        if (hasSeriesConfiguration(output)) {
            final StatusInterval[] intervals = createIntervals(getSeriesIntervals(output));
            checkForBackwardCompatiblity(output, intervals);
            return wrapSingleIntoMap(intervals);
        } else if (hasPhenomenonConfiguration(output)) {
            final StatusInterval[] intervals = createIntervals(getPhenomenonIntervals(output));
            checkForBackwardCompatiblity(output, intervals);
            return wrapSingleIntoMap(intervals);
        }
        LOGGER.error("No status intervals found for {} (id={})", output, output.getId());
        return Collections.emptyMap();
    }

    private boolean hasExtrasToReturn(DatasetOutput output, IoParameters parameters) {
        return super.hasExtrasToReturn(output, parameters)
                && hasStatusIntervals(output);
    }

    private StatusIntervalsExtensionConfig.ConfigInterval getSeriesIntervals(DatasetOutput output) {
        return intervalConfig.getTimeseriesIntervals().get(output.getId());
    }

    private StatusIntervalsExtensionConfig.ConfigInterval getPhenomenonIntervals(DatasetOutput output) {
        String id = output.getSeriesParameters().getPhenomenon().getId();
        return intervalConfig.getPhenomenonIntervals().get(id);
    }

    private StatusInterval[] createIntervals(ConfigInterval configInterval) {
        Map<String, StatusInterval> statusIntervals = configInterval.getStatusIntervals();
        for (Entry<String, StatusInterval> entry : statusIntervals.entrySet()) {
            StatusInterval value = entry.getValue();
            value.setName(entry.getKey());
        }
        return statusIntervals.values().toArray(new StatusInterval[0]);
    }

    private void checkForBackwardCompatiblity(DatasetOutput output, StatusInterval[] intervals) {
        if (output instanceof TimeseriesMetadataOutput) {
            ((TimeseriesMetadataOutput) output).setStatusIntervals(intervals);
        }

    }

}
