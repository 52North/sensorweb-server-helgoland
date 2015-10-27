/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.io.extension.v1;

import org.n52.io.*;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.n52.io.ConfigStatusIntervals.ConfigInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.io.v1.data.PhenomenonOutput;
import org.n52.io.v1.data.StatusInterval;
import org.n52.io.v1.data.TimeseriesMetadataOutput;

public class StatusIntervalsExtension implements MetadataExtension<TimeseriesMetadataOutput> {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(StatusIntervalsExtension.class);
	
	private static final String CONFIG_FILE = "/config-status-intervals.json";
    
    private static final String EXTENSION_NAME = "statusIntervals";

	private final ConfigStatusIntervals intervalConfig = readConfig();
	
	private ConfigStatusIntervals readConfig() {
		try (InputStream config = getClass().getResourceAsStream(CONFIG_FILE);) {
			ObjectMapper om = new ObjectMapper();
			return om.readValue(config, ConfigStatusIntervals.class);
		} catch (Exception e) {
			LOGGER.error("Could not load {). Using empty config.", CONFIG_FILE, e);
			return new ConfigStatusIntervals();
		}
	}

    @Override
    public void addExtensionTo(TimeseriesMetadataOutput output) {
        output.addExtra(EXTENSION_NAME);
    }

    @Override
    public Object getExtras(TimeseriesMetadataOutput output, IoParameters parameters) {
        String timeseriesId = output.getId();
        PhenomenonOutput phenomenon = output.getParameters().getPhenomenon();
        Map<String, ConfigInterval> timeseriesIntervals = this.intervalConfig.getTimeseriesIntervals();
        Map<String, ConfigInterval> phenomenaIntervals = this.intervalConfig.getPhenomenonIntervals();
        if (timeseriesIntervals.containsKey(timeseriesId)) {
            final StatusInterval[] intervals = createIntervals(timeseriesIntervals.get(timeseriesId));
            output.setStatusIntervals(intervals); // stay backwards compatible
            return intervals;
        } else if (phenomenaIntervals.containsKey(phenomenon.getId())) {
            final StatusInterval[] intervals = createIntervals(phenomenaIntervals.get(phenomenon.getId()));
            output.setStatusIntervals(intervals); // stay backwards compatible
            return intervals;
        }
        return null;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

	private StatusInterval[] createIntervals(ConfigInterval configInterval) {
		Map<String, StatusInterval> statusIntervals = configInterval.getStatusIntervals();
		for (Entry<String, StatusInterval> entry : statusIntervals.entrySet()) {
			StatusInterval value = entry.getValue();
			value.setName(entry.getKey());
		}
		return statusIntervals.values().toArray(new StatusInterval[0]);
	}

}
