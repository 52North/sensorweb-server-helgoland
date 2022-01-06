/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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

public interface ResoureControllerConstants {

    String RESOURCE_SERVICES = "services";
    String RESOURCE_CATEGORIES = "categories";
    String RESOURCE_OFFERINGS = "offerings";
    String RESOURCE_FEATURES = "features";
    String RESOURCE_PROCEDURES = "procedures";
    String RESOURCE_PHENOMENA = "phenomena";
    String RESOURCE_PLATFORMS = "platforms";
    String RESOURCE_DATASETS = "datasets";
    String RESOURCE_SAMPLINGS = "samplings";
    String RESOURCE_MEASURING_PROGRAMS = "measuringPrograms";
    String RESOURCE_TAGS = "tags";
    String RESOURCE_TIMESERIES = "timeseries";
    String RESOURCE_TRAJECTORIES = "trajectories";
    String RESOURCE_INDIVIDUAL_OBSERVATIONS = "individualObservations";

    String LABEL_SERVICES = "Service Provider";
    String LABEL_CATEGORIES = "Category";
    String LABEL_OFFERINGS = "Offering";
    String LABEL_FEATURES = "Feature";
    String LABEL_PROCEDURES = "Procedure";
    String LABEL_PHENOMENA = "Phenomenon";
    String LABEL_PLATFORMS = "Platform";
    String LABEL_DATASETS = "Dataset";
    String LABEL_SAMPLINGS = "Sampling";
    String LABEL_MEASURING_PROGRAMS = "MeasuringProgram";
    String LABEL_TAGS = "Tag";
    String LABEL_TIMESERIES = "Timeseries";
    String LABEL_TRAJECTORIES = "Trajectory";
    String LABEL_INDIVIDUAL_OBSERVATIONS = "IndividualObservation";

    String DESCRIPTION_KEY_SERVICES = "msg.web.resources.services";
    String DESCRIPTION_KEY_CATEGORIES = "msg.web.resources.categories";
    String DESCRIPTION_KEY_OFFERINGS = "msg.web.resources.offerings";
    String DESCRIPTION_KEY_FEATURES = "msg.web.resources.features";
    String DESCRIPTION_KEY_PROCEDURES = "msg.web.resources.procedures";
    String DESCRIPTION_KEY_PHENOMENA = "msg.web.resources.phenomena";
    String DESCRIPTION_KEY_PLATFORMS = "msg.web.resources.platforms";
    String DESCRIPTION_KEY_DATASETS = "msg.web.resources.datasets";
    String DESCRIPTION_KEY_SAMPLINGS = "msg.web.resources.samplings";
    String DESCRIPTION_KEY_MEASURING_PROGRAMS = "msg.web.resources.measuringPrograms";
    String DESCRIPTION_KEY_TAGS = "msg.web.resources.tags";
    String DESCRIPTION_KEY_TIMESERIES  = "msg.web.resources.timeseries";
    String DESCRIPTION_KEY_TRAJECTORIES = "msg.web.resources.individualObservations";
    String DESCRIPTION_KEY_INDIVIDUAL_OBSERVATIONS = "msg.web.resources.trajectories";

    String DEFAULT_DESCRIPTION_SERVICES = "A service provider offers timeseries data.";
    String DEFAULT_DESCRIPTION_CATEGORIES = "A category groups available timeseries.";
    String DEFAULT_DESCRIPTION_OFFERINGS = "An organizing unit to filter resources.";
    String DEFAULT_DESCRIPTION_FEATURES = "A location where the observation takes place.";
    String DEFAULT_DESCRIPTION_PROCEDURES = "A procedure/senor that produces an observation.";
    String DEFAULT_DESCRIPTION_PHENOMENA = "A phenomenon that is observed.";
    String DEFAULT_DESCRIPTION_PLATFORMS = "A sensor platform where observations are made.";
    String DEFAULT_DESCRIPTION_DATASETS = "Represents a sequence of data values observed over time.";
    String DEFAULT_DESCRIPTION_SAMPLINGS = "A sampling represents a sequence of data values.";
    String DEFAULT_DESCRIPTION_MEASURING_PROGRAMS = "A measuring program to group samplings.";
    String DEFAULT_DESCRIPTION_TAGS = "Used to tag dataset resources.";
    String DEFAULT_DESCRIPTION_TIMESERIES = "Represents a sequence of data values measured over time.";
    String DEFAULT_DESCRIPTION_TRAJECTORIES = "Represents a sequence of trajectory data values observed over time.";
    String DEFAULT_DESCRIPTION_INDIVIDUAL_OBSERVATIONS =
            "Represents a sequence of individual data values observed over time.";
}
