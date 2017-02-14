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
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.AbstractSosConnector;
import org.n52.proxy.connector.ServiceConstellation;
import org.n52.proxy.harvest.DataSourceHarvesterJob;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.util.CodingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:artic-sea-test.xml"})
public class SosConnectorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosConnectorTest.class);

    @Autowired
    private Set<AbstractSosConnector> connectors;

    @Autowired
    private DecoderRepository decoderRepository;

//    @Test
    public void collectLocalhost() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName("serviceName");
        config.setUrl("http://localhost:8081/52n-sos-webapp/service");
        testConfig(config);
    }
    
//    @Test
    public void collectSensorwebTestbed() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName("sensorwebTestbed");
        config.setUrl("http://sensorweb.demo.52north.org/sensorwebtestbed/service");
        testConfig(config);
    }
    
//    @Test
    public void collectSensorwebDemo() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName("sensorwebDemo");
        config.setUrl("http://sensorweb.demo.52north.org/52n-sos-webapp/service");
        testConfig(config);
    }

//    @Test
    public void collectOceanotron() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName("serviceName");
        config.setUrl("http://oceanotrondemo.ifremer.fr/oceanotron/SOS/default");
        config.setConnector("OceanotronSosConnector");
        testConfig(config);
    }

//    @Test
    public void collectHzgSOS() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName("serviceName");
        config.setUrl("http://codm.hzg.de/52n-sos-webapp/service");
        config.setConnector("TrajectorySOSConnector");
        testConfig(config);
    }

    private void testConfig(DataSourceConfiguration config) {
        ServiceConstellation constellation = findConstellation(config);
        printConstellation(constellation);
    }
    
    private void printConstellation(ServiceConstellation constellation) {
        constellation.getCategories().forEach((id, entity) -> {
            LOGGER.info("Category: " + id + " - " + entity.getName());
        });
        constellation.getFeatures().forEach((id, entity) -> {
            LOGGER.info("Feature: " + id + " - " + entity.getName());
        });
        constellation.getOfferings().forEach((id, entity) -> {
            LOGGER.info("Offering: " + id + " - " + entity.getName());
        });
        constellation.getPhenomenons().forEach((id, entity) -> {
            LOGGER.info("Phenomenon: " + id + " - " + entity.getName());
        });
        constellation.getProcedures().forEach((id, entity) -> {
            LOGGER.info("Procedure: " + id + " - " + entity.getName());
        });
        constellation.getDatasets().forEach(coll -> {
            LOGGER.info("DatasetCollection: " + coll);
        });
        LOGGER.info("Service: " + constellation.getService());
    }

    private ServiceConstellation findConstellation(DataSourceConfiguration config) {
        Iterator<AbstractSosConnector> iterator = connectors.iterator();
        AbstractSosConnector current = iterator.next();
        GetCapabilitiesResponse capabilities = createCapabilities(config);
        while (!current.matches(config, capabilities)) {
            LOGGER.info(current.toString() + " cannot handle " + config);
            current = iterator.next();
        }
        LOGGER.info(current.toString() + " create a constellation for " + config);
        ServiceConstellation constellation = current.getConstellation(config, capabilities);
        return constellation;
    }

    private GetCapabilitiesResponse createCapabilities(DataSourceConfiguration config) {
        try {
            SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
            HttpResponse response = simpleHttpClient.executeGet(config.getUrl() + "?service=SOS&request=GetCapabilities");
            XmlObject xmlResponse = XmlObject.Factory.parse(response.getEntity().getContent());
            return (GetCapabilitiesResponse) decoderRepository.getDecoder(CodingHelper.getDecoderKey(xmlResponse)).decode(xmlResponse);
        } catch (IOException | UnsupportedOperationException | XmlException | DecodingException ex) {
            java.util.logging.Logger.getLogger(DataSourceHarvesterJob.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
