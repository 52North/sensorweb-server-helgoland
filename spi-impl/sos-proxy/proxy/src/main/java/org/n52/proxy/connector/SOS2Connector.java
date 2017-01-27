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
package org.n52.proxy.connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.SortedSet;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesRequest;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityRequest;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.shetland.ogc.sos.request.GetFeatureOfInterestRequest;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.Encoder;
import org.n52.svalbard.encode.EncoderKey;
import org.n52.svalbard.encode.EncoderRepository;
import org.n52.svalbard.encode.exception.EncodingException;
import org.n52.svalbard.util.CodingHelper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class SOS2Connector extends AbstractSOSConnector {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SOS2Connector.class);

    private final EncoderRepository encoderRepository;
    private final DecoderRepository decoderRepository;

    public SOS2Connector(String serviceURI, String name, String description, DecoderRepository decoderRepo, EncoderRepository encoderRepo) {
        super(serviceURI, name, description);
        encoderRepository = encoderRepo;
        decoderRepository = decoderRepo;
    }

    public ServiceConstellation getConstellation() {
        try {
            ServiceConstellation serviceConstellation = new ServiceConstellation();
            serviceConstellation.setService(EntityBuilder.createService(serviceName, serviceDescription, serviceURI, Sos2Constants.SERVICEVERSION));

            HttpResponse response = this.sendRequest(createGetCapabilitiesDocument());
            GetCapabilitiesResponse capabilitiesResponse = createGetCapabilitiesResponse(response.getEntity().getContent());
            SosCapabilities sosCaps = (SosCapabilities) capabilitiesResponse.getCapabilities();
            addDatasets(serviceConstellation, sosCaps);

            return serviceConstellation;
        } catch (EncodingException | IOException | UnsupportedOperationException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps) {
        Optional.ofNullable(sosCaps).map((capabilities) -> {
            Optional<SortedSet<SosObservationOffering>> contents = capabilities.getContents().map((offerings) -> {
                offerings.forEach((offering) -> {

                    String offeringId = offering.getIdentifier();
                    // offering
                    serviceConstellation.putOffering(offeringId);

                    offering.getProcedures().forEach((procedure) -> {
                        try {
                            // procedure
                            serviceConstellation.putProcedure(procedure, true, false);
                            HttpResponse response = this.sendRequest(createFOIRequest(procedure));
                            GetFeatureOfInterestResponse foiResponse = createFoiResponse(response.getEntity().getContent());

                            SamplingFeature abstractFeature = (SamplingFeature) foiResponse.getAbstractFeature();
                            String featureName = abstractFeature.getIdentifier();
                            double lat = abstractFeature.getGeometry().getCoordinate().y;
                            double lng = abstractFeature.getGeometry().getCoordinate().x;
                            int srid = abstractFeature.getGeometry().getSRID();
                            // feature
                            serviceConstellation.putFeature(featureName, lat, lng, srid);

                            GetDataAvailabilityResponse gdaResponse = createGDAResponse(this.sendRequest(createGDARequest(procedure)).getEntity().getContent());
                            gdaResponse.getDataAvailabilities().forEach((dataAval) -> {
                                String phenomenon = dataAval.getObservedProperty().getTitle();
                                // phenomenon
                                serviceConstellation.putPhenomenon(phenomenon);
                                String category = dataAval.getObservedProperty().getTitle();
                                // category
                                serviceConstellation.putCategory(category);
                                String feature = dataAval.getFeatureOfInterest().getTitle();

                                serviceConstellation.add(new DatasetConstellation(procedure, offeringId, category, phenomenon, feature));
                            });

                            LOGGER.info(foiResponse.toString());
                        } catch (EncodingException | IOException | UnsupportedOperationException ex) {
                            LOGGER.error(ex.getLocalizedMessage(), ex);
                        }
                    });
                });
                return null;
            });
            return null;
        });
    }

    private XmlObject createGetCapabilitiesDocument() throws EncodingException {
        GetCapabilitiesRequest request = new GetCapabilitiesRequest(SosConstants.SOS);
        EncoderKey encoderKey = CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, request);
        Encoder<Object, Object> encoder = encoderRepository.getEncoder(encoderKey);
        XmlObject xml = (XmlObject) encoder.encode(request);
        return xml;
    }

    private GetCapabilitiesResponse createGetCapabilitiesResponse(InputStream responseStream) {
        try {
            XmlObject response = XmlObject.Factory.parse(responseStream);
            DecoderKey decoderKey = CodingHelper.getDecoderKey(response);
            Decoder<Object, Object> decoder = decoderRepository.getDecoder(decoderKey);
            GetCapabilitiesResponse temp = (GetCapabilitiesResponse) decoder.decode(response);
            return temp;
        } catch (DecodingException | XmlException | IOException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private GetFeatureOfInterestResponse createFoiResponse(InputStream responseStream) {
        try {
            XmlObject response = XmlObject.Factory.parse(responseStream);
            DecoderKey decoderKey = CodingHelper.getDecoderKey(response);
            Decoder<Object, Object> decoder = decoderRepository.getDecoder(decoderKey);
            return (GetFeatureOfInterestResponse) decoder.decode(response);
        } catch (DecodingException | XmlException | IOException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private XmlObject createFOIRequest(String procedure) throws EncodingException {
        GetFeatureOfInterestRequest request = new GetFeatureOfInterestRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(new ArrayList<>(Arrays.asList(procedure)));
        EncoderKey encoderKey = CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, request);
        return (XmlObject) encoderRepository.getEncoder(encoderKey).encode(request);
    }

    private XmlObject createGDARequest(String procedure) throws EncodingException {
        GetDataAvailabilityRequest request = new GetDataAvailabilityRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(new ArrayList<>(Arrays.asList(procedure)));
        EncoderKey encoderKey = CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, request);
        XmlObject xml = (XmlObject) encoderRepository.getEncoder(encoderKey).encode(request);
        LOGGER.info(xml.xmlText());
        return xml;
    }

    private GetDataAvailabilityResponse createGDAResponse(InputStream responseStream) {
        try {
            XmlObject response = XmlObject.Factory.parse(responseStream);
            DecoderKey decoderKey = CodingHelper.getDecoderKey(response);
            Decoder<Object, Object> decoder = decoderRepository.getDecoder(decoderKey);
            return (GetDataAvailabilityResponse) decoder.decode(response);
        } catch (DecodingException | XmlException | IOException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

}
