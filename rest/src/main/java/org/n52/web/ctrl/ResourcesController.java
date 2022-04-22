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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.n52.io.HrefHelper;
import org.n52.io.I18N;
import org.n52.io.request.IoParameters;
import org.n52.series.spi.srv.CountingMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@RestController
@RequestMapping(value = UrlSettings.BASE, produces = { "application/json" })
public class ResourcesController extends BaseController implements ResoureControllerConstants {

    private final CountingMetadataService metadataService;
    private Collection<ParameterRequestMappingAdapter<?>> parameterController;

    @Autowired
    @SuppressFBWarnings({ "EI_EXPOSE_REP2" })
    public ResourcesController(CountingMetadataService metadataService,
            Collection<ParameterRequestMappingAdapter<?>> parameterController) {
        this.metadataService = metadataService;
        this.parameterController = parameterController;
    }

    @RequestMapping("/")
    public ModelAndView getResources(HttpServletResponse response,
            @RequestParam(required = false) MultiValueMap<String, String> parameters) {
        this.addVersionHeader(response);
        IoParameters query = IoParameters.createFromMultiValueMap(addAdditionalParameter(parameters));
        return new ModelAndView().addObject(createResources(query));
    }

    private ResourceCollection add(String resource, String label, String description, IoParameters parameters) {
        return ResourceCollection.createResource(resource).withDescription(description).withLabel(label)
                .withHref(HrefHelper.constructHref(parameters.getHrefBase(), resource));
    }

    private Set<ResourceCollection> createResources(IoParameters parameters) {
        I18N i18n = I18N.getMessageLocalizer(parameters.getLocale());
        if (parameterController == null || parameterController.isEmpty()) {
            return createStaticResources(parameters, i18n);
        }
        return createDynamicResources(parameters, i18n);
    }

    private Set<ResourceCollection> createDynamicResources(IoParameters parameters, I18N i18n) {
        return parameterController.stream().filter(p -> p.getResource() != null && !p.getResource().isEmpty())
                .map(p -> p.getResourceCollection(i18n, parameters)).collect(Collectors.toCollection(TreeSet::new));
    }

