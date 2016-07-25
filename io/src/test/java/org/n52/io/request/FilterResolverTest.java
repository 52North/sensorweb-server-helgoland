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

import static org.junit.Assert.assertTrue;
import static org.n52.io.request.IoParameters.createDefaults;

import org.junit.Assert;
import org.junit.Test;

public class FilterResolverTest {

	private FilterResolver createResolver(IoParameters resolver) {
		return new FilterResolver(resolver);
	}

	@Test
	public void when_defaults_then_behaveBackwardsCompatible() {
	    FilterResolver resolver = createResolver(createDefaults());
	    assertTrue(resolver.shallBehaveBackwardsCompatible());
	}

	@Test
    public void when_setPlatformTypeFilter_then_dontBehaveBackwardsCompatible() {
        FilterResolver resolver = createResolver(createDefaults()
                 .extendWith(Parameters.FILTER_PLATFORM_TYPES, "blah"));
        Assert.assertFalse(resolver.shallBehaveBackwardsCompatible());
    }

    @Test
    public void when_setDatasetTypeFiltre_then_dontBehaveBackwardsCompatible() {
        FilterResolver resolver = createResolver(createDefaults()
                 .extendWith(Parameters.FILTER_DATASET_TYPES, "blah"));
        Assert.assertFalse(resolver.shallBehaveBackwardsCompatible());
    }

