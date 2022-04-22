/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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

import org.n52.faroe.annotation.Configurable;
import org.n52.faroe.annotation.Setting;
import org.n52.janmayen.lifecycle.Constructable;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Configurable
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class JobScheduler implements Constructable, JobUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);
    private static final String JOB_SCHEDULER_ENABLE_KEY = "helgoland.job.scheduler.enable";
    private static final String JOB_SCHEDULER_STARTUP_DELAY_KEY = "helgoland.job.scheduler.startup.delay";

    private List<ScheduledJob> scheduledJobs = new ArrayList<>();
    private Scheduler scheduler;
    // 5 seconds
    private int startupDelayInSeconds = 5;
    private boolean enabled = true;
    private boolean initialized;

    @Override
    public void init() {
        initialized = true;
        if (!enabled) {
            LOGGER.info("Job schedular disabled. No jobs will be triggered. "
                    + "This is also true for particularly enabled jobs.");
            return;
        }

        try {
            ReSchedulerJob reSchedulerJob = new ReSchedulerJob();
            JobDetail jobDetails = reSchedulerJob.createJobDetails();
            Trigger trigger = new ReSchedulerJob().createTrigger(jobDetails.getKey());
            if (getScheduler().getTriggersOfJob(trigger.getJobKey()).isEmpty()) {
                getScheduler().scheduleJob(jobDetails, trigger);
            } else {
                getScheduler().rescheduleJob(trigger.getKey(), trigger);
            }
        } catch (SchedulerException e) {
            LOGGER.error("Could not start re-scheduler job.", e);
        }

        try {
            getScheduler().startDelayed(startupDelayInSeconds);
            LOGGER.info("Scheduler will start jobs in {}s ...", startupDelayInSeconds);
        } catch (SchedulerException e) {
            LOGGER.error("Could not start scheduler.", e);
        }
    }

    /**
     * Shuts down the task scheduler without waiting tasks to be finished.
     */
    public void shutdown() {
        try {
            getScheduler().shutdown(false);
        } catch (SchedulerException e) {
            LOGGER.error("Could not shutdown scheduler.", e);
        }
    }

    public List<ScheduledJob> getScheduledJobs() {
        return scheduledJobs;
    }

    @Autowired
    public void setScheduledJobs(List<ScheduledJob> scheduledJobs) {
        this.scheduledJobs = scheduledJobs;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public int getStartupDelayInSeconds() {
        return startupDelayInSeconds;
    }

    @Setting(JOB_SCHEDULER_STARTUP_DELAY_KEY)
    public void setStartupDelayInSeconds(int startupDelayInSeconds) {
        this.startupDelayInSeconds = startupDelayInSeconds;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Setting(JOB_SCHEDULER_ENABLE_KEY)
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (initialized) {
                try {
                    if (enabled) {
                        init();
                    } else {
                        getScheduler().standby();
                    }
                } catch (SchedulerException e) {
                    LOGGER.error("Error while stopping/starting scheduler", e);
                }
            }
        }
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
