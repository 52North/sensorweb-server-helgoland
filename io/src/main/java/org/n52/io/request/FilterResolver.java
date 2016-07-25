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

package org.n52.io.request;

import java.util.Set;

public class FilterResolver {

    private final IoParameters parameters;

    public FilterResolver(IoParameters parameters) {
        this.parameters = parameters;
    }

    public boolean shallIncludeMobilePlatformTypes() {
        return shallIncludeAllPlatformTypes()
                || isSetMobileFilter()
                || !isSetStationaryFilter();
    }

    public boolean shallIncludeStationaryPlatformTypes() {
        return shallIncludeAllPlatformTypes()
                || isSetStationaryFilter()
                || !isSetMobileFilter();
    }

    public boolean shallIncludeInsituPlatformTypes() {
        return shallIncludeAllPlatformTypes()
                || isSetInsituFilter()
                || !isSetRemoteFilter();
    }

    public boolean shallIncludeRemotePlatformTypes() {
        return shallIncludeAllPlatformTypes()
                || isSetRemoteFilter()
                || !isSetInsituFilter();
    }

    public boolean isSetStationaryFilter() {
        return getPlatformTypes().contains("stationary")
                || getPlatformTypes().contains("all");
    }

    public boolean isSetMobileFilter() {
        return getPlatformTypes().contains("mobile")
                || getPlatformTypes().contains("all");
    }

    public boolean isSetInsituFilter() {
        return getPlatformTypes().contains("insitu")
                || getPlatformTypes().contains("all");
    }

    public boolean isSetRemoteFilter() {
        return getPlatformTypes().contains("remote")
                || getPlatformTypes().contains("all");
    }

    public boolean shallIncludeAllPlatformTypes() {
        return !isSetPlatformTypeFilter()
                || getPlatformTypes().contains("all");
    }

    public boolean isSetPlatformTypeFilter() {
        return !getPlatformTypes().isEmpty();
    }

    private boolean shallIncludeAllPlatformGeometries() {
        return !isSetPlatformGeometriesFilter()
                || getPlatformGeometryTypes().contains("all");
    }

    private boolean isSetPlatformGeometriesFilter() {
        return !getPlatformGeometryTypes().isEmpty();
    }

    private boolean shallIncludeAllObservedGeometries() {
        return !isSetObservedGeometriesFilter()
                || getObservedGeometryTypes().contains("all");
    }

    private boolean isSetObservedGeometriesFilter() {
        return !getObservedGeometryTypes().isEmpty();
    }

    public boolean shallIncludePlatformGeometriesSite() {
        return shallIncludeAllPlatformGeometries()
                && shallIncludeAllPlatformTypes()
                && !isSetObservedGeometriesFilter()
                && !isSetMobileFilter()
                && !isSetRemoteFilter()
                || isSetStationaryFilter()
                || isSetInsituFilter()
                || isSetSiteFilter();
    }

    private boolean isSetSiteFilter() {
        return getPlatformGeometryTypes().contains("site");
    }

    public boolean shallIncludePlatformGeometriesTrack() {
        return shallIncludeAllPlatformGeometries()
                && shallIncludeAllPlatformTypes()
                && !isSetObservedGeometriesFilter()
                && !isSetStationaryFilter()
                && !isSetRemoteFilter()
                || isSetMobileFilter()
                || isSetInsituFilter()
                || isSetTrackFilter();
    }

    private boolean isSetTrackFilter() {
        return getPlatformGeometryTypes().contains("track");
    }

    public boolean shallIncludeObservedGeometriesStatic() {
        return shallIncludeAllObservedGeometries()
                && shallIncludeAllPlatformTypes()
                || isSetStaticFilter()
                || !isSetDynamicFilter()
                        && !isSetInsituFilter();
    }

    private boolean isSetStaticFilter() {
        return getObservedGeometryTypes().contains("static")
                || getObservedGeometryTypes().contains("all");
    }

    public boolean shallIncludeObservedGeometriesDynamic() {
        return shallIncludeAllObservedGeometries()
                && shallIncludeAllPlatformTypes()
                || isSetDynamicFilter()
                || !isSetStaticFilter()
                        && !isSetInsituFilter();
    }

    private boolean isSetDynamicFilter() {
        return getObservedGeometryTypes().contains("dynamic")
                || getObservedGeometryTypes().contains("all");
    }

    private Set<String> getPlatformTypes() {
        return parameters.getPlatformTypes();
    }

    private Set<String> getObservedGeometryTypes() {
        return parameters.getObservedGeometryTypes();
    }

    private Set<String> getPlatformGeometryTypes() {
        return parameters.getPlatformGeometryTypes();
    }
    
    public boolean shallIncludeAllDatasetTypes() {
        Set<String> datasetTypes = parameters.getDatasetTypes();
        return !isSetDatasetTypeFilter()
                || datasetTypes.contains("all");
    }

    public boolean shallIncludeDatasetType(String datasetType) {
        Set<String> datasetTypes = parameters.getDatasetTypes();
        return datasetTypes.contains(datasetType)
                || datasetTypes.contains("all");
    }

    private boolean isSetDatasetTypeFilter() {
        return !parameters.getDatasetTypes().isEmpty();
    }

}
