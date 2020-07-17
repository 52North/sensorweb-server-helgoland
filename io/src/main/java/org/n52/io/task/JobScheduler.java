/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.n52.faroe.annotation.Configurable;
import org.n52.faroe.annotation.Setting;
import org.n52.janmayen.lifecycle.Constructable;
import org.quartz.InterruptableJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configurable
public class JobScheduler implements Constructable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);
    private static final String JOB_SCHEDULER_ENABLE_KEY = "helgoland.job.scheduler.enable";
    private static final String JOB_SCHEDULER_STARTUP_DELAY_KEY = "helgoland.job.scheduler.startup.delay";

    private List<ScheduledJob> scheduledJobs = new ArrayList<>();

    // 5 seconds
    private int startupDelayInSeconds = 5;

    private Scheduler scheduler;

    private boolean enabled = true;
    private boolean initialized;

    /**
     * Runs all scheduled tasks
     */
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
            if (!scheduler.checkExists(reSchedulerJob.createJobDetails().getKey())) {
                scheduleJob(new ReSchedulerJob());
            }
        } catch (SchedulerException e) {
            LOGGER.error("Could not start re-scheduler job.", e);
        }

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

    @Setting(JOB_SCHEDULER_STARTUP_DELAY_KEY)
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

    @Setting(JOB_SCHEDULER_ENABLE_KEY)
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (initialized) {
                try {
                    if (enabled) {
                        init();
                    } else {
                        scheduler.standby();
                    }
                } catch (SchedulerException e) {
                    LOGGER.error("Error while stopping/starting scheduler", e);
                }
            }
        }
    }

    public class ReSchedulerJob extends ScheduledJob implements InterruptableJob {

        @Override
        public Trigger createTrigger(JobKey jobKey) {
            TriggerBuilder<Trigger> tb =
                    TriggerBuilder.newTrigger().forJob(jobKey).withIdentity(getTriggerName()).startNow();
            if (getCronExpression() != null) {
                tb.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(30));
            }
            return tb.build();
        }

        @Override
        public JobDetail createJobDetails() {
            return JobBuilder.newJob(ReSchedulerJob.class)
                    .withIdentity(getJobName())
                    .build();
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            for (ScheduledJob scheduledJob : JobScheduler.this.scheduledJobs) {
                JobDetail jobDetails = scheduledJob.createJobDetails();
                Trigger trigger = scheduledJob.createTrigger(jobDetails.getKey());
                try {
                    if (!scheduler.checkExists(jobDetails.getKey()) && !scheduler.checkExists(trigger.getKey())) {
                        if (scheduledJob.isEnabled()) {
                            scheduleJob(scheduledJob);
                        }
                    } else {
                        if (!scheduledJob.isEnabled()) {
                            scheduler.deleteJob(jobDetails.getKey());
                        } else if (scheduledJob.isModified()) {
                            updateJob(scheduledJob);
                        }
                    }
                } catch (SchedulerException e) {
                    LOGGER.error("Error while processing trigger {} of job {}", trigger.getKey().getName(),
                            jobDetails.getKey().getName());

                }
            }
        }

        public void updateJob(ScheduledJob taskToSchedule) throws SchedulerException {
            JobDetail details = taskToSchedule.createJobDetails();
            Trigger trigger = taskToSchedule.createTrigger(details.getKey());
            Date nextExecution = scheduler.rescheduleJob(trigger.getKey(), trigger);
            LOGGER.debug("Rescheduled job '{}' will be executed at '{}'!", details.getKey(),
                    new DateTime(nextExecution));
            taskToSchedule.setModified(false);
        }

        @Override
        public void interrupt() throws UnableToInterruptJobException {
            LOGGER.info("Marked job to interrupt.");
        }
    }
}
