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
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.om.ObservationValue;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.om.values.Value;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesRequest;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityRequest;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse;
import org.n52.shetland.ogc.sos.request.GetFeatureOfInterestRequest;
import org.n52.shetland.ogc.sos.request.GetObservationRequest;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.n52.shetland.ogc.sos.response.GetObservationResponse;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.Encoder;
import org.n52.svalbard.encode.EncoderKey;
import org.n52.svalbard.encode.exception.EncodingException;
import org.n52.svalbard.util.CodingHelper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class SOS2Connector extends AbstractSosConnector {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SOS2Connector.class);

    @Override
    protected boolean canHandle(DataSourceConfiguration config) {
        return true;
    }

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config) {
        try {
            ServiceConstellation serviceConstellation = new ServiceConstellation();
            serviceConstellation.setService(EntityBuilder.createService(config.getItemName(), getConnectorName(), config.getUrl(), Sos2Constants.SERVICEVERSION));

            HttpResponse response = this.sendRequest(createGetCapabilitiesDocument(), config.getUrl());
            GetCapabilitiesResponse capabilitiesResponse = createGetCapabilitiesResponse(response.getEntity().getContent());
            SosCapabilities sosCaps = (SosCapabilities) capabilitiesResponse.getCapabilities();
            addDatasets(serviceConstellation, sosCaps, config.getUrl());

            return serviceConstellation;
        } catch (EncodingException | IOException | UnsupportedOperationException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    @Override
    public List<MeasurementDataEntity> getObservations(MeasurementDatasetEntity seriesEntity, DbQuery query) {
        try {
            HttpResponse response = this.sendRequest(createGetObservationDocument(seriesEntity, query), seriesEntity.getService().getUrl());
            GetObservationResponse obsResp = createGetObservationResponse(response.getEntity().getContent());

            List<MeasurementDataEntity> data = new ArrayList<>();

            obsResp.getObservationCollection().forEach((observation) -> {
                MeasurementDataEntity entity = new MeasurementDataEntity();
                ObservationValue<? extends Value<?>> value = observation.getValue();

//                entity.setValue(observation.getValue());
            });

            return data;
        } catch (EncodingException | IOException | UnsupportedOperationException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    private void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps, String serviceURI) {
        Optional.ofNullable(sosCaps).map((capabilities) -> {
            Optional<SortedSet<SosObservationOffering>> contents = capabilities.getContents().map((offerings) -> {
                offerings.forEach((offering) -> {

                    String offeringId = addOffering(offering, serviceConstellation);

                    offering.getProcedures().forEach((procedureId) -> {
                        try {
                            addProcedure(procedureId, serviceConstellation);

                            HttpResponse response = this.sendRequest(createFOIRequest(procedureId), serviceURI);
                            GetFeatureOfInterestResponse foiResponse = createFoiResponse(response.getEntity().getContent());

                            addFeature(foiResponse, serviceConstellation);

                            GetDataAvailabilityResponse gdaResponse = createGDAResponse(this.sendRequest(createGDARequest(procedureId), serviceURI).getEntity().getContent());
                            gdaResponse.getDataAvailabilities().forEach((dataAval) -> {
                                String phenomenonId = addPhenomenon(dataAval, serviceConstellation);
                                String categoryId = addCategory(dataAval, serviceConstellation);
                                String featureId = dataAval.getFeatureOfInterest().getHref();
                                serviceConstellation.add(new DatasetConstellation(procedureId, offeringId, categoryId, phenomenonId, featureId));
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

    private String addCategory(GetDataAvailabilityResponse.DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String categoryId = dataAval.getObservedProperty().getHref();
        String categoryName = dataAval.getObservedProperty().getTitle();
        serviceConstellation.putCategory(categoryId, categoryName);
        return categoryId;
    }

    private String addPhenomenon(GetDataAvailabilityResponse.DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String phenomenonId = dataAval.getObservedProperty().getHref();
        String phenomenonName = dataAval.getObservedProperty().getTitle();
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonName);
        return phenomenonId;
    }

    private String addProcedure(String procedureId, ServiceConstellation serviceConstellation) {
        serviceConstellation.putProcedure(procedureId, procedureId, true, false);
        return procedureId;
    }

    private String addOffering(SosObservationOffering offering, ServiceConstellation serviceConstellation) {
        String offeringId = offering.getIdentifier();
        serviceConstellation.putOffering(offeringId, offeringId);
        return offeringId;
    }

    private String addFeature(GetFeatureOfInterestResponse foiResponse, ServiceConstellation serviceConstellation) {
        SamplingFeature abstractFeature = (SamplingFeature) foiResponse.getAbstractFeature();
        String featureId = abstractFeature.getIdentifier();
        String featureName;
        if (abstractFeature.getName().size() == 1 && abstractFeature.getName().get(0).getValue() != null) {
            featureName = abstractFeature.getName().get(0).getValue();
        } else {
            featureName = featureId;
        }
        double lat = abstractFeature.getGeometry().getCoordinate().x;
        double lng = abstractFeature.getGeometry().getCoordinate().y;
        int srid = abstractFeature.getGeometry().getSRID();
        serviceConstellation.putFeature(featureId, featureName, lat, lng, srid);
        return featureId;
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

    private XmlObject createGetObservationDocument(MeasurementDatasetEntity series, DbQuery query) throws EncodingException {
        GetObservationRequest request = new GetObservationRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(new ArrayList<>(Arrays.asList(series.getProcedure().getDomainId())));
        request.setOfferings(new ArrayList<>(Arrays.asList(series.getOffering().getDomainId())));
        request.setObservedProperties(new ArrayList<>(Arrays.asList(series.getPhenomenon().getDomainId())));
        request.setFeatureIdentifiers(new ArrayList<>(Arrays.asList(series.getFeature().getDomainId())));
        // TODO add temporal filter
        EncoderKey encoderKey = CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, request);
        Object encode = encoderRepository.getEncoder(encoderKey).encode(request);
        return (XmlObject) encode;
    }

    private GetObservationResponse createGetObservationResponse(InputStream responseStream) {
        try {
            XmlObject response = XmlObject.Factory.parse(responseStream);
            DecoderKey decoderKey = CodingHelper.getDecoderKey(response);
            Decoder<Object, Object> decoder = decoderRepository.getDecoder(decoderKey);
            return (GetObservationResponse) decoder.decode(response);
        } catch (XmlException | IOException | DecodingException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return null;
    }

}
