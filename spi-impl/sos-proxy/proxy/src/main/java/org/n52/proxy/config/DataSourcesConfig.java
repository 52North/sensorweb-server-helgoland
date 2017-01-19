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
package org.n52.proxy.config;

import java.util.ArrayList;
import java.util.List;

public class DataSourcesConfig {

    private List<DataSourceConfig> dataSources = new ArrayList<>();

    public List<DataSourceConfig> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<DataSourceConfig> dataSources) {
        this.dataSources = dataSources;
    }

    public static class DataSourceConfig {

        private String itemName;
        private String url;
        private String version;
        private DataSourceJobConfig job;

        public DataSourceJobConfig getJob() {
            return job;
        }

        public void setJob(DataSourceJobConfig job) {
            this.job = job;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class DataSourceJobConfig {

        private String cronExpression;
        private boolean enabled;
        private boolean triggerAtStartup;

        public String getCronExpression() {
            return cronExpression;
        }

        public void setCronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isTriggerAtStartup() {
            return triggerAtStartup;
        }

        public void setTriggerAtStartup(boolean triggerAtStartup) {
            this.triggerAtStartup = triggerAtStartup;
        }
    }
}
