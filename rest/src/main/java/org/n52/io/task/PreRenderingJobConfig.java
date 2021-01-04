/*
 * Copyright (C) 2013-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.io.task;

import org.n52.faroe.annotation.Configurable;
import org.n52.faroe.annotation.Setting;

@Configurable
public class PreRenderingJobConfig {

    protected static final String PRERENDERING_JOB_ENABLE_KEY = "helgoland.job.prerendering.enable";
    protected static final String PRERENDERING_JOB_CONFIG_FILE_KEY = "helgoland.job.prerendering.config.file";
    protected static final String PRERENDERING_JOB_TRIGGER_STARTUP_KEY = "helgoland.job.prerendering.trigger.startup";
    protected static final String PRERENDERING_JOB_CRON_EXPRESSION_KEY = "helgoland.job.prerendering.cron.expression";

    private boolean enabled;
    private String configFile;
    private boolean triggerAtStartup;
    private String cronExpression;
    private boolean modified;

    @Setting(PRERENDERING_JOB_ENABLE_KEY)
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
        }
    }

    @Setting(PRERENDERING_JOB_CONFIG_FILE_KEY)
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    @Setting(PRERENDERING_JOB_TRIGGER_STARTUP_KEY)
    public void setTriggerAtStartup(boolean triggerAtStartup) {
        this.triggerAtStartup = triggerAtStartup;
    }

    @Setting(PRERENDERING_JOB_CRON_EXPRESSION_KEY)
    public void setCronExpression(String cronExpression) {
        if (this.cronExpression != null && !this.cronExpression.equals(cronExpression)) {
            this.modified = true;
        }
        this.cronExpression = cronExpression;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getConfigFile() {
        return configFile;
    }

    public boolean isTriggerAtStartup() {
        return triggerAtStartup;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public boolean isModified() {
        return modified;
    }
}