    private Set<ResourceCollection> createStaticResources(IoParameters parameters, I18N i18n) {
        ResourceCollection services =
                add(RESOURCE_SERVICES, LABEL_SERVICES, i18n.get(DESCRIPTION_KEY_SERVICES), parameters);
        ResourceCollection categories =
                add(RESOURCE_CATEGORIES, LABEL_CATEGORIES, i18n.get(DESCRIPTION_KEY_CATEGORIES), parameters);
        ResourceCollection offerings =
                add(RESOURCE_OFFERINGS, LABEL_OFFERINGS, i18n.get(DESCRIPTION_KEY_OFFERINGS), parameters);
        ResourceCollection features =
                add(RESOURCE_FEATURES, LABEL_FEATURES, i18n.get(DESCRIPTION_KEY_FEATURES), parameters);
        ResourceCollection procedures =
                add(RESOURCE_PROCEDURES, LABEL_PROCEDURES, i18n.get(DESCRIPTION_KEY_PROCEDURES), parameters);
        ResourceCollection phenomena =
                add(RESOURCE_PHENOMENA, LABEL_PHENOMENA, i18n.get(DESCRIPTION_KEY_PHENOMENA), parameters);
        ResourceCollection platforms =
                add(RESOURCE_PLATFORMS, LABEL_PLATFORMS, i18n.get(DESCRIPTION_KEY_PLATFORMS), parameters);
        ResourceCollection datasets =
                add(RESOURCE_DATASETS, LABEL_DATASETS, i18n.get(DESCRIPTION_KEY_DATASETS), parameters);
        ResourceCollection timeseries =
                add(RESOURCE_TIMESERIES, LABEL_TIMESERIES, i18n.get(DESCRIPTION_KEY_TIMESERIES), parameters);
        ResourceCollection individualObservations = add(RESOURCE_INDIVIDUAL_OBSERVATIONS,
                LABEL_INDIVIDUAL_OBSERVATIONS, i18n.get(DESCRIPTION_KEY_TIMESERIES), parameters);
        ResourceCollection trajectories =
                add(RESOURCE_TRAJECTORIES, LABEL_TRAJECTORIES, i18n.get(DESCRIPTION_KEY_TRAJECTORIES), parameters);
        ResourceCollection samplings =
                add(RESOURCE_SAMPLINGS, LABEL_SAMPLINGS, i18n.get(DESCRIPTION_KEY_SAMPLINGS), parameters);
        ResourceCollection measuringPrograms = add(RESOURCE_MEASURING_PROGRAMS, LABEL_MEASURING_PROGRAMS,
                i18n.get(DESCRIPTION_KEY_MEASURING_PROGRAMS), parameters);
        ResourceCollection tags = add(RESOURCE_TAGS, LABEL_TAGS, i18n.get(DESCRIPTION_KEY_TAGS), parameters);
        if (parameters.isExpanded()) {
            services.setSize(metadataService.getServiceCount(parameters));
            categories.setSize(metadataService.getCategoryCount(parameters));
            offerings.setSize(metadataService.getOfferingCount(parameters));
            features.setSize(metadataService.getFeatureCount(parameters));
            procedures.setSize(metadataService.getProcedureCount(parameters));
            phenomena.setSize(metadataService.getPhenomenaCount(parameters));
            platforms.setSize(metadataService.getPlatformCount(parameters));
            datasets.setSize(metadataService.getDatasetCount(parameters));
            List<String> datasetTypes = new LinkedList<>(parameters.getDatasetTypes());
            timeseries.setSize(countDatasets(parameters, RESOURCE_TIMESERIES));
            trajectories.setSize(countDatasets(parameters, RESOURCE_TRAJECTORIES));
            individualObservations.setSize(countDatasets(parameters, RESOURCE_INDIVIDUAL_OBSERVATIONS));
            parameters.extendWith(IoParameters.FILTER_DATASET_TYPES, datasetTypes);
            samplings.setSize(metadataService.getSamplingCounter(parameters));
            measuringPrograms.setSize(metadataService.getMeasuringProgramCounter(parameters));
            tags.setSize(metadataService.getTagCounter(parameters));
        }
        Set<ResourceCollection> resources = new TreeSet<>();
        resources.add(services);
        resources.add(timeseries);
        resources.add(categories);
        resources.add(offerings);
        resources.add(features);
        resources.add(procedures);
        resources.add(phenomena);
        resources.add(platforms);
        resources.add(datasets);
        resources.add(individualObservations);
        resources.add(trajectories);
        resources.add(samplings);
        resources.add(measuringPrograms);
        resources.add(tags);

        return resources;
    }

    private Long countDatasets(IoParameters parameters, String datasetType) {
        String filterName = IoParameters.FILTER_DATASET_TYPES;
        IoParameters filter = parameters.extendWith(filterName, datasetType);
        return metadataService.getDatasetCount(filter);
    }

    private void addVersionHeader(HttpServletResponse response) {
        String implementationVersion = getClass().getPackage().getImplementationVersion();
        String version = implementationVersion != null ? implementationVersion : "unknown";
        response.addHeader("API-Version", version);
    }

    @Override
    protected void addCacheHeader(IoParameters parameter, HttpServletResponse response) {
        addCacheHeader(response, 1440);
    }

    public static final class ResourceCollection implements Comparable<ResourceCollection> {

        private String id;
        private String label;
        private String description;
        private String href;

        private Long size;

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

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Integer size) {
            setSize(size.longValue());
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public ResourceCollection withLabel(String name) {
            setLabel(name);
            return this;
        }

        public ResourceCollection withDescription(String details) {
            setDescription(details);
            return this;
        }

        public ResourceCollection withHref(String href) {
            setHref(href);
            return this;
        }

        public ResourceCollection withCount(Integer count) {
            return withCount(count.longValue());
        }

        public ResourceCollection withCount(Long count) {
            setSize(count);
            return this;
        }

        public static ResourceCollection createResource(String id) {
            return new ResourceCollection(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ResourceCollection) {
                return getId().equals(((ResourceCollection) obj).getId());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getLabel());
        }

        @Override
        public int compareTo(ResourceCollection o) {
            return getId().compareTo(o.getId());
        }

    }
}
