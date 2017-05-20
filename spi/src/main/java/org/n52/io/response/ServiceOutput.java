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
package org.n52.io.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.n52.io.MimeType;
import org.n52.io.Utils;

public class ServiceOutput extends ParameterOutput {

    private String serviceUrl;

    private String version;

    private String type;

    private Map<String, Object> features;

    private ParameterCount quantities;

    /**
     * @deprecated since 2.0.0
     */
    @Deprecated
    private Boolean supportsFirstLatest;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addSupportedDatasets(Map<String, Set<String>> mimeTypesByDatasetTypes) {
        for (Set<String> supportedMimeTypes : mimeTypesByDatasetTypes.values()) {
            supportedMimeTypes.add(MimeType.APPLICATION_JSON.getMimeType());
        }
        addFeature("supportedMimeTypes", mimeTypesByDatasetTypes);
    }

    @JsonAnyGetter
    public Map<String, Object> getFeatures() {
        return features != null
                ? Collections.unmodifiableMap(features)
                : null;
    }

    public void setFeatures(Map<String, Object> features) {
        this.features = features;
    }

    public void addFeature(String featureName, Object featureInfo) {
        if (features == null) {
            features = new HashMap<>();
        }
        features.put(featureName, featureInfo);
    }

    /**
     * @return if service supports first and latest values
     * @deprecated since 2.0.0, {@link #features} get serialized instead
     */
    @JsonIgnore
    @Deprecated
    public Boolean isSupportsFirstLatest() {
        return supportsFirstLatest;
    }

    public void setSupportsFirstLatest(Boolean supportsFirstLatest) {
        addFeature("supportsFirstLatest", supportsFirstLatest);
        this.supportsFirstLatest = supportsFirstLatest;
    }

    /**
     * @return the parameter count
     */
    @JsonIgnore
    public ParameterCount getQuantities() {
        return quantities;
    }

    /**
     * @param countedParameters the parameter count object
     */
    public void setQuantities(ParameterCount countedParameters) {
        addFeature("quantities", countedParameters);
        this.quantities = countedParameters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((serviceUrl == null) ? 0 : serviceUrl.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof ServiceOutput)) {
            return false;
        }
        ServiceOutput other = (ServiceOutput) obj;
        if (serviceUrl == null) {
            if (other.serviceUrl != null) {
                return false;
            }
        } else if (!serviceUrl.equals(other.serviceUrl)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    public static class ParameterCount {

        private String[] selectedPlatformTypes;

        private Integer amountOfferings;

        private Integer amountFeatures;

        private Integer amountProcedures;

        private Integer amountPhenomena;

        private Integer amountCategories;

        private Integer amountPlatforms;

        private Integer amountDatasets;

        /**
         * @deprecated  since 2.0.0
         */
        @Deprecated
        private Integer amountStations;

        /**
         * @deprecated  since 2.0.0
         */
        @Deprecated
        private Integer amountTimeseries;

        public String[] getSelectedPlatformTypes() {
            return Utils.copy(selectedPlatformTypes);
        }

        public void setSelectedPlatformTypes(String... selectedPlatformTypes) {
            this.selectedPlatformTypes = selectedPlatformTypes;
        }

        public Integer getOfferings() {
            return amountOfferings;
        }

        public void setOfferingsSize(Integer size) {
            this.amountOfferings = size;
        }

        public Integer getFeatures() {
            return amountFeatures;
        }

        public void setFeaturesSize(Integer size) {
            this.amountFeatures = size;
        }

        public Integer getProcedures() {
            return amountProcedures;
        }

        public void setProceduresSize(Integer size) {
            this.amountProcedures = size;
        }

        public Integer getPhenomena() {
            return amountPhenomena;
        }

        public void setPhenomenaSize(Integer size) {
            this.amountPhenomena = size;
        }

        public Integer getCategories() {
            return amountCategories;
        }

        public void setCategoriesSize(Integer size) {
            this.amountCategories = size;
        }

        public Integer getPlatforms() {
            return amountPlatforms;
        }

        public void setPlatformsSize(Integer size) {
            this.amountPlatforms = size;
        }

        public Integer getDatasets() {
            return amountDatasets;
        }

        public void setDatasetsSize(Integer size) {
            this.amountDatasets = size;
        }

        @Deprecated
        public Integer getStations() {
            return amountStations;
        }

        @Deprecated
        public void setStationsSize(int size) {
            this.amountStations = size;
        }

        @Deprecated
        public void setTimeseriesSize(int countTimeseries) {
            this.amountTimeseries = countTimeseries;
        }

        @Deprecated
        public Integer getTimeseries() {
            return this.amountTimeseries;
        }
    }

}
