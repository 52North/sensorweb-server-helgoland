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
package org.n52.io.request;

import static org.n52.io.request.IoParameters.createDefaults;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilterResolverTest {

    private FilterResolver createResolver(IoParameters resolver) {
        return new FilterResolver(resolver);
    }

//    @Test
//    public void when_defaults_then_behaveBackwardsCompatible() {
//        FilterResolver resolver = createResolver(createDefaults());
//        assertTrue(resolver.shallBehaveBackwardsCompatible());
//    }
//
//    @Test
//    public void when_setDatasetTypeFilter_then_dontBehaveBackwardsCompatible() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_DATASET_TYPES, "blah");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertFalse(resolver.shallBehaveBackwardsCompatible());
//    }
//
//    @Test
//    public void when_setValueTypeFiltre_then_dontBehaveBackwardsCompatible() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_VALUE_TYPES, "blah");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertFalse(resolver.shallBehaveBackwardsCompatible());
//    }
//
//    @Test
//    public void when_defaults_then_allPlatformGeometryFiltersActive() {
//        FilterResolver resolver = createResolver(createDefaults());
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesSite());
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
//    }
//
//    @Test
//    public void when_defaults_then_allObservedGeometryFiltersActive() {
//        FilterResolver resolver = createResolver(createDefaults());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
//    }
//
//    @Test
//    public void when_stationaryPlatformsFilter_then_sitesAndAndRemoteFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_TYPES, "stationary");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesSite());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
//    }
//
//    @Test
//    public void when_mobilePlatformsFilter_then_tracksAndRemoteFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_TYPES, "mobile");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesSite());
//    }
//
//    @Test
//    public void when_insituPlatformsFilter_then_sitesAndtracksFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_TYPES, "insitu");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesSite());
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
//        Assertions.assertFalse(resolver.shallIncludeObservedGeometriesStatic());
//        Assertions.assertFalse(resolver.shallIncludeObservedGeometriesDynamic());
//    }
//
//    @Test
//    public void when_remotePlatformsFilter_then_allGeometriesFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_TYPES, "remote");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesSite());
//    }
//
//    @Test
//    public void when_allObservedGeometries_then_allObservedGeometryFiltersActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "all");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
//    }
//
//    @Test
//    public void when_allPlatformGeometries_then_allPlatformGeometryFiltersActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "all");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesSite());
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
//    }
//
//    @Test
//    public void when_allPlatformsRemoteGeometries_then_allGeometriesFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_TYPES, "remote")
//                                                  .extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "all");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesSite());
//    }
//
//    @Test
//    public void when_allGeometriesStationaryPlatforms_then_allSitesFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "all")
//                                                  .extendWith(Parameters.FILTER_PLATFORM_TYPES, "stationary");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesSite());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
//    }
//
//    @Test
//    public void when_allGeometriesMobilePlatforms_then_allTracksFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_TYPES, "mobile")
//                                                  .extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "all");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesSite());
//    }
//
//    @Test
//    public void when_sitePlatformGeometries_then_siteFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "site");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesSite());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
//    }
//
//    @Test
//    public void when_trackPlatformGeometries_then_trackFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "track");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
//    }
//
//    @Test
//    public void when_trackPlatformGeometries_then_siteFilterInactive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "track");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesSite());
//    }
//
//    @Test
//    public void when_staticObservedGeometries_then_staticFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "static");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
//        Assertions.assertFalse(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesSite());
//    }
//
//    @Test
//    public void when_dynamicObservedGeometries_then_dynamicFilterActive() {
//        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "dynamic");
//        FilterResolver resolver = createResolver(parameters);
//        Assertions.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
//        Assertions.assertFalse(resolver.shallIncludeObservedGeometriesStatic());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
//        Assertions.assertFalse(resolver.shallIncludePlatformGeometriesSite());
//    }

    @Test
    public void when_defaults_then_insituFilterActive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assertions.assertFalse(resolver.hasInsituFilter());
    }

    @Test
    public void when_defaults_then_stationaryFilterActive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assertions.assertFalse(resolver.hasMobileFilter());
    }

    @Test
    public void when_defaults_then_remoteFilterActive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assertions.assertFalse(resolver.hasInsituFilter());
    }

    @Test
    public void when_defaults_then_mobileFilterActive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assertions.assertFalse(resolver.hasMobileFilter());
    }

    @Test
    public void when_setMobile_then_allMobilesFilterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_MOBILE, "true");
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertTrue(resolver.hasMobileFilter());
        Assertions.assertTrue(resolver.isMobileFilter());
        Assertions.assertFalse(resolver.hasInsituFilter());
    }

    @Test
    public void when_setRemote_then_allRemotesFilterActive() {
        FilterResolver resolver = createResolver(createDefaults().extendWith(Parameters.FILTER_INSITU, "false"));
        Assertions.assertTrue(resolver.hasInsituFilter());
        Assertions.assertFalse(resolver.isInsituFilter());
    }

    @Test
    public void when_setMobileRemote_then_allMobileRemotesFilterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_MOBILE, "true")
                .extendWith(Parameters.FILTER_INSITU, "false");
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertTrue(resolver.hasMobileFilter());
        Assertions.assertTrue(resolver.isMobileFilter());
        Assertions.assertTrue(resolver.hasInsituFilter());
        Assertions.assertFalse(resolver.isInsituFilter());
    }

    @Test
    public void when_setMobileInsitu_then_allMobileInsitusFilterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_MOBILE, "true")
                .extendWith(Parameters.FILTER_INSITU, "true");
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertTrue(resolver.hasMobileFilter());
        Assertions.assertTrue(resolver.isMobileFilter());
        Assertions.assertTrue(resolver.hasInsituFilter());
        Assertions.assertTrue(resolver.isInsituFilter());
    }

    @Test
    public void when_setStationaryInsitu_then_allStationaryInsitusFilterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_MOBILE, "false")
                .extendWith(Parameters.FILTER_INSITU, "true");
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertTrue(resolver.hasMobileFilter());
        Assertions.assertFalse(resolver.isMobileFilter());
        Assertions.assertTrue(resolver.hasInsituFilter());
        Assertions.assertTrue(resolver.isInsituFilter());
    }

    @Test
    public void when_setStationaryRemote_then_allStationaryRemotesFilterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_MOBILE, "false")
                .extendWith(Parameters.FILTER_INSITU, "false");
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertTrue(resolver.hasMobileFilter());
        Assertions.assertFalse(resolver.isMobileFilter());
        Assertions.assertTrue(resolver.hasInsituFilter());
        Assertions.assertFalse(resolver.isInsituFilter());
    }

    @Test
    public void when_setAllDatasets_then_insituFilterActive() {
        IoParameters parameters = createDefaults();
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertFalse(resolver.hasInsituFilter());
    }

    @Test
    public void when_setAllDatasets_then_stationaryFilterActive() {
        IoParameters parameters = createDefaults();
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertFalse(resolver.hasMobileFilter());
    }

    @Test
    public void when_setAllDatasets_then_remoteFilterActive() {
        IoParameters parameters = createDefaults();
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertFalse(resolver.hasInsituFilter());
    }

    @Test
    public void when_setAllDatasets_then_mobileFilterActive() {
        IoParameters parameters = createDefaults();
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertFalse(resolver.hasMobileFilter());
    }

    @Test
    public void when_setAllDatasetTypes_then_noFilterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_DATASET_TYPES, "all");
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertTrue(resolver.shallIncludeAllDatasetTypes());
    }

    @Test
    public void when_setDatasetTypeFilter_then_filterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_DATASET_TYPES, "foobar");
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertFalse(resolver.shallIncludeAllDatasetTypes());
        Assertions.assertTrue(resolver.shallIncludeDatasetType("foobar"));
    }

    @Test
    public void when_setObservationTypeFilter_then_filterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_OBSERVATION_TYPES, "foobar");
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertFalse(resolver.shallIncludeAllObservationTypes());
        Assertions.assertTrue(resolver.shallIncludeObservationType("foobar"));
    }

    @Test
    public void when_setValueTypeFilter_then_filterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_VALUE_TYPES, "foobar");
        FilterResolver resolver = createResolver(parameters);
        Assertions.assertFalse(resolver.shallIncludeAllValueTypes());
        Assertions.assertTrue(resolver.shallIncludeValueType("foobar"));
    }

}
