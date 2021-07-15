/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

public interface JobUtils {

    String JOB_DETAIL = "jobDetail";

    default void scheduleJob(ScheduledJob taskToSchedule, Scheduler scheduler) {
        try {
            JobDetail details = taskToSchedule.createJobDetails();
            scheduler.scheduleJob(details, taskToSchedule.createTrigger(details.getKey()));
            if (taskToSchedule.isTriggerAtStartup()) {
                getLogger().debug("Schedule job '{}' to run once at startup.", details.getKey());
                Trigger onceAtStartup = TriggerBuilder.newTrigger().withIdentity("onceAtStartup")
                        .forJob(details.getKey()).startNow().build();
                scheduler.scheduleJob(onceAtStartup);
            }
        } catch (SchedulerException e) {
            getLogger().warn("Could not schdule Job '{}'.", taskToSchedule.getJobName(), e);
        }
    }

    default void updateJob(ScheduledJob taskToSchedule, Scheduler scheduler) throws SchedulerException {
        JobDetail details = taskToSchedule.createJobDetails();
        Trigger trigger = taskToSchedule.createTrigger(details.getKey());
        Date nextExecution = scheduler.rescheduleJob(trigger.getKey(), trigger);
        getLogger().debug("Rescheduled job '{}' will be executed at '{}'!", details.getKey(),
                new DateTime(nextExecution));
        taskToSchedule.setModified(false);
    }

    default JobDetailFactoryBean createJobDetail(Class<? extends Job> jobClass, String jobName, String group) {
        getLogger().debug("createJobDetail(jobClass={}, jobName={}, groupName={})", jobClass.getName(), jobName,
                group);
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setName(jobName);
        factoryBean.setJobClass(jobClass);
        factoryBean.setGroup(group);
        // job has to be durable to be stored in DB:
        factoryBean.setDurability(true);
        factoryBean.afterPropertiesSet();
        return factoryBean;
    }

    default CronTriggerFactoryBean createCronTrigger(JobDetail jobDetail, String cronExpression, String triggerName) {
        getLogger().debug("createTrigger(jobDetail={}, cronExpression={}, triggerName={})", jobDetail.toString(),
                cronExpression, triggerName);
        // To fix an issue with time-based cron jobs
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setStartTime(calendar.getTime());
        factoryBean.setStartDelay(0L);
        factoryBean.setName(triggerName);
        factoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        return factoryBean;
    }

    default BeanCreationException createBeanCreationException(String expectedType, Object factory) {
        return new BeanCreationException(String.format("Could not create '%s' from '%s'",
                factory.getClass().getSimpleName(), factory.toString()));
    }

    Logger getLogger();

}
