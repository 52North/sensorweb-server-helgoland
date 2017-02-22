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
import java.util.Set;
import java.util.logging.Level;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.AbstractSosConnector;
import org.n52.proxy.connector.utils.EntityBuilder;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.harvest.DataSourceHarvesterJob;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.util.CodingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//@Ignore
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
        String serviceName = "localhost";
        String url = "http://localhost:8081/52n-sos-webapp/service";
        config.setItemName(serviceName);
        config.setUrl(url);
        GetCapabilitiesResponse capabilities = createCapabilities(config);
        AbstractSosConnector connector = getConnector(config, capabilities);
        printConstellation(connector.getConstellation(config, capabilities));
        MeasurementDatasetEntity entity = new MeasurementDatasetEntity();
        entity.setPhenomenon(EntityBuilder.createPhenomenon("AirTemperature", "AirTemperature", null));
        entity.setProcedure(EntityBuilder.createProcedure("wxt520", "wxt520", true, false, null));
        entity.setOffering(EntityBuilder.createOffering("field_0", "field_0", null));
        entity.setFeature(EntityBuilder.createFeature("wxt520", "wxt520", null, null));
        entity.setService(EntityBuilder.createService(serviceName, "bla", "", url, ""));
        UnitEntity uom = connector.getUom(entity);
        LOGGER.info(uom.getName().toString());
    }

//    @Test
    public void collectSensorwebTestbed() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName("sensorwebTestbed");
        config.setUrl("http://sensorweb.demo.52north.org/sensorwebtestbed/service");
        GetCapabilitiesResponse capabilities = createCapabilities(config);
        AbstractSosConnector connector = getConnector(config, capabilities);
        printConstellation(connector.getConstellation(config, capabilities));
    }

//    @Test
    public void collectSensorwebDemo() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName("sensorwebDemo");
        config.setUrl("http://sensorweb.demo.52north.org/52n-sos-webapp/service");
        GetCapabilitiesResponse capabilities = createCapabilities(config);
        AbstractSosConnector connector = getConnector(config, capabilities);
        printConstellation(connector.getConstellation(config, capabilities));
    }

//    @Test
    public void collectOceanotron() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName("oceanoTron");
        config.setUrl("http://oceanotrondemo.ifremer.fr/oceanotron/SOS/default");
        config.setConnector("OceanotronSosConnector");
        GetCapabilitiesResponse capabilities = createCapabilities(config);
        AbstractSosConnector connector = getConnector(config, capabilities);
        printConstellation(connector.getConstellation(config, capabilities));
    }

//    @Test
    public void collectHzgSOS() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        String serviceName = "hzg-sos";
        String url = "http://codm.hzg.de/52n-sos-webapp/service";
        config.setItemName(serviceName);
        config.setUrl(url);
        config.setConnector("TrajectorySOSConnector");
        GetCapabilitiesResponse capabilities = createCapabilities(config);
        AbstractSosConnector connector = getConnector(config, capabilities);
//        printConstellation(connector.getConstellation(config, capabilities));
        MeasurementDatasetEntity entity = new MeasurementDatasetEntity();
        entity.setPhenomenon(EntityBuilder.createPhenomenon("O2-Sat-Ind_Aanderaa", "O2-Sat-Ind_Aanderaa", null));
        entity.setProcedure(EntityBuilder.createProcedure("FerryBox", "FerryBox", true, true, null));
        entity.setOffering(EntityBuilder.createOffering("Cuxhaven-Harwich", "Cuxhaven-Harwich", null));
        entity.setFeature(EntityBuilder.createFeature("2004-11-08T16:11:22Z Cuxhaven", "2004-11-08T16:11:22Z Cuxhaven", null, null));
        entity.setService(EntityBuilder.createService(serviceName, "bla", "", url, ""));
        connector.getObservations(entity, null);
//        UnitEntity uom = connector.getUom(entity);
//        LOGGER.info(uom.getName());
    }

//    @Test
    public void collectArpaV5() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName("arpav5");
        config.setConnector("HydroSOSConnector");
        config.setUrl("http://arpa-er-axe.geodab.eu/gi-axe-arpa/services/sos?viewId=vc0ef5197e1e74cb9bef34fe1e0ea4bbc");
        GetCapabilitiesResponse capabilities = createCapabilities(config);
        AbstractSosConnector connector = getConnector(config, capabilities);
        printConstellation(connector.getConstellation(config, capabilities));
    }

//    @Test
    public void collectAirSenSOS() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        String serviceName = "airSenSOS";
        String url = "http://sossvr1.liberaintentio.com:8080/AirSenSOS/service";
        config.setItemName(serviceName);
        config.setUrl(url);
        config.setConnector("SOS2Connector");
        GetCapabilitiesResponse capabilities = createCapabilities(config);
        AbstractSosConnector connector = getConnector(config, capabilities);
        printConstellation(connector.getConstellation(config, capabilities));
//        MeasurementDatasetEntity entity = new MeasurementDatasetEntity();
//        entity.setPhenomenon(EntityBuilder.createPhenomenon("AirTemperature", "AirTemperature", null));
//        entity.setProcedure(EntityBuilder.createProcedure("wxt520", "wxt520", true, false, null));
//        entity.setOffering(EntityBuilder.createOffering("field_0", "field_0", null));
//        entity.setFeature(EntityBuilder.createFeature("wxt520", "wxt520", null, null));
//        entity.setService(EntityBuilder.createService(serviceName, "bla", "", url, ""));
//        UnitEntity uom = connector.getUom(entity);
//        LOGGER.info(uom.getName().toString());
    }

    @Test
    public void collectNestedOfferingsSOS() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        String serviceName = "nestedOfferingsSOS";
        String url = "http://192.168.52.117:8080/52n-sos-webapp/service";
        config.setItemName(serviceName);
        config.setUrl(url);
        config.setConnector("NestedOfferingsSOSConnector");
        GetCapabilitiesResponse capabilities = createCapabilities(config);
        AbstractSosConnector connector = getConnector(config, capabilities);
        printConstellation(connector.getConstellation(config, capabilities));
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
        StringBuilder sb = new StringBuilder();
        sb.append(constellation.getCategories().size()).append(" Categories - ");
        sb.append(constellation.getFeatures().size()).append(" Features - ");
        sb.append(constellation.getOfferings().size()).append(" Offerings - ");
        sb.append(constellation.getPhenomenons().size()).append(" Phenomena - ");
        sb.append(constellation.getProcedures().size()).append(" Procedures - ");
        sb.append(constellation.getDatasets().size()).append(" Datasets");
        LOGGER.info(sb.toString());
    }

    private AbstractSosConnector getConnector(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        AbstractSosConnector found = null;
        for (AbstractSosConnector connector : connectors) {
            if (connector.matches(config, capabilities)) {
                found = connector;
                break;
            }
        }
        return found;
    }

    private GetCapabilitiesResponse createCapabilities(DataSourceConfiguration config) {
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
