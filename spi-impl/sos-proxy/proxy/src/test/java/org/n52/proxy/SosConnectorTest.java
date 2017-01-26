package org.n52.proxy;

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
import javax.inject.Inject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.proxy.connector.SOS2Connector;
import org.n52.proxy.connector.ServiceConstellation;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.encode.EncoderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:artic-sea-test.xml"})
public class SosConnectorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosConnectorTest.class);

    private final String uri = "http://sensorweb.demo.52north.org/sensorwebtestbed/service";
//    private final String uri = "http://sensorweb.demo.52north.org/52n-sos-webapp/service";

    @Inject
    private EncoderRepository encoderRepository;

    @Inject
    private DecoderRepository decoderRepository;

    @Test
    public void collectEntities() {
        SOS2Connector connector = new SOS2Connector(uri, "serviceName", "serviceDescription", decoderRepository, encoderRepository);
        ServiceConstellation constellation = connector.getConstellation();
        constellation.getCategories().forEach((name, entity) -> {
            LOGGER.info("Category: " + name);
        });
        constellation.getFeatures().forEach((name, entity) -> {
            LOGGER.info("Features: " + name);
        });
        constellation.getOfferings().forEach((name, entity) -> {
            LOGGER.info("Offerings: " + name);
        });
        constellation.getPhenomenons().forEach((name, entity) -> {
            LOGGER.info("Phenomenons: " + name);
        });
        constellation.getProcedures().forEach((name, entity) -> {
            LOGGER.info("Features: " + name);
        });
        constellation.getDatasets().forEach(coll -> {
            LOGGER.info("DatasetCollection: " + coll);
        });
        LOGGER.info("Service: " + constellation.getService());
    }

}
