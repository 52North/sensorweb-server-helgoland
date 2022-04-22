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
package org.n52.io.response;

import java.util.Map;
import java.util.Objects;

import org.n52.io.Utils;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ServiceOutput extends ParameterOutput {

    public static final String COLLECTION_PATH = "services";

    public static final String SERVICE_URL = "serviceUrl";
    public static final String VERSION = "version";
    public static final String TYPE = "type";
    public static final String FEATURES = "features";
    public static final String QUANTITIES = "quantities";
    public static final String SUPPORTS_FIRST_LATEST = "supportsFirstLatest";
    public static final String SUPPORTED_MIME_TYPES = "supportedMimeTypes";

    private OptionalOutput<String> serviceUrl;

    private OptionalOutput<String> version;

    private OptionalOutput<String> type;

    private OptionalOutput<Map<String, Object>> features;

    @Override
    public String getCollectionName() {
        return COLLECTION_PATH;
    }

    public String getServiceUrl() {
        return getIfSerialized(serviceUrl);
    }

    public void setServiceUrl(OptionalOutput<String> serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getVersion() {
        return getIfSerialized(version);
    }

    public void setVersion(OptionalOutput<String> version) {
        this.version = version;
    }

    public String getType() {
        return getIfSerialized(type);
    }

    public void setType(OptionalOutput<String> type) {
        this.type = type;
    }

    // public void setSupportedDatasets(Map<String, Set<String>> mimeTypesByDatasetTypes) {
    // for (Set<String> supportedMimeTypes : mimeTypesByDatasetTypes.values()) {
    // supportedMimeTypes.add(MimeType.APPLICATION_JSON.getMimeType());
    // }
    // addFeature("supportedMimeTypes", mimeTypesByDatasetTypes);
    // }

    @JsonAnyGetter
    public Map<String, Object> getFeatures() {
        return getIfSerializedMap(features);
    }

    public void setFeatures(OptionalOutput<Map<String, Object>> features) {
        this.features = features;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceUrl, version, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ServiceOutput)) {
            return false;
        } else {
            ServiceOutput other = (ServiceOutput) obj;
            return Objects.equals(serviceUrl, other.serviceUrl)
                    && Objects.equals(version, other.version)
                    && Objects.equals(type, other.type)
                    && super.equals(other);
        }
    }

    @JsonPropertyOrder({ "features", "phenomena", "procedures", "categories", "platforms", "offerings", "tags",
            "measuringPrograms", "samplings" })
    public static class ParameterCount {

        private String[] selectedPlatformTypes;

        private Long amountOfferings;

        private Long amountFeatures;

        private Long amountProcedures;

        private Long amountPhenomena;

        private Long amountCategories;

        private Long amountPlatforms;

        private DatasetCount amountDatasets;

        /**
         * @deprecated since 2.0.0
         */
        @Deprecated
        private Long amountStations;

        private Long amountTimeseries;

        private Long amountSamplings;

        private Long amountMeasuringPrograms;

        private Long amountTags;

        public String[] getSelectedPlatformTypes() {
            return Utils.copy(selectedPlatformTypes);
        }

        public void setSelectedPlatformTypes(String... selectedPlatformTypes) {
            this.selectedPlatformTypes = selectedPlatformTypes;
        }

        public Long getOfferings() {
            return amountOfferings;
        }

        public void setOfferingsSize(Long size) {
            this.amountOfferings = size;
        }

        public Long getFeatures() {
            return amountFeatures;
        }

        public void setFeaturesSize(Long size) {
            this.amountFeatures = size;
        }

        public Long getProcedures() {
            return amountProcedures;
        }

        public void setProceduresSize(Long size) {
            this.amountProcedures = size;
        }

        public Long getPhenomena() {
            return amountPhenomena;
        }

        public void setPhenomenaSize(Long size) {
            this.amountPhenomena = size;
        }

        public Long getCategories() {
            return amountCategories;
        }

        public void setCategoriesSize(Long size) {
            this.amountCategories = size;
        }

        public Long getPlatforms() {
            return amountPlatforms;
        }

        public void setPlatformsSize(Long size) {
            this.amountPlatforms = size;
        }

        public DatasetCount getDatasets() {
            return amountDatasets;
        }

        public void setDatasets(DatasetCount datasets) {
            this.amountDatasets = datasets;
        }

        @Deprecated
        public Long getStations() {
            return amountStations;
        }

        @Deprecated
        public void setStationsSize(int size) {
            this.amountStations = Integer.toUnsignedLong(size);
        }

        public void setTimeseriesSize(Long countTimeseries) {
            this.amountTimeseries = countTimeseries;
        }

        public Long getTimeseries() {
            return this.amountTimeseries;
        }

        public void setSamplingsSize(Long countSamplings) {
            this.amountSamplings = countSamplings;
        }

        public Long getSamplings() {
            return this.amountSamplings;
        }

        public void setMeasuringProgramsSize(Long countMeasuringPrograms) {
            this.amountMeasuringPrograms = countMeasuringPrograms;
        }

        public Long getMeasuringPrograms() {
            return this.amountMeasuringPrograms;
        }

        public void setTagsSize(Long countTags) {
            this.amountTags = countTags;
        }

        public Long getTags() {
            return this.amountTags;
        }
    }

    @JsonPropertyOrder({ "total", "timeseries", "individualObservations", "trajectories", "profiles" })
    public static class DatasetCount {

        private Long totalAmount = 0L;

        private Long amountIndividualObservations = 0L;

        private Long amountTimeseries = 0L;

        private Long amountProfiles = 0L;

        private Long amountTrajectories = 0L;

        public Long getTotal() {
            return totalAmount;
        }

        public void setTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
        }

        public Long getIndividualObservations() {
            return amountIndividualObservations;
        }

        public void setAmountIndividualObservations(Long amountIndividualObservations) {
            this.amountIndividualObservations = amountIndividualObservations;
        }

        public Long getTimeseries() {
            return amountTimeseries;
        }

        public void setAmountTimeseries(Long amountTimeseries) {
            this.amountTimeseries = amountTimeseries;
        }

        public Long getProfiles() {
            return amountProfiles;
        }

        public void setAmountProfiles(Long amountProfiles) {
            this.amountProfiles = amountProfiles;
        }

        public Long getTrajectories() {
            return amountTrajectories;
        }

        public void setAmountTrajectories(Long amountTrajectories) {
            this.amountTrajectories = amountTrajectories;
        }

    }

}
