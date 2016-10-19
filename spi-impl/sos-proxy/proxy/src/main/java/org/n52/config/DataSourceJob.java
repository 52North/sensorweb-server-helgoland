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
package org.n52.config;

import org.n52.config.DataSourcesConfig.DataSourceConfig;
import org.n52.io.task.ScheduledJob;
import org.quartz.InterruptableJob;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceJob extends ScheduledJob implements InterruptableJob {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataSourceJob.class);

    private boolean interrupted;

    private DataSourceConfig config;

    public DataSourceConfig getConfig() {
        return config;
    }

    public void setConfig(DataSourceConfig config) {
        this.config = config;
    }

    @Override
    public JobDetail createJobDetails() {
        return JobBuilder.newJob(DataSourceJob.class)
                .withIdentity(getJobName())
                .usingJobData("url", config.getUrl())
                .build();
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted = true;
        LOGGER.info(getJobName() + " is interrupted.");
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (interrupted) {
            return;
        }
        LOGGER.info(context.getJobDetail().getKey() + " is executed.");

        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        String url = jobDataMap.getString("url");

        LOGGER.info(url);
    }

    public void init(DataSourceConfig config) {
        setConfig(config);
        setJobName(config.getItemName());
        if (config.getJob() != null) {
            DataSourcesConfig.DataSourceJobConfig job = config.getJob();
            setEnabled(job.isEnabled());
            setCronExpression(job.getCronExpression());
            setTriggerAtStartup(job.isTriggerAtStartup());
        }
    }

}
