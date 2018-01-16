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

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public abstract class ScheduledJob {

    private boolean enabled = true;

    private String jobName;

    private String triggerName;

    private String jobDescription;

    private String cronExpression;

    private boolean triggerAtStartup;

    // XXX job details create a job instance! snake biting tail
    public abstract JobDetail createJobDetails();

    public String getJobName() {
        return jobName == null || jobName.isEmpty()
                ? getClass().getSimpleName()
                : jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTriggerName() {
        return triggerName == null || triggerName.isEmpty()
                ? "trigger_" + getJobName()
                : triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpresssion) {
        this.cronExpression = cronExpresssion;
    }

    public boolean isTriggerAtStartup() {
        return triggerAtStartup;
    }

    public void setTriggerAtStartup(boolean triggerAtStartup) {
        this.triggerAtStartup = triggerAtStartup;
    }

    public Trigger createTrigger(JobKey jobKey) {
        TriggerBuilder<Trigger> tb = TriggerBuilder.newTrigger()
                .forJob(jobKey)
                .withIdentity(getTriggerName());
        if (getCronExpression() != null) {
            tb.withSchedule(CronScheduleBuilder.cronSchedule(getCronExpression()));
        }

        if (isTriggerAtStartup()) {
            tb.startAt(DateBuilder.futureDate(5, DateBuilder.IntervalUnit.SECOND));
        }
        return tb.build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
