/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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

import org.n52.io.response.PlatformType;

public class FilterResolver {

    private final IoParameters parameters;

    public FilterResolver(IoParameters parameters) {
        this.parameters = parameters;
    }

    boolean shallBehaveBackwardsCompatible() {
        return !(isSetPlatformTypeFilter() || isSetDatasetTypeFilter());
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
        return getPlatformTypes().contains(PlatformType.PLATFORM_TYPE_STATIONARY)
                || getPlatformTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    }

    public boolean isSetMobileFilter() {
        return getPlatformTypes().contains(PlatformType.PLATFORM_TYPE_MOBILE)
                || getPlatformTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    }

    public boolean isSetInsituFilter() {
        return getPlatformTypes().contains(PlatformType.PLATFORM_TYPE_INSITU)
                || getPlatformTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    }

    public boolean isSetRemoteFilter() {
        return getPlatformTypes().contains(PlatformType.PLATFORM_TYPE_REMOTE)
                || getPlatformTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    }

    public boolean shallIncludeAllPlatformTypes() {
        return !isSetPlatformTypeFilter()
                || getPlatformTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    }

    public boolean isSetPlatformTypeFilter() {
        return !getPlatformTypes().isEmpty();
    }

    private boolean shallIncludeAllPlatformGeometries() {
        return !isSetPlatformGeometriesFilter()
                || getPlatformGeometryTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    }

    private boolean isSetPlatformGeometriesFilter() {
        return !getPlatformGeometryTypes().isEmpty();
    }

    private boolean shallIncludeAllObservedGeometries() {
        return !isSetObservedGeometriesFilter()
                || getObservedGeometryTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
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
                || getObservedGeometryTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
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
                || getObservedGeometryTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
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
        Set<String> datasetTypes = parameters.getValueTypes();
        return !isSetDatasetTypeFilter()
                || datasetTypes.contains(PlatformType.PLATFORM_TYPE_ALL);
    }

    public boolean shallIncludeDatasetType(String datasetType) {
        Set<String> datasetTypes = parameters.getValueTypes();
        return datasetTypes.contains(datasetType)
                || datasetTypes.contains(PlatformType.PLATFORM_TYPE_ALL);
    }

    private boolean isSetDatasetTypeFilter() {
        return !parameters.getValueTypes()
                          .isEmpty();
    }

}
