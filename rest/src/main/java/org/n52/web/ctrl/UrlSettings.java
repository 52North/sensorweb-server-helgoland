/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.web.ctrl;

import org.n52.io.response.CategoryOutput;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.GeometryOutput;
import org.n52.io.response.OfferingOutput;
import org.n52.io.response.PhenomenonOutput;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.ServiceOutput;
import org.n52.io.response.TagOutput;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.IndividualObservationOutput;
import org.n52.io.response.dataset.ProfileOutput;
import org.n52.io.response.dataset.StationOutput;
import org.n52.io.response.dataset.TimeseriesMetadataOutput;
import org.n52.io.response.dataset.TrajectoryOutput;
import org.n52.io.response.sampling.MeasuringProgramOutput;
import org.n52.io.response.sampling.SamplingOutput;

/**
 * <p>
 * The {@link UrlSettings} serves as markup interface, so that each controller
 * instance uses the same URL subpaths.</p>
 *
 * <p>
 * <b>Note:</b> Do not code against this type.</p>
 */
public interface UrlSettings {

    /**
     * Subpath identifying the search.
     */
    String SEARCH = "/search";

    /**
     * Subpath identifying a collection of services available.
     */
    String COLLECTION_SERVICES = "/" + ServiceOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of categories available.
     */
    String COLLECTION_CATEGORIES = "/" + CategoryOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of tags available.
     */
    String COLLECTION_TAGS = "/" + TagOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of offerings available.
     */
    String COLLECTION_OFFERINGS = "/" + OfferingOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of features available.
     */
    String COLLECTION_FEATURES = "/" + FeatureOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of procedures available.
     */
    String COLLECTION_PROCEDURES = "/" + ProcedureOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of phenomenons available.
     */
    String COLLECTION_PHENOMENA = "/" + PhenomenonOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of stations available.
     */
    String COLLECTION_STATIONS = "/" + StationOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of timeseries metadata available.
     */
    String COLLECTION_TIMESERIES = "/" + TimeseriesMetadataOutput.COLLECTION_PATH;

    /**
     * Subpaths identifying platforms collections available.
     */
    String COLLECTION_PLATFORMS = "/" + PlatformOutput.COLLECTION_PATH;

    /**
     * Subpaths identifying datasets collections available.
     */
    String COLLECTION_DATASETS = "/" + DatasetOutput.COLLECTION_PATH;

    /**
     * Subpaths identifying individual observation collections available.
     */
    String COLLECTION_INDIVIDUAL_OBSERVATIONS = "/" + IndividualObservationOutput.COLLECTION_PATH;

    /**
     * Subpaths identifying profile collections available.
     */
    String COLLECTION_PROFILES = "/" + ProfileOutput.COLLECTION_PATH;

    /**
     * Subpaths identifying trajectories collections available.
     */
    String COLLECTION_TRAJECTORIES = "/" + TrajectoryOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of samplings available.
     */
    String COLLECTION_SAMPLINGS = "/" + SamplingOutput.COLLECTION_PATH;

    /**
     * Subpath identifying a collection of measuring programs available.
     */
    String COLLECTION_MEASURING_PROGRAMS = "/" + MeasuringProgramOutput.COLLECTION_PATH;

    /**
     * Subpaths identifying geometries collections available.
     */
    String COLLECTION_GEOMETRIES = "/" + GeometryOutput.COLLECTION_PATH;
}
