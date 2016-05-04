/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.da.beans.v1.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.v1.FeatureOutput;
import org.n52.io.response.v1.PhenomenonOutput;
import org.n52.io.response.v1.ProcedureOutput;
import org.n52.io.response.v2.test.FeatureOutputCollection;
import org.n52.io.response.v2.test.GeometryOutputCollection;
import org.n52.io.response.v2.test.MeasurementSeriesOutput;
import org.n52.io.response.v2.test.ObservationType;
import org.n52.io.response.v2.test.PhenomenonOutputCollection;
import org.n52.io.response.v2.test.PlatformOutput;
import org.n52.io.response.v2.test.PlatformType;
import org.n52.io.response.v2.test.ProcedureOutputCollection;
import org.n52.io.response.v2.test.SeriesMetadataOutput;
import org.n52.io.response.v2.test.SeriesParameters;
import org.n52.io.response.v2.test.SeriesOutputCollection;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public enum PlatformExample {

    BEVER_TALSPERRE,
    RV_SONNE_MISSION_1,
    RV_SONNE_MISSION_2,
    STATIC_WEBCAM,
    LANDSAT_SATELLITE;

    private final String id = UUID.randomUUID().toString();

    public PlatformOutput getCondensed() {
        PlatformOutput platform = null;
        switch (this) {
            case BEVER_TALSPERRE:
                platform = createCondensedPlatform(PlatformType.STATIONARY_INSITU, "Bever Talsperre");
                break;
            case RV_SONNE_MISSION_1:
                platform = createCondensedPlatform(PlatformType.MOBILE_INSITU, "Research Vessel Sonne, Mission 1");
                break;
            case RV_SONNE_MISSION_2:
                platform = createCondensedPlatform(PlatformType.MOBILE_INSITU, "Research Vessel Sonne, Mission 2");
                break;
            case STATIC_WEBCAM:
                platform = createCondensedPlatform(PlatformType.STATIONARY_REMOTE, "Ordinary Webcam");
                break;
            case LANDSAT_SATELLITE:
                platform = createCondensedPlatform(PlatformType.MOBILE_REMOTE, "Landsat Satellite");
                break;
            default:
        }
        return platform;
    }

    public PlatformOutput getOutput() {
        PlatformOutput output = getCondensed();
        switch (this) {
            case BEVER_TALSPERRE:
                output.setDomainId(output.getLabel().replace(" ", "_"));
                output.setGeometries(new GeometryOutputCollection(
                        Collections.singletonList(GeometryExample.SITE_BEVER_TALSPERRE.getCondensed())
                ));
                output.setFeatures(new FeatureOutputCollection(
                        Collections.singletonList(FeatureExample.STATION_BEVER_TALSPERRE.getCondensed())
                ));
                output.setPhenomena(new PhenomenonOutputCollection(
                        PhenomenonExample.PRECIPITATION_HEIGHT.getCondensed()
                ));
                output.setProcedures(new ProcedureOutputCollection(
                        ProcedureExample.PRECIPITATION_HOURLY.getCondensed(),
                        ProcedureExample.PRECIPITATION_DAILY.getCondensed()
                ));
                break;
            case RV_SONNE_MISSION_1:
                addMobilePlatformResources(output);
                break;
            case RV_SONNE_MISSION_2:
                addMobilePlatformResources(output);
                break;
            case STATIC_WEBCAM:
                output.setGeometries(new GeometryOutputCollection(
                        GeometryExample.SITE_BEVER_TALSPERRE.getCondensed(),
                        GeometryExample.STATIC_OBSERVATION_WEBCAM.getCondensed()
                ));
                output.setFeatures(new FeatureOutputCollection(
                        FeatureExample.TALSPERRE_ABFLUSS.getCondensed()
                ));
                output.setPhenomena(new PhenomenonOutputCollection(
                        PhenomenonExample.RADIANCE.getCondensed()
                ));
                break;
            default:
        }

        output.setSeries(createSeriesCollectionFor(output));
        return output;
    }

    private void addMobilePlatformResources(PlatformOutput output) {
        if (this == RV_SONNE_MISSION_1) {
            output.setDomainId(RV_SONNE_MISSION_1.name());
            output.setGeometries(new GeometryOutputCollection(
                    GeometryExample.TRACK_1_RV_SONNE_MISSION_1.getCondensed(),
                    GeometryExample.TRACK_1_RV_SONNE_MISSION_1.getCondensed()
            ));
        } else {
            output.setDomainId(RV_SONNE_MISSION_2.name());
            output.setGeometries(new GeometryOutputCollection(
                    GeometryExample.TRACK_1_RV_SONNE_MISSION_2.getCondensed(),
                    GeometryExample.TRACK_1_RV_SONNE_MISSION_2.getCondensed()
            ));
        }
        output.setFeatures(new FeatureOutputCollection(
                FeatureExample.TRACK_1_RESEACH_VESSEL_SONNE.getCondensed(),
                FeatureExample.TRACK_2_RESEACH_VESSEL_SONNE.getCondensed()
        ));
        output.setPhenomena(new PhenomenonOutputCollection(
                PhenomenonExample.PRECIPITATION_HEIGHT.getCondensed(),
                PhenomenonExample.AIR_TEMPERATURE.getCondensed(),
                PhenomenonExample.SALINITY.getCondensed()
        ));
        output.setProcedures(new ProcedureOutputCollection(
                ProcedureExample.PROFILE_MEASUREMENTS.getCondensed()
        ));
    }

    public PlatformOutput createCondensedPlatform(PlatformType type, String label) {
        PlatformOutput condensed = new PlatformOutput(type);
        condensed.setId(id);
        condensed.setLabel(label);
        condensed.setHref(ExampleConstants.BASE_URL + "/platforms/" + condensed.getId());
        return condensed;
    }

    private SeriesOutputCollection createSeriesCollectionFor(PlatformOutput platform) {
        SeriesOutputCollection seriesCollection = new SeriesOutputCollection();

        switch (platform.getType()) {
            case STATIONARY_INSITU:
                seriesCollection.addItems(createStationryInsituSeries(platform));
                break;
            case MOBILE_INSITU:
                seriesCollection.addItems(createMobileInsituSeries(platform));

                break;
            case STATIONARY_REMOTE:
                break;
            default:
                throw new IllegalStateException("not implemented platform type: " + platform.getType());
        }

        return seriesCollection;
    }

    private Collection<SeriesMetadataOutput<SeriesParameters>> createStationryInsituSeries(PlatformOutput platform) {
        List<SeriesMetadataOutput<SeriesParameters>> series = new ArrayList<>();
        FeatureOutput feature = new FeatureOutputCollection(platform.getFeatures()).getItem(0);
        for (PhenomenonOutput phenomenon : platform.getPhenomena()) {
            for (ProcedureOutput procedure : platform.getProcedures()) {
                MeasurementSeriesOutput output = new MeasurementSeriesOutput();
                output.setId(UUID.randomUUID().toString());
                output.setLabel(phenomenon.getLabel() + "@" + feature.getLabel());
                output.setHref(ExampleConstants.BASE_URL + "/series/" + output.getObservationType() + "/" + output.getId());

                SeriesParameters parameters = new SeriesParameters();
                parameters.setService(ExampleConstants.service);
                parameters.setOffering(OfferingExample.PRECIPITATION.getCondensed());
                parameters.setPhenomenon(phenomenon);
                parameters.setFeature(feature);
                parameters.setProcedure(procedure);
                output.setParameters(parameters);
                series.add(output);
            }
        }
        return series;
    }

    private Collection<SeriesMetadataOutput<SeriesParameters>> createMobileInsituSeries(PlatformOutput platform) {
        List<SeriesMetadataOutput<SeriesParameters>> series = new ArrayList<>();
        for (FeatureOutput feature : platform.getFeatures()) {
            for (PhenomenonOutput phenomenon : platform.getPhenomena()) {
                for (ProcedureOutput procedure : platform.getProcedures()) {
                    MeasurementSeriesOutput output = new MeasurementSeriesOutput();
                    output.setId(UUID.randomUUID().toString());
                    output.setLabel(phenomenon.getLabel() + "@" + feature.getLabel());

                    SeriesParameters parameters = new SeriesParameters();
                    parameters.setOffering(OfferingExample.valueOf(platform.getDomainId()).getCondensed());
                    parameters.setService(ExampleConstants.service);
                    parameters.setPhenomenon(phenomenon);
                    parameters.setFeature(feature);
                    parameters.setProcedure(procedure);
                    output.setParameters(parameters);
                    series.add(output);
                }
            }
        }
        return series;
    }

}
