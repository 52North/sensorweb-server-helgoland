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
package org.n52.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.n52.io.ConfigRenderingHints.ConfiguredStyle;
import org.n52.io.v1.data.PhenomenonOutput;
import org.n52.io.v1.data.StyleProperties;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RenderingHintsConfigApplier extends ConfigApplier<TimeseriesMetadataOutput> {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(RenderingHintsConfigApplier.class);
	
	private static final String CONFIG_FILE = "/config-rendering-hints.json";
	
	private ConfigRenderingHints configRendering;

	public RenderingHintsConfigApplier() {
		this.configRendering = readConfig();
	}
	
	private ConfigRenderingHints readConfig() {
		InputStream config = getClass().getResourceAsStream(CONFIG_FILE);
		try {
			ObjectMapper om = new ObjectMapper();
			return om.readValue(config, ConfigRenderingHints.class);
		} catch (IOException e) {
			LOGGER.error("Could not load {}. Using empty config.", CONFIG_FILE);
			return new ConfigRenderingHints();
		}
		finally {
			if(config != null) {
				try {
					config.close();
				} catch (IOException e) {
					LOGGER.debug("Stream already closed.");
				}
			}
		}
	}
	
    @Override
    public void applyConfigOn(TimeseriesMetadataOutput toApplyConfigOn) {
        String timeseriesId = toApplyConfigOn.getId();
        PhenomenonOutput phenomenon = toApplyConfigOn.getParameters().getPhenomenon();
        Map<String, ConfiguredStyle> timeseriesStyles = this.configRendering.getTimeseriesStyles();
        Map<String, ConfiguredStyle> phenomenonStyles = this.configRendering.getPhenomenonStyles();
        if (timeseriesStyles.containsKey(timeseriesId)) {
            toApplyConfigOn.setRenderingHints(createStyle(timeseriesStyles.get(timeseriesId)));
        } else if (phenomenonStyles.containsKey(phenomenon.getId())) {
            toApplyConfigOn.setRenderingHints(createStyle(phenomenonStyles.get(phenomenon.getId())));
        }
    }

    private StyleProperties createStyle(ConfiguredStyle configuredStyle) {
        return configuredStyle.getStyle();
    }

}
