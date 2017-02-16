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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.utils.ConnectorHelper;
import org.n52.proxy.connector.utils.DatasetConstellation;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.GeometryEntity;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.filter.TemporalFilter;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.n52.shetland.ogc.om.NamedValue;
import org.n52.shetland.ogc.om.SingleObservationValue;
import org.n52.shetland.ogc.om.values.GeometryValue;
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
            config.setVersion(Sos2Constants.SERVICEVERSION);
            config.setConnector(getConnectorName());
            ConnectorHelper.addService(config, serviceConstellation);
            SosCapabilities sosCaps = (SosCapabilities) capabilities.getCapabilities();
            addDatasets(serviceConstellation, sosCaps, config.getUrl());
        } catch (UnsupportedOperationException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return serviceConstellation;
    }

    @Override
    public List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query) {
        // TODO get information only if they are not currently there...
//        GetDataAvailabilityResponse dataAvailabilityResponse = getDataAvailabilityResponse(seriesEntity);

        GetObservationResponse obsResp = createObservationResponse(seriesEntity, query);

        List<DataEntity> data = new ArrayList<>();

        obsResp.getObservationCollection().forEach((observation) -> {
            MeasurementDataEntity entity = new MeasurementDataEntity();
            SingleObservationValue obsValue = (SingleObservationValue) observation.getValue();
            TimeInstant instant = (TimeInstant) obsValue.getPhenomenonTime();
            entity.setTimestart(instant.getValue().toDate());
            entity.setTimeend(instant.getValue().toDate());
            QuantityValue value = (QuantityValue) obsValue.getValue();
            entity.setValue(value.getValue());
            Collection<NamedValue<?>> parameters = observation.getParameter();
            parameters.forEach((parameter) -> {
                if (parameter.getName().getHref().equals("http://www.opengis.net/def/param-name/OGC-OM/2.0/samplingGeometry")
                        && parameter.getValue() instanceof GeometryValue) {
                    GeometryValue geom = (GeometryValue) parameter.getValue();
                    GeometryEntity geometryEntity = new GeometryEntity();
                    geometryEntity.setLat(geom.getGeometry().getCoordinate().x);
                    geometryEntity.setLon(geom.getGeometry().getCoordinate().y);
                    geometryEntity.setAlt(geom.getGeometry().getCoordinate().z);
                    entity.setGeometry(geometryEntity);
                }
            });
            data.add(entity);
        });
        LOGGER.info("Found " + data.size() + " Entries");

        return data;
    }

    private void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps, String serviceUri) {
        if (sosCaps != null) {
//            sosCaps.getContents().get().forEach((obsOff) -> {
//                doForOffering(obsOff, serviceConstellation, serviceUri);
//            });
            doForOffering(sosCaps.getContents().get().first(), serviceConstellation, serviceUri);
        }
    }

    private void doForOffering(SosObservationOffering offering, ServiceConstellation serviceConstellation, String serviceUri) {
        String offeringId = ConnectorHelper.addOffering(offering, serviceConstellation);
//        offering.getProcedures().forEach((procedureId) -> {
//            offering.getObservableProperties().forEach((obsProp) -> {
//                doDataAvailability(obsProp, procedureId, offeringId, serviceUri, serviceConstellation);
//            });
//        });
        doDataAvailability(offering.getObservableProperties().first(), offering.getProcedures().first(), offeringId, serviceUri, serviceConstellation);
    }

    private void doDataAvailability(String obsProp, String procedureId, String offeringId, String serviceUri, ServiceConstellation serviceConstellation) {
        GetDataAvailabilityResponse gdaResponse = getDataAvailabilityResponse(procedureId, offeringId, obsProp, serviceUri);
        gdaResponse.getDataAvailabilities().forEach((dataAval) -> {
            String featureId = addFeature(dataAval, serviceConstellation);
            ConnectorHelper.addProcedure(dataAval, true, true, serviceConstellation);
            String phenomenonId = ConnectorHelper.addPhenomenon(dataAval, serviceConstellation);
            String categoryId = ConnectorHelper.addCategory(dataAval, serviceConstellation);
            serviceConstellation.add(new DatasetConstellation(procedureId, offeringId, categoryId, phenomenonId, featureId));
        });
    }

    private String addFeature(GetDataAvailabilityResponse.DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String featureId = dataAval.getFeatureOfInterest().getHref();
        String featureName = dataAval.getFeatureOfInterest().getTitle();
        serviceConstellation.putFeature(featureId, featureName, 0, 0, 0);
        return featureId;
    }

    private GetDataAvailabilityResponse getDataAvailabilityResponse(String procedureId, String offeringId, String obsPropId, String url) {
        GetDataAvailabilityRequest request = new GetDataAvailabilityRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(Arrays.asList(procedureId));
        request.setOffering(Arrays.asList(offeringId));
        request.setObservedProperty(Arrays.asList(obsPropId));
        LOGGER.info("Send GetDataAvailability: " + request.toString());
        return (GetDataAvailabilityResponse) getSosRepsonseFor(request, Sos2Constants.NS_SOS_20, url);
    }

    private GetDataAvailabilityResponse getDataAvailabilityResponse(DatasetEntity seriesEntity) {
        GetDataAvailabilityRequest request = new GetDataAvailabilityRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(Arrays.asList(seriesEntity.getProcedure().getDomainId()));
        request.setOffering(Arrays.asList(seriesEntity.getOffering().getDomainId()));
        request.setObservedProperty(Arrays.asList(seriesEntity.getPhenomenon().getDomainId()));
        request.setFeatureOfInterest(Arrays.asList(seriesEntity.getFeature().getDomainId()));
        return (GetDataAvailabilityResponse) getSosRepsonseFor(request, Sos2Constants.NS_SOS_20, seriesEntity.getService().getUrl());
    }

    private GetObservationResponse createObservationResponse(DatasetEntity seriesEntity, DbQuery query) {
        GetObservationRequest request = new GetObservationRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(new ArrayList<>(Arrays.asList(seriesEntity.getProcedure().getDomainId())));
        request.setOfferings(new ArrayList<>(Arrays.asList(seriesEntity.getOffering().getDomainId())));
        request.setObservedProperties(new ArrayList<>(Arrays.asList(seriesEntity.getPhenomenon().getDomainId())));
        request.setFeatureIdentifiers(new ArrayList<>(Arrays.asList(seriesEntity.getFeature().getDomainId())));
        return (GetObservationResponse) this.getSosRepsonseFor(request, Sos2Constants.NS_SOS_20, seriesEntity.getService().getUrl());
    }

}
