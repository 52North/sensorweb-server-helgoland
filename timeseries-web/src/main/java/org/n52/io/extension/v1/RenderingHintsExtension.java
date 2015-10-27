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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.n52.io.response.v1.PhenomenonOutput;
import org.n52.io.request.StyleProperties;
import org.n52.io.response.v1.TimeseriesMetadataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import org.n52.io.extension.MetadataExtension;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;

public class RenderingHintsExtension implements MetadataExtension<TimeseriesMetadataOutput> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RenderingHintsExtension.class);
	
	private static final String CONFIG_FILE = "/config-rendering-hints.json";
	
    private static final String EXTENSION_NAME = "renderingHints";

	private final RenderingHintsExtensionConfig renderingConfig = readConfig();
    
	private RenderingHintsExtensionConfig readConfig() {
		try (InputStream config = getClass().getResourceAsStream(CONFIG_FILE);) {
			ObjectMapper om = new ObjectMapper();
			return om.readValue(config, RenderingHintsExtensionConfig.class);
		} catch (IOException e) {
			LOGGER.error("Could not load {}. Using empty config.", CONFIG_FILE);
			return new RenderingHintsExtensionConfig();
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
        Map<String, RenderingHintsExtensionConfig.ConfiguredStyle> timeseriesStyles = this.renderingConfig.getTimeseriesStyles();
        Map<String, RenderingHintsExtensionConfig.ConfiguredStyle> phenomenonStyles = this.renderingConfig.getPhenomenonStyles();
        if (timeseriesStyles.containsKey(timeseriesId)) {
            final StyleProperties style = createStyle(timeseriesStyles.get(timeseriesId));
            output.setRenderingHints(style); // stay backward compatible
            return style;
        } else if (phenomenonStyles.containsKey(phenomenon.getId())) {
            final StyleProperties style = createStyle(phenomenonStyles.get(phenomenon.getId()));
            output.setRenderingHints(style); // stay backward compatible
            return style;
        }
        return null;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    private StyleProperties createStyle(RenderingHintsExtensionConfig.ConfiguredStyle configuredStyle) {
        return configuredStyle.getStyle();
    }

}
