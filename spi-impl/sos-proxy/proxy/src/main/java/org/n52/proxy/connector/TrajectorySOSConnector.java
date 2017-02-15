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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.gml.CodeType;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.om.SingleObservationValue;
import org.n52.shetland.ogc.om.values.QuantityValue;
import org.n52.shetland.ogc.ows.OwsCapabilities;
import org.n52.shetland.ogc.ows.OwsServiceProvider;
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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class TrajectorySOSConnector extends AbstractSosConnector {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TrajectorySOSConnector.class);

    /**
     * Matches when the provider name is equal "52North" and service version is
     * 2.0.0
     */
    @Override
    protected boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        OwsCapabilities owsCaps = capabilities.getCapabilities();
        if (owsCaps.getVersion().equals(Sos2Constants.SERVICEVERSION) && owsCaps.getServiceProvider().isPresent()) {
            OwsServiceProvider servProvider = owsCaps.getServiceProvider().get();
            if (servProvider.getProviderName().equals("52North")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        try {
            addService(serviceConstellation, config);
            SosCapabilities sosCaps = (SosCapabilities) capabilities.getCapabilities();
            addDatasets(serviceConstellation, sosCaps, config.getUrl());
        } catch (UnsupportedOperationException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return serviceConstellation;
    }

    @Override
    public List<MeasurementDataEntity> getObservations(MeasurementDatasetEntity seriesEntity, DbQuery query) {
        GetObservationResponse obsResp = createObservationResponse(seriesEntity, query);

        List<MeasurementDataEntity> data = new ArrayList<>();

        obsResp.getObservationCollection().forEach((observation) -> {
            MeasurementDataEntity entity = new MeasurementDataEntity();
            SingleObservationValue obsValue = (SingleObservationValue) observation.getValue();

            TimeInstant instant = (TimeInstant) obsValue.getPhenomenonTime();
            entity.setTimestart(instant.getValue().toDate());
            entity.setTimeend(instant.getValue().toDate());
            QuantityValue value = (QuantityValue) obsValue.getValue();
            entity.setValue(value.getValue());

            data.add(entity);
        });
        LOGGER.info("Found " + data.size() + " Entries");

        return data;
    }

    private void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps, String serviceUri) {
        Optional.ofNullable(sosCaps).map((capabilities) -> {
            Optional<SortedSet<SosObservationOffering>> contents = capabilities.getContents().map((offerings) -> {
                offerings.forEach((offering) -> {
                    doForOffering(offering, serviceConstellation, serviceUri);
                });
//                doForOffering(offerings.first(), serviceConstellation, serviceUri);
                return null;
            });
            return null;
        });
    }

    private void doForOffering(SosObservationOffering offering, ServiceConstellation serviceConstellation, String serviceUri) {
        String offeringId = addOffering(offering, serviceConstellation);
                    offering.getProcedures().forEach((procedureId) -> {
                        offering.getObservableProperties().forEach((obsProp) -> {
                            doDataAvailability(obsProp, procedureId, offeringId, serviceUri, serviceConstellation);
                        });
                    });
//        doDataAvailability(offering.getObservableProperties().first(), offering.getProcedures().first(), offeringId, serviceUri, serviceConstellation);
    }

    private void doDataAvailability(String obsProp, String procedureId, String offeringId, String serviceUri, ServiceConstellation serviceConstellation) {
        GetDataAvailabilityResponse gdaResponse = getDataAvailabilityResponse(procedureId, offeringId, obsProp, serviceUri);
        gdaResponse.getDataAvailabilities().forEach((dataAval) -> {
            String featureId = addFeature(dataAval, serviceConstellation);
            addProcedure(dataAval, serviceConstellation);
            String phenomenonId = addPhenomenon(dataAval, serviceConstellation);
            String categoryId = addCategory(dataAval, serviceConstellation);
            serviceConstellation.add(new DatasetConstellation(procedureId, offeringId, categoryId, phenomenonId, featureId));
        });
    }

    private void addService(ServiceConstellation serviceConstellation, DataSourceConfiguration config) {
        serviceConstellation.setService(EntityBuilder.createService(config.getItemName(), "here goes description", getConnectorName(), config.getUrl(), Sos2Constants.SERVICEVERSION));
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

    private String addProcedure(GetDataAvailabilityResponse.DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String procedureId = dataAval.getProcedure().getHref();
        String procedureName = dataAval.getProcedure().getTitle();
        serviceConstellation.putProcedure(procedureId, procedureName, true, true);
        return procedureId;
    }

    private String addOffering(SosObservationOffering offering, ServiceConstellation serviceConstellation) {
        String offeringId = offering.getIdentifier();
        CodeType name = offering.getFirstName();
        if (name != null) {
            serviceConstellation.putOffering(offeringId, name.getValue());
        } else {
            serviceConstellation.putOffering(offeringId, offeringId);
        }
        return offeringId;
    }

    private String addFeature(GetDataAvailabilityResponse.DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String featureId = dataAval.getFeatureOfInterest().getHref();
        String featureName = dataAval.getFeatureOfInterest().getTitle();
        serviceConstellation.putFeature(featureId, featureName, 0, 0, 0);
        return featureId;
    }

//    private String addFeature(GetFeatureOfInterestResponse foiResponse, ServiceConstellation serviceConstellation) {
//        SamplingFeature abstractFeature = (SamplingFeature) foiResponse.getAbstractFeature();
//        String featureId = abstractFeature.getIdentifier();
//        String featureName;
//        if (abstractFeature.getName().size() == 1 && abstractFeature.getName().get(0).getValue() != null) {
//            featureName = abstractFeature.getName().get(0).getValue();
//        } else {
//            featureName = featureId;
//        }
//        double lat = abstractFeature.getGeometry().getCoordinate().x;
//        double lng = abstractFeature.getGeometry().getCoordinate().y;
//        int srid = abstractFeature.getGeometry().getSRID();
//        serviceConstellation.putFeature(featureId, featureName, lat, lng, srid);
//        return featureId;
//    }
    private GetFeatureOfInterestResponse getFeatureOfInterestResponse(String procedureId, String serviceUri) {
        GetFeatureOfInterestRequest request = new GetFeatureOfInterestRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(new ArrayList<>(Arrays.asList(procedureId)));
        return (GetFeatureOfInterestResponse) getSosRepsonseFor(request, Sos2Constants.NS_SOS_20, serviceUri);
    }

    private GetDataAvailabilityResponse getDataAvailabilityResponse(String procedureId, String offeringId, String obsPropId, String serviceUri) {
        GetDataAvailabilityRequest request = new GetDataAvailabilityRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(Arrays.asList(procedureId));
        request.setOffering(Arrays.asList(offeringId));
        request.setObservedProperty(Arrays.asList(obsPropId));
        LOGGER.info("Send GetDataAvailability: " + request.toString());
        return (GetDataAvailabilityResponse) getSosRepsonseFor(request, Sos2Constants.NS_SOS_20, serviceUri);
    }

    private GetObservationResponse createObservationResponse(MeasurementDatasetEntity seriesEntity, DbQuery query) {
        GetObservationRequest request = new GetObservationRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(new ArrayList<>(Arrays.asList(seriesEntity.getProcedure().getDomainId())));
        request.setOfferings(new ArrayList<>(Arrays.asList(seriesEntity.getOffering().getDomainId())));
        request.setObservedProperties(new ArrayList<>(Arrays.asList(seriesEntity.getPhenomenon().getDomainId())));
        request.setFeatureIdentifiers(new ArrayList<>(Arrays.asList(seriesEntity.getFeature().getDomainId())));
        Time time = new TimePeriod(query.getTimespan().getStart(), query.getTimespan().getEnd());
        TemporalFilter temporalFilter = new TemporalFilter(FilterConstants.TimeOperator.TM_During, time, "phenomenonTime");
        request.setTemporalFilters(new ArrayList<>(Arrays.asList(temporalFilter)));
        return (GetObservationResponse) this.getSosRepsonseFor(request, Sos2Constants.NS_SOS_20, seriesEntity.getService().getUrl());
    }

}
