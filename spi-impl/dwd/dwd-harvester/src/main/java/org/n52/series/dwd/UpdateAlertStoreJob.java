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
package org.n52.series.dwd;

import org.n52.io.task.ScheduledJob;
import org.n52.web.common.Stopwatch;
import org.quartz.InterruptableJob;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class UpdateAlertStoreJob extends ScheduledJob implements InterruptableJob {

    private final static Logger LOGGER = LoggerFactory.getLogger(UpdateAlertStoreJob.class);

    @Autowired
    @Qualifier("harvester")
    private DwdHarvester harvester;

    @Autowired
    @Qualifier("geometryHarvester")
    private GeometryHarvester geometryHarvester;

    private boolean interrupted;

    @Override
    public JobDetail createJobDetails() {
        return JobBuilder.newJob(UpdateAlertStoreJob.class).withIdentity(getJobName()).withDescription(getJobDescription()).build();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (interrupted) {
            return;
        }

        LOGGER.info("Start update task");
        final Stopwatch stopwatch = Stopwatch.startStopwatch();
        final JobDetail details = context.getJobDetail();
        JobDataMap jobDataMap = details.getJobDataMap();
        getHarvester().harvest();
        getGeometryHarvester().harvest();
        LOGGER.debug("update took '{}'", stopwatch.stopInSeconds());
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted = true;
        LOGGER.info("Marked job to interrupt.");
    }

    public DwdHarvester getHarvester() {
        return harvester;
    }

    public void setHarvester(DwdHarvester harvester) {
        this.harvester = harvester;
    }

    public GeometryHarvester getGeometryHarvester() {
        return geometryHarvester;
    }

    public void setGeometryHarvester(GeometryHarvester geometryHarvester) {
        this.geometryHarvester = geometryHarvester;
    }

}
