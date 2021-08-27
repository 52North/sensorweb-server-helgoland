/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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

    private static final String ALL = "all";

    private final IoParameters parameters;

    public FilterResolver(IoParameters parameters) {
        this.parameters = parameters;
    }

    boolean shallBehaveBackwardsCompatible() {
        return false;
        // return !(isSetPlatformTypeFilter() || isSetDatasetTypeFilter() ||
        // isSetValueTypeFilter() || isSetObservationTypeFilter());
    }

    // public boolean shallIncludeMobileDatasets() {
    // return hasMobileFilter() ? isSetMobileFilter() : shallIncludeAllDatasets();
    // }
    //
    // public boolean shallIncludeInsituDatasets() {
    // return hasInsituFilter() ? isSetInsituFilter() : shallIncludeAllDatasets();
    // }

    public boolean hasMobileFilter() {
        return parameters.getMobile() != null;
    }

    public boolean hasInsituFilter() {
        return parameters.getInsitu() != null;
    }

    public boolean isMobileFilter() {
        return hasMobileFilter() && Boolean.parseBoolean(parameters.getMobile());
    }

    public boolean isInsituFilter() {
        return hasInsituFilter() && Boolean.parseBoolean(parameters.getInsitu());
    }

    public boolean shallIncludeAllDatasets() {
        return !hasMobileFilter() && !hasInsituFilter();
    }

    public boolean isSetPlatformTypeFilter() {
        return !getDatasets().isEmpty();
    }

    // private boolean shallIncludeAllPlatformGeometries() {
    // return !isSetPlatformGeometriesFilter()
    // || getPlatformGeometryTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    // }
    //
    // private boolean isSetPlatformGeometriesFilter() {
    // return !getPlatformGeometryTypes().isEmpty();
    // }
    //
    // private boolean shallIncludeAllObservedGeometries() {
    // return !isSetObservedGeometriesFilter()
    // || getObservedGeometryTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    // }
    //
    // private boolean isSetObservedGeometriesFilter() {
    // return !getObservedGeometryTypes().isEmpty();
    // }
    //
    // public boolean shallIncludePlatformGeometriesSite() {
    // return shallIncludeAllPlatformGeometries()
    // && shallIncludeAllDatasets()
    // && !isSetObservedGeometriesFilter()
    // && !isSetMobileFilter()
    // && !isSetRemoteFilter()
    // || isSetStationaryFilter()
    // || isSetInsituFilter()
    // || isSetSiteFilter();
    // }
    //
    // private boolean isSetSiteFilter() {
    // return getPlatformGeometryTypes().contains("site");
    // }
    //
    // public boolean shallIncludePlatformGeometriesTrack() {
    // return shallIncludeAllPlatformGeometries()
    // && shallIncludeAllDatasets()
    // && !isSetObservedGeometriesFilter()
    // && !isSetStationaryFilter()
    // && !isSetRemoteFilter()
    // || isSetMobileFilter()
    // || isSetInsituFilter()
    // || isSetTrackFilter();
    // }
    //
    // private boolean isSetTrackFilter() {
    // return getPlatformGeometryTypes().contains("track");
    // }
    //
    // public boolean shallIncludeObservedGeometriesStatic() {
    // return shallIncludeAllObservedGeometries()
    // && shallIncludeAllDatasets()
    // || isSetStaticFilter()
    // || !isSetDynamicFilter()
    // && !isSetInsituFilter();
    // }
    //
    // private boolean isSetStaticFilter() {
    // return getObservedGeometryTypes().contains("static")
    // || getObservedGeometryTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    // }
    //
    // public boolean shallIncludeObservedGeometriesDynamic() {
    // return shallIncludeAllObservedGeometries()
    // && shallIncludeAllDatasets()
    // || isSetDynamicFilter()
    // || !isSetStaticFilter()
    // && !isSetInsituFilter();
    // }
    //
    // private boolean isSetDynamicFilter() {
    // return getObservedGeometryTypes().contains("dynamic")
    // || getObservedGeometryTypes().contains(PlatformType.PLATFORM_TYPE_ALL);
    // }

    @Deprecated
    private Set<String> getDatasets() {
        return parameters.getDatasets();
    }

    private Set<String> getObservedGeometryTypes() {
        return parameters.getObservedGeometryTypes();
    }

    private Set<String> getPlatformGeometryTypes() {
        return parameters.getPlatformGeometryTypes();
    }

    public boolean shallIncludeAllDatasetTypes() {
        Set<String> types = parameters.getDatasetTypes();
        return !isSetDatasetTypeFilter() && !isSetObservationTypeFilter() && !isSetValueTypeFilter()
                || types.contains(ALL);
    }

    public boolean shallIncludeDatasetType(String datasetType) {
        Set<String> datasetTypes = parameters.getDatasetTypes();
        return datasetTypes.contains(datasetType)
                || datasetTypes.contains(ALL);
    }

    public boolean shallIncludeAllObservationTypes() {
        Set<String> types = parameters.getObservationTypes();
        return !isSetObservationTypeFilter()
                || types.contains(ALL);
    }

    public boolean shallIncludeObservationType(String observationType) {
        Set<String> observationTypes = parameters.getObservationTypes();
        return observationTypes.contains(observationType)
                || observationTypes.contains(ALL);
    }

    public boolean shallIncludeAllValueTypes() {
        Set<String> types = parameters.getValueTypes();
        return !isSetValueTypeFilter()
                || types.contains(ALL);
    }

    public boolean shallIncludeValueType(String valueType) {
        Set<String> valueTypes = parameters.getValueTypes();
        return valueTypes.contains(valueType)
                || valueTypes.contains(ALL);
    }

    private boolean isSetDatasetTypeFilter() {
        return !parameters.getDatasetTypes()
                          .isEmpty();
    }

    private boolean isSetObservationTypeFilter() {
        return !parameters.getObservationTypes()
                          .isEmpty();
    }

    private boolean isSetValueTypeFilter() {
        return !parameters.getValueTypes()
                          .isEmpty();
    }

}
