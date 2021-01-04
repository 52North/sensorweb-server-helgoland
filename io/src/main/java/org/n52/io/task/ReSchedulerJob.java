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

import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ReSchedulerJob extends ScheduledJob implements InterruptableJob, JobUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReSchedulerJob.class);
    private static final String JOBS = "jobs";
    private static final String SCHEDULER = "scheduler";

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private List<ScheduledJob> scheduledJobs;

    public Trigger createTrigger() {
        return createTrigger(createJobDetails().getKey());
    }

    @Override
    public Trigger createTrigger(JobKey jobKey) {
        return TriggerBuilder.newTrigger().forJob(jobKey).withIdentity("reSchedulerJobTrigger").startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0 */1 * ? * *")).build();
    }

    @Override
    public JobDetail createJobDetails() {
         JobDetail details = JobBuilder.newJob(ReSchedulerJob.class)
                .withIdentity("reSchedulerJob")
                .build();
         return details;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        for (ScheduledJob scheduledJob : getJobs(context)) {
            JobDetail jobDetails = scheduledJob.createJobDetails();
            Trigger trigger = scheduledJob.createTrigger(jobDetails.getKey());
            try {
                if (scheduler != null && !scheduler.checkExists(jobDetails.getKey())
                        && !scheduler.checkExists(trigger.getKey())) {
                    if (scheduledJob.isEnabled()) {
                        scheduleJob(scheduledJob, scheduler);
                    }
                } else {
                    if (scheduler != null && !scheduledJob.isEnabled()) {
                        scheduler.deleteJob(jobDetails.getKey());
                    } else if (scheduledJob.isModified()) {
                        updateJob(scheduledJob, scheduler);
                    }
                }
            } catch (SchedulerException e) {
                LOGGER.error("Error while processing trigger {} of job {}", trigger.getKey().getName(),
                        jobDetails.getKey().getName());

            }
        }
    }

    private List<ScheduledJob> getJobs(JobExecutionContext context) {
        return context.getJobDetail().getJobDataMap().containsKey(JOBS)
                ? (List<ScheduledJob>) context.getJobDetail().getJobDataMap().get(JOBS)
                : scheduledJobs;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        LOGGER.info("Marked job to interrupt.");
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}

