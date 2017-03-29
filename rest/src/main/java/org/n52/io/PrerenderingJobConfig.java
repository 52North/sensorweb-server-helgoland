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
package org.n52.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.io.request.StyleProperties;

public class PrerenderingJobConfig {

    private Map<String, String> generalConfig = new HashMap<>();

    private List<RenderingConfig> phenomenonStyles = new ArrayList<>();

    private List<RenderingConfig> seriesStyles = new ArrayList<>();

    public Map<String, String> getGeneralConfig() {
        return generalConfig;
    }

    public void setGeneralConfig(Map<String, String> generalConfig) {
        this.generalConfig = generalConfig;
    }

    public List<RenderingConfig> getPhenomenonStyles() {
        return phenomenonStyles;
    }

    public void setPhenomenonStyles(List<RenderingConfig> phenomenonStyles) {
        this.phenomenonStyles = phenomenonStyles;
    }

    // keep for backwards compatibility
    @Deprecated
    public List<RenderingConfig> getTimeseriesStyles() {
        return seriesStyles;
    }

    // keep for backwards compatibility
    @Deprecated
    public void setTimeseriesStyles(List<RenderingConfig> timeseriesStyles) {
        this.seriesStyles = timeseriesStyles;
    }

    public List<RenderingConfig> getSeriesStyles() {
        return seriesStyles;
    }

    public void setSeriesStyles(List<RenderingConfig> seriesStyles) {
        this.seriesStyles = seriesStyles;
    }

    public static class RenderingConfig {

        private String id;

        private String title;

        private String chartQualifier;

        private String[] interval;

        private StyleProperties style;

        private Map<String, String> config;

        private Map<String, StyleProperties> referenceValueStyleProperties = new HashMap<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getChartQualifier() {
            return chartQualifier;
        }

        public void setChartQualifier(String chartQualifier) {
            this.chartQualifier = chartQualifier;
        }

        public String[] getInterval() {
            return interval;
        }

        public void setInterval(String[] interval) {
            this.interval = interval.clone();
        }

        public StyleProperties getStyle() {
            return style;
        }

        public void setStyle(StyleProperties style) {
            this.style = style;
        }

        public Map<String, String> getConfig() {
            return config;
        }

        public void setConfig(Map<String, String> config) {
            this.config = config;
        }

        public Map<String, StyleProperties> getReferenceValueStyleProperties() {
            return referenceValueStyleProperties;
        }

        public void setReferenceValueStyleProperties(Map<String, StyleProperties> referenceValueStyleProperties) {
            this.referenceValueStyleProperties = referenceValueStyleProperties;
        }

    }

}
