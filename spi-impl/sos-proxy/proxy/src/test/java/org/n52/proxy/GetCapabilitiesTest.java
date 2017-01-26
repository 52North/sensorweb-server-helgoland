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
import java.io.InputStream;
import javax.inject.Inject;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesRequest;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.Encoder;
import org.n52.svalbard.encode.EncoderKey;
import org.n52.svalbard.encode.EncoderRepository;
import org.n52.svalbard.encode.exception.EncodingException;
import org.n52.svalbard.util.CodingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:artic-sea-test.xml"})
public class GetCapabilitiesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetCapabilitiesTest.class);

    private String uri = "http://sensorweb.demo.52north.org/sensorwebtestbed/service";
//    private String uri = "http://sensorweb.demo.52north.org/52n-sos-webapp/service";

    @Inject
    private EncoderRepository encoderRepository;

    @Inject
    private DecoderRepository decoderRepository;

    @Test
    public void sendGetCapabilitiesRequest() throws EncodingException, IOException, XmlException, DecodingException {
        SimpleHttpClient client = new SimpleHttpClient();
        XmlObject getCapabilitiesDocument = createGetCapabilitiesDocument();
        // send getCapabilities
        HttpResponse response = client.executePost(uri, getCapabilitiesDocument);
        // handle response
        GetCapabilitiesResponse capabilitiesResponse = createGetCapabilitiesResponse(response.getEntity().getContent());
        LOGGER.info("Service: " + capabilitiesResponse.getService());
        LOGGER.info("Version: " + capabilitiesResponse.getVersion());
        LOGGER.info("XML-String: " + capabilitiesResponse.getXmlString());
        SosCapabilities sosCapabilities = (SosCapabilities) capabilitiesResponse.getCapabilities();
        sosCapabilities.getContents().get().forEach(elem -> {
            LOGGER.info("Contents-Element: " + elem);
        });
        LOGGER.info("FilterCapabilities: " + sosCapabilities.getFilterCapabilities());
    }

    private GetCapabilitiesResponse createGetCapabilitiesResponse(InputStream responseStream) {
        try {
            XmlObject response = XmlObject.Factory.parse(responseStream);
            DecoderKey decoderKey = CodingHelper.getDecoderKey(response);
            Decoder<Object, Object> decoder = decoderRepository.getDecoder(decoderKey);
            GetCapabilitiesResponse temp = (GetCapabilitiesResponse) decoder.decode(response);
            return temp;
        } catch (DecodingException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        } catch (XmlException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private XmlObject createGetCapabilitiesDocument() throws EncodingException {
        GetCapabilitiesRequest request = new GetCapabilitiesRequest("SOS");
        EncoderKey encoderKey = CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, request);
        Encoder<Object, Object> encoder = encoderRepository.getEncoder(encoderKey);
        XmlObject xml = (XmlObject) encoder.encode(request);
        return xml;
    }

}
