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
package org.n52.proxy.harvest;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.io.task.ScheduledJob;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.config.DataSourceJobConfiguration;
import org.n52.proxy.connector.AbstractSosConnector;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.db.beans.ProxyServiceEntity;
import org.n52.proxy.db.da.InsertRepository;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.util.CodingHelper;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class DataSourceHarvesterJob extends ScheduledJob implements Job {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataSourceHarvesterJob.class);

    private DataSourceConfiguration config;

    private static final String JOB_CONNECTOR = "connector";
    private static final String JOB_VERSION = "version";
    private static final String JOB_NAME = "name";
    private static final String JOB_URL = "url";

    @Autowired
    private InsertRepository insertRepository;

    @Autowired
    private DecoderRepository decoderRepository;

    @Autowired
    private Set<AbstractSosConnector> connectors;

    public DataSourceConfiguration getConfig() {
        return config;
    }

    public void setConfig(DataSourceConfiguration config) {
        this.config = config;
    }

    @Override
    public JobDetail createJobDetails() {
        return JobBuilder.newJob(DataSourceHarvesterJob.class)
                .withIdentity(getJobName())
                .usingJobData(JOB_URL, config.getUrl())
                .usingJobData(JOB_NAME, config.getItemName())
                .usingJobData(JOB_VERSION, config.getVersion())
                .usingJobData(JOB_CONNECTOR, config.getConnector())
                .build();
    }

    private DataSourceConfiguration recreateConfig(JobDataMap jobDataMap) {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setUrl(jobDataMap.getString(JOB_URL));
        config.setItemName(jobDataMap.getString(JOB_NAME));
        config.setVersion(jobDataMap.getString(JOB_VERSION));
        config.setConnector(jobDataMap.getString(JOB_CONNECTOR));
        return config;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info(context.getJobDetail().getKey() + " execution starts.");

        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        DataSourceConfiguration dataSource = recreateConfig(jobDataMap);
        GetCapabilitiesResponse capabilities = getCapabilities(dataSource);

        ServiceConstellation constellation = null;
        for (AbstractSosConnector connector : connectors) {
            if (connector.matches(dataSource, capabilities)) {
                LOGGER.info(connector.toString() + " create a constellation for " + dataSource);
                constellation = connector.getConstellation(dataSource, capabilities);
                break;
            }
        }
        if (constellation == null) {
            LOGGER.warn("No connector found for " + dataSource);
        } else {
            saveConstellation(constellation);
        }

        LOGGER.info(context.getJobDetail().getKey() + " execution ends.");
    }

    public void init(DataSourceConfiguration config) {
        setConfig(config);
        setJobName(config.getItemName());
        if (config.getJob() != null) {
            DataSourceJobConfiguration job = config.getJob();
            setEnabled(job.isEnabled());
            setCronExpression(job.getCronExpression());
            setTriggerAtStartup(job.isTriggerAtStartup());
        }
    }

    private void saveConstellation(ServiceConstellation constellation) {
        // serviceEntity
        ProxyServiceEntity service = insertRepository.insertService(constellation.getService());
        insertRepository.prepareInserting(service);

        // save all constellations
        constellation.getDatasets().forEach((dataset) -> {
            final ProcedureEntity procedure = constellation.getProcedures().get(dataset.getProcedure());
            procedure.setService(service);
            final CategoryEntity category = constellation.getCategories().get(dataset.getCategory());
            category.setService(service);
            final FeatureEntity feature = constellation.getFeatures().get(dataset.getFeature());
            feature.setService(service);
            final OfferingEntity offering = constellation.getOfferings().get(dataset.getOffering());
            offering.setService(service);
            final PhenomenonEntity phenomenon = constellation.getPhenomenons().get(dataset.getPhenomenon());
            phenomenon.setService(service);
            // add empty unit entity, will be replaced later in the repositories
            final UnitEntity unit = EntityBuilder.createUnit("", service); 

            if (procedure != null && category != null && feature != null && offering != null && phenomenon != null && unit != null) {
                MeasurementDatasetEntity measurement = EntityBuilder.createMeasurementDataset(
                        procedure, category, feature, offering, phenomenon, unit, service
                );
                insertRepository.insertDataset(measurement);
                LOGGER.info("Add dataset constellation: " + dataset);
            } else {
                LOGGER.warn("Can't add dataset: " + dataset);
            }
        });

        insertRepository.cleanUp(service);
    }

    private GetCapabilitiesResponse getCapabilities(DataSourceConfiguration config) {
        try {
            SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
            String url = config.getUrl();
            if (url.contains("?")) {
                url = url + "&";
            } else {
                url = url + "?";
            }
            HttpResponse response = simpleHttpClient.executeGet(url + "service=SOS&request=GetCapabilities");
            XmlObject xmlResponse = XmlObject.Factory.parse(response.getEntity().getContent());
            return (GetCapabilitiesResponse) decoderRepository.getDecoder(CodingHelper.getDecoderKey(xmlResponse)).decode(xmlResponse);
        } catch (IOException | UnsupportedOperationException | XmlException | DecodingException ex) {
            java.util.logging.Logger.getLogger(DataSourceHarvesterJob.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
