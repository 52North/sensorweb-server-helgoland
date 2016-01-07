/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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

import org.n52.io.request.StyleProperties;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.n52.io.response.ext.MetadataExtension;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v1.SeriesMetadataV1Output;

public class RenderingHintsExtension extends MetadataExtension<SeriesMetadataV1Output> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RenderingHintsExtension.class);
	
	private static final String CONFIG_FILE = "/config-rendering-hints.json";
	
    private static final String EXTENSION_NAME = "renderingHints";

	private final RenderingHintsExtensionConfig renderingConfig = readConfig();

	private RenderingHintsExtensionConfig readConfig() {
		try (InputStream config = getClass().getResourceAsStream(CONFIG_FILE);) {
			ObjectMapper om = new ObjectMapper();
			return om.readValue(config, RenderingHintsExtensionConfig.class);
		} catch (IOException e) {
			LOGGER.error("Could not load {}. Using empty config.", CONFIG_FILE, e);
			return new RenderingHintsExtensionConfig();
		}
	}

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }
    
    @Override
    public void addExtraMetadataFieldNames(SeriesMetadataV1Output output) {
        if (hasRenderingHints(output)) {
            output.addExtra(EXTENSION_NAME);
        }
    }
    
    private boolean hasRenderingHints(SeriesMetadataV1Output output) {
        return hasSeriesConfiguration(output) || hasPhenomenonConfiguration(output);
    }

    private boolean hasSeriesConfiguration(SeriesMetadataV1Output output) {
        return hasSeriesConfiguration(output) || hasPhenomenonConfiguration(output);
    }

    private boolean hasPhenomenonConfiguration(SeriesMetadataV1Output output) {
        String id = output.getParameters().getPhenomenon().getId();
        return renderingConfig.getPhenomenonStyles().containsKey(id);
    }

    @Override
    public Map<String, Object> getExtras(SeriesMetadataV1Output output, IoParameters parameters) {
        if ( !hasExtrasToReturn(output, parameters)) {
            return Collections.emptyMap();
        }
        
        if (hasSeriesConfiguration(output)) {
            final StyleProperties style = createStyle(getSeriesStyle(output));
            output.setRenderingHints(style); // stay backward compatible
            return wrapSingleIntoMap(style);
        } else if (hasPhenomenonConfiguration(output)) {
            final StyleProperties style = createStyle(getPhenomenonStyle(output));
            output.setRenderingHints(style); // stay backward compatible
            return wrapSingleIntoMap(style);
        }
        
        LOGGER.error("No rendering style found for {} (id={})", output, output.getId());
        return Collections.emptyMap();
    }

    private boolean hasExtrasToReturn(SeriesMetadataV1Output output, IoParameters parameters) {
        return super.hasExtrasToReturn(output, parameters)
                && hasRenderingHints(output);
    }
    
    private RenderingHintsExtensionConfig.ConfiguredStyle getSeriesStyle(TimeseriesMetadataOutput output) {
        return renderingConfig.getTimeseriesStyles().get(output.getId());
    }
    
    private RenderingHintsExtensionConfig.ConfiguredStyle getPhenomenonStyle(SeriesMetadataV1Output output) {
        String id = output.getParameters().getPhenomenon().getId();
        return renderingConfig.getPhenomenonStyles().get(id);
    }

    private StyleProperties createStyle(RenderingHintsExtensionConfig.ConfiguredStyle configuredStyle) {
        return configuredStyle.getStyle();
    }

}
