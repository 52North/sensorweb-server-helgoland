/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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


import java.util.ArrayList;
import java.util.List;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

    private List<ScheduledJob> scheduledJobs = new ArrayList<>();

    // 5 seconds
    private int startupDelayInSeconds = 5;

    private Scheduler scheduler;

    private boolean enabled = true;

    /**
     * Runs all scheduled tasks
     */
    public void init() {
        if (!enabled) {
            LOGGER.info("Job schedular disabled. No jobs will be triggered. "
                    + "This is also true for particularly enabled jobs.");
            return;
        }

        scheduledJobs.stream()
                .filter(scheduledJob -> scheduledJob.isEnabled())
                .forEach(scheduledJob -> scheduleJob(scheduledJob));

        try {
            scheduler.startDelayed(startupDelayInSeconds);
            LOGGER.info("Scheduler will start jobs in {}s ...", startupDelayInSeconds);
        } catch (SchedulerException e) {
            LOGGER.error("Could not start scheduler.", e);
        }
    }

    private void scheduleJob(ScheduledJob taskToSchedule) {
        try {
            JobDetail details = taskToSchedule.createJobDetails();
            Trigger trigger = taskToSchedule.createTrigger(details.getKey());
            scheduler.scheduleJob(details, trigger);
            if (taskToSchedule.isTriggerAtStartup()) {
                LOGGER.debug("Schedule job '{}' to run once at startup.", details.getKey());
                Trigger onceAtStartup = TriggerBuilder.newTrigger()
                        .withIdentity("onceAtStartup")
                        .forJob(details.getKey()).build();
                scheduler.scheduleJob(onceAtStartup);
            }
        } catch (SchedulerException e) {
            LOGGER.warn("Could not schdule Job '{}'.", taskToSchedule.getJobName(), e);
        }
    }

    /**
     * Shuts down the task scheduler without waiting tasks to be finished.
     */
    public void shutdown() {
        try {
            scheduler.shutdown(false);
        } catch (SchedulerException e) {
            LOGGER.error("Could not shutdown scheduler.", e);
        }
    }

    public List<ScheduledJob> getScheduledJobs() {
        return scheduledJobs;
    }

    public void setScheduledJobs(List<ScheduledJob> scheduledJobs) {
        this.scheduledJobs = scheduledJobs;
    }

    public int getStartupDelayInSeconds() {
        return startupDelayInSeconds;
    }

    public void setStartupDelayInSeconds(int startupDelayInSeconds) {
        this.startupDelayInSeconds = startupDelayInSeconds;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