    @Test
    public void when_defaults_then_allPlatformGeometryFiltersActive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesSite());
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
    }

    @Test
    public void when_defaults_then_allObservedGeometryFiltersActive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
    }

    @Test
    public void when_stationaryPlatformsFilter_then_sitesAndAndRemoteFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_TYPES, "stationary"));
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesSite());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
    }

    @Test
    public void when_mobilePlatformsFilter_then_tracksAndRemoteFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_TYPES, "mobile"));
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesSite());
    }

    @Test
    public void when_insituPlatformsFilter_then_sitesAndtracksFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_TYPES, "insitu"));
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesSite());
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
        Assert.assertFalse(resolver.shallIncludeObservedGeometriesStatic());
        Assert.assertFalse(resolver.shallIncludeObservedGeometriesDynamic());
    }

    @Test
    public void when_remotePlatformsFilter_then_allGeometriesFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_TYPES, "remote"));
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesSite());
    }

    @Test
    public void when_allObservedGeometries_then_allObservedGeometryFiltersActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "all"));
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
    }

    @Test
    public void when_allPlatformGeometries_then_allPlatformGeometryFiltersActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "all"));
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesSite());
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
    }

    @Test
    public void when_allPlatformsRemoteGeometries_then_allGeometriesFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_TYPES, "remote")
        		.extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "all"));
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesSite());
    }

    @Test
    public void when_allGeometriesStationaryPlatforms_then_allSitesFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "all")
        		.extendWith(Parameters.FILTER_PLATFORM_TYPES, "stationary"));
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesSite());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
    }

    @Test
    public void when_allGeometriesMobilePlatforms_then_allTracksFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_TYPES, "mobile")
        		.extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "all"));
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesSite());
    }

    @Test
    public void when_sitePlatformGeometries_then_siteFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "site"));
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesSite());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
    }

    @Test
    public void when_trackPlatformGeometries_then_trackFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "track"));
        Assert.assertTrue(resolver.shallIncludePlatformGeometriesTrack());
    }

    @Test
    public void when_trackPlatformGeometries_then_siteFilterInactive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "track"));
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesSite());
    }

    @Test
    public void when_staticObservedGeometries_then_staticFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "static"));
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesStatic());
        Assert.assertFalse(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesSite());
    }

    @Test
    public void when_dynamicObservedGeometries_then_dynamicFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
        		.extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "dynamic"));
        Assert.assertTrue(resolver.shallIncludeObservedGeometriesDynamic());
        Assert.assertFalse(resolver.shallIncludeObservedGeometriesStatic());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesTrack());
        Assert.assertFalse(resolver.shallIncludePlatformGeometriesSite());
    }

    @Test
    public void when_defaults_then_insituFilterActive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assert.assertTrue(resolver.shallIncludeInsituPlatformTypes());
    }

    @Test
    public void when_defaults_then_stationaryFilterActive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assert.assertTrue(resolver.shallIncludeStationaryPlatformTypes());
    }

    @Test
    public void when_defaults_then_remoteFilterInactive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assert.assertTrue(resolver.shallIncludeRemotePlatformTypes());
    }

    @Test
    public void when_defaults_then_mobileFilterInactive() {
        FilterResolver resolver = createResolver(createDefaults());
        Assert.assertTrue(resolver.shallIncludeMobilePlatformTypes());
    }

    @Test
    public void when_setMobile_then_allMobilesFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "mobile"));
        Assert.assertTrue(resolver.shallIncludeMobilePlatformTypes());
        Assert.assertTrue(resolver.shallIncludeInsituPlatformTypes());
        Assert.assertTrue(resolver.shallIncludeRemotePlatformTypes());
        Assert.assertFalse(resolver.shallIncludeStationaryPlatformTypes());
    }

    @Test
    public void when_setRemote_then_allRemotesFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "remote"));
        Assert.assertTrue(resolver.shallIncludeRemotePlatformTypes());
        Assert.assertTrue(resolver.shallIncludeMobilePlatformTypes());
        Assert.assertTrue(resolver.shallIncludeStationaryPlatformTypes());
        Assert.assertFalse(resolver.shallIncludeInsituPlatformTypes());
    }

    @Test
    public void when_setMobileRemote_then_allMobileRemotesFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "mobile", "remote"));
        Assert.assertTrue(resolver.shallIncludeMobilePlatformTypes());
        Assert.assertTrue(resolver.shallIncludeRemotePlatformTypes());
        Assert.assertFalse(resolver.shallIncludeInsituPlatformTypes());
        Assert.assertFalse(resolver.shallIncludeStationaryPlatformTypes());
    }

    @Test
    public void when_setMobileInsitu_then_allMobileInsitusFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "mobile", "insitu"));
        Assert.assertTrue(resolver.shallIncludeMobilePlatformTypes());
        Assert.assertTrue(resolver.shallIncludeInsituPlatformTypes());
        Assert.assertFalse(resolver.shallIncludeStationaryPlatformTypes());
        Assert.assertFalse(resolver.shallIncludeRemotePlatformTypes());
    }

    @Test
    public void when_setStationaryInsitu_then_allStationaryInsitusFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "stationary", "insitu"));
        Assert.assertTrue(resolver.shallIncludeInsituPlatformTypes());
        Assert.assertTrue(resolver.shallIncludeStationaryPlatformTypes());
        Assert.assertFalse(resolver.shallIncludeMobilePlatformTypes());
        Assert.assertFalse(resolver.shallIncludeRemotePlatformTypes());
    }

    @Test
    public void when_setStationaryRemote_then_allStationaryRemotesFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "stationary", "remote"));
        Assert.assertTrue(resolver.shallIncludeStationaryPlatformTypes());
        Assert.assertTrue(resolver.shallIncludeRemotePlatformTypes());
        Assert.assertFalse(resolver.shallIncludeInsituPlatformTypes());
        Assert.assertFalse(resolver.shallIncludeMobilePlatformTypes());
    }

    @Test
    public void when_setAllPlatformTypes_then_insituFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "all"));
        Assert.assertTrue(resolver.shallIncludeInsituPlatformTypes());
    }

    @Test
    public void when_setAllPlatformTypes_then_stationaryFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "all"));
        Assert.assertTrue(resolver.shallIncludeStationaryPlatformTypes());
    }

    @Test
    public void when_setAllPlatformTypes_then_remoteFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "all"));
        Assert.assertTrue(resolver.shallIncludeRemotePlatformTypes());
    }


    @Test
    public void when_setAllPlatformTypes_then_mobileFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "all"));
        Assert.assertTrue(resolver.shallIncludeMobilePlatformTypes());
    }

    @Test
    public void when_setAllDatasetTypes_then_noFilterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                 .extendWith(Parameters.FILTER_DATASET_TYPES, "all"));
        Assert.assertTrue(resolver.shallIncludeAllDatasetTypes());
    }

    @Test
    public void when_setDatasetTypeFilter_then_filterActive() {
        FilterResolver resolver = createResolver(createDefaults()
                 .extendWith(Parameters.FILTER_DATASET_TYPES, "foobar"));
        Assert.assertFalse(resolver.shallIncludeAllDatasetTypes());
        Assert.assertTrue(resolver.shallIncludeDatasetType("foobar"));
    }

}
