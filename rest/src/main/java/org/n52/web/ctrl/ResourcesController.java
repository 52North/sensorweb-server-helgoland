/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.n52.io.I18N;
import org.n52.io.request.IoParameters;
import org.n52.series.spi.srv.CountingMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = "/", produces = {
    "application/json"
})
public class ResourcesController {

    private static final String TIMESERIES = "timeseries";
    private static final String TAJECTORIES = "trajectories";
    private static final String INDIVIDUAL_OBSERVATIONS = "individualObservations";

    private final CountingMetadataService metadataService;

    @Autowired
    public ResourcesController(CountingMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @RequestMapping("/")
    public ModelAndView getResources(HttpServletResponse response,
                                     @RequestParam(required = false) MultiValueMap<String, String> parameters) {
        this.addVersionHeader(response);
        IoParameters query = IoParameters.createFromMultiValueMap(parameters);
        // .respectBackwardsCompatibility();
        return new ModelAndView().addObject(createResources(query));
    }

    private ResourceCollection add(String resource, String label, String description) {
        return ResourceCollection.createResource(resource)
                                 .withDescription(description)
                                 .withLabel(label);
    }

    private List<ResourceCollection> createResources(IoParameters parameters) {
        I18N i18n = I18N.getMessageLocalizer(parameters.getLocale());

        ResourceCollection services = add("services", "Service Provider", i18n.get("msg.web.resources.services"));
        // ResourceCollection stations = add("stations", "Station", i18n.get("msg.web.resources.stations"));
        ResourceCollection timeseries = add(TIMESERIES, "Timeseries", i18n.get("msg.web.resources.timeseries"));
        ResourceCollection categories = add("categories", "Category", i18n.get("msg.web.resources.categories"));
        ResourceCollection offerings = add("offerings", "Offering", i18n.get("msg.web.resources.offerings"));
        ResourceCollection features = add("features", "Feature", i18n.get("msg.web.resources.features"));
        ResourceCollection procedures = add("procedures", "Procedure", i18n.get("msg.web.resources.procedures"));
        ResourceCollection phenomena = add("phenomena", "Phenomenon", i18n.get("msg.web.resources.phenomena"));
        if (parameters.isExpanded()) {
            services.setSize(metadataService.getServiceCount(parameters));
            // if (parameters.shallBehaveBackwardsCompatible()) {
            // ensure backwards compatibility
            // stations.setSize(metadataService.getStationCount());
            // timeseries.setSize(metadataService.getTimeseriesCount());
            // }
            categories.setSize(metadataService.getCategoryCount(parameters));
            offerings.setSize(metadataService.getOfferingCount(parameters));
            features.setSize(metadataService.getFeatureCount(parameters));
            procedures.setSize(metadataService.getProcedureCount(parameters));
            phenomena.setSize(metadataService.getPhenomenaCount(parameters));
        }

        List<ResourceCollection> resources = new ArrayList<>();
        resources.add(services);
        // resources.add(stations);
        resources.add(timeseries);
        resources.add(categories);
        resources.add(offerings);
        resources.add(features);
        resources.add(procedures);
        resources.add(phenomena);

        // since 2.0.0
        ResourceCollection platforms = add("platforms", "Platforms", i18n.get("msg.web.resources.platforms"));
        ResourceCollection datasets = add("datasets", "Datasets", i18n.get("msg.web.resources.datasets"));
        ResourceCollection individualObservations = add(INDIVIDUAL_OBSERVATIONS,
                                                        "IndividualObservations",
                                                        i18n.get("msg.web.resources.individualObservations"));
        // ResourceCollection profiles = add("profiles", "Profiles",
        // i18n.get("msg.web.resources.profiles"));
        ResourceCollection trajectories = add(TAJECTORIES, "Trajectories", i18n.get("msg.web.resources.trajectories"));
        // ResourceCollection geometries = add("geometries", "Geometries",
        // i18n.get("msg.web.resources.geometries"));
        resources.add(platforms);
        resources.add(datasets);
        resources.add(individualObservations);
        // resources.add(profiles);
        resources.add(trajectories);

        // resources.add(geometries);
        if (parameters.isExpanded()) {
            platforms.setSize(metadataService.getPlatformCount(parameters));
            datasets.setSize(metadataService.getDatasetCount(parameters));

            List<String> datasetTypes = new LinkedList<>(parameters.getDatasetTypes());
            timeseries.setSize(countDatasets(parameters, TIMESERIES));
            trajectories.setSize(countDatasets(parameters, TAJECTORIES));
            individualObservations.setSize(countDatasets(parameters, INDIVIDUAL_OBSERVATIONS));
            parameters.extendWith(IoParameters.FILTER_DATASET_TYPES, datasetTypes);
        }

        ResourceCollection samplings = add("samplings", "Samplings", i18n.get("msg.web.resources.samplings"));
        ResourceCollection measuringPrograms = add("measuringPrograms",
                                                   "MeasuringPrograms",
                                                   i18n.get("msg.web.resources.measuringPrograms"));
        resources.add(samplings);
        resources.add(measuringPrograms);

        return resources;
    }

    private int countDatasets(IoParameters parameters, String datasetType) {
        String filterName = IoParameters.FILTER_DATASET_TYPES;
        IoParameters filter = parameters.extendWith(filterName, datasetType);
        return metadataService.getDatasetCount(filter);
    }

    private void addVersionHeader(HttpServletResponse response) {
        String implementationVersion = getClass().getPackage()
                                                 .getImplementationVersion();
        String version = implementationVersion != null
                ? implementationVersion
                : "unknown";
        response.addHeader("API-Version", version);
    }

    public static final class ResourceCollection {

        private String id;
        private String label;
        private String description;
        private Integer size;

        private ResourceCollection(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public ResourceCollection withLabel(String name) {
            this.label = name;
            return this;
        }

        public ResourceCollection withDescription(String details) {
            this.description = details;
            return this;
        }

        public ResourceCollection withCount(Integer count) {
            this.size = count;
            return this;
        }

        public static ResourceCollection createResource(String id) {
            return new ResourceCollection(id);
        }
    }
}
