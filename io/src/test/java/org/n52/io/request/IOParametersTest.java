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

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import static org.n52.io.request.IoParameters.getJsonNodeFrom;
import static org.n52.io.request.RequestSimpleParameterSet.createForSingleSeries;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.n52.io.request.IoParameters.createDefaults;
import static org.n52.io.request.IoParameters.createFromMultiValueMap;
import static org.n52.io.request.IoParameters.createFromQuery;
import static org.n52.io.request.IoParameters.createFromSingleValueMap;

public class IOParametersTest {

    private File getAlternativeConfigFile() throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/").toURI());
        return root.resolve("test-config.json").toFile();
    }

    @Test
    public void when_creationViaFromSingleValuedMap_then_keysGetLowerCased() {
        Map<String, String> map = new HashMap<>();
        map.put("camelCased", "value");
        map.put("UPPERCASED", "value");
        IoParameters parameters = createFromSingleValueMap(map);
        Assert.assertTrue(parameters.containsParameter("camelCased"));
        Assert.assertTrue(parameters.containsParameter("camelcased"));
        Assert.assertTrue(parameters.containsParameter("UPPERCASED"));
        Assert.assertTrue(parameters.containsParameter("uppercased"));
    }

    @Test
    public void when_creationViaFromMultiValuedMap_then_keysGetLowerCased() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("camelCased", "value");
        map.add("UPPERCASED", "value");
        IoParameters parameters = createFromMultiValueMap(map);
        Assert.assertTrue(parameters.containsParameter("camelCased"));
        Assert.assertTrue(parameters.containsParameter("camelcased"));
        Assert.assertTrue(parameters.containsParameter("UPPERCASED"));
        Assert.assertTrue(parameters.containsParameter("uppercased"));
    }

    @Test
    public void when_creationViaRequestParameterSet_then_keysGetLowerCased() {
        RequestParameterSet request = new RequestSimpleParameterSet();
        request.addParameter("camelCased", getJsonNodeFrom("value"));
        request.addParameter("UPPERCASED", getJsonNodeFrom("value"));
        IoParameters parameters = createFromQuery(request);
        Assert.assertTrue(parameters.containsParameter("camelCased"));
        Assert.assertTrue(parameters.containsParameter("camelcased"));
        Assert.assertTrue(parameters.containsParameter("UPPERCASED"));
        Assert.assertTrue(parameters.containsParameter("uppercased"));
    }

    @Test
    public void when_defaults_then_valuesFromDefaultConfigFile() {
        IoParameters parameters = createDefaults();
        assertThat(parameters.getChartDimension().getWidth(), is(2000));
    }

    @Test
    public void when_createdWithConfig_then_widthIsOfAppropriateValue() throws URISyntaxException {
        IoParameters parameters = createDefaults(getAlternativeConfigFile());
        assertThat(parameters.getChartDimension().getWidth(), is(1000));
    }

    @Test
    public void testBooleanValue() {
        IoParameters parameters = createDefaults();
        Assert.assertTrue(parameters.isGeneralize());
    }

    @Test
    public void testAfterConvertedFromParameterSet() {
        final IoParameters defaults = createDefaults();
        RequestSimpleParameterSet set = createForSingleSeries("1", defaults);
        IoParameters parameters = createFromQuery(set);
        Assert.assertTrue(parameters.isGeneralize());
    }

    @Test
    public void when_extending_then_parameterIsPresent() {
        IoParameters defaults = createDefaults();
        IoParameters extended = defaults.extendWith("test", "value");
        Assert.assertFalse(defaults.containsParameter("test"));
        Assert.assertTrue(extended.containsParameter("test"));
        Assert.assertThat(extended.getAsString("test"), Matchers.is("value"));
    }

    @Test
    public void when_extendingCamelCased_then_parameterIsPresent() {
        IoParameters defaults = createDefaults();
        IoParameters extended = defaults.extendWith("testParameter", "value");
        Assert.assertFalse(defaults.containsParameter("testParameter"));
        Assert.assertTrue(extended.containsParameter("testParameter"));
        Assert.assertThat(extended.getAsString("testParameter"), Matchers.is("value"));
    }

    @Test
    public void when_extending_then_valueObjectIsDifferent() {
        IoParameters defaults = createDefaults();
        IoParameters extended = defaults.extendWith("test", "value");
        Assert.assertFalse(defaults == extended);
    }
    
    @Test
    public void when_defaults_then_allPlatformGeometryFiltersActive() {
        IoParameters defaults = createDefaults();
        Assert.assertTrue(defaults.shallIncludePlatformGeometriesSite());
        Assert.assertTrue(defaults.shallIncludePlatformGeometriesTrack());
    }
    
    @Test
    public void when_defaults_then_allObservedGeometryFiltersActive() {
        IoParameters defaults = createDefaults();
        Assert.assertTrue(defaults.shallIncludeObservedGeometriesDynamic());
        Assert.assertTrue(defaults.shallIncludeObservedGeometriesStatic());
    }
    
    @Test
    public void when_allObservedGeometries_then_allObservedGeometryFiltersActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "all");
        Assert.assertTrue(parameters.shallIncludeObservedGeometriesDynamic());
        Assert.assertTrue(parameters.shallIncludeObservedGeometriesStatic());
    }
    
    @Test
    public void when_allPlatformGeometries_then_allPlatformGeometryFiltersActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "all");
        Assert.assertTrue(parameters.shallIncludePlatformGeometriesSite());
        Assert.assertTrue(parameters.shallIncludePlatformGeometriesTrack());
    }
    
    @Test
    public void when_sitePlatformGeometries_then_siteFilterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "site");
        Assert.assertTrue(parameters.shallIncludePlatformGeometriesSite());
    }
    
    @Test
    public void when_sitePlatformGeometries_then_trackFilterInactive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_PLATFORM_GEOMETRIES, "site");
        Assert.assertFalse(parameters.shallIncludePlatformGeometriesTrack());
    }
    
    @Test
    public void when_staticObservedGeometries_then_dynamicFilterActive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "static");
        Assert.assertTrue(parameters.shallIncludeObservedGeometriesStatic());
    }
    
    @Test
    public void when_staticObservedGeometries_then_dynamicFilterInactive() {
        IoParameters parameters = createDefaults().extendWith(Parameters.FILTER_OBSERVED_GEOMETRIES, "static");
        Assert.assertFalse(parameters.shallIncludeObservedGeometriesDynamic());
    }
    
    @Test
    public void when_defaults_then_insituFilterActive() {
        IoParameters defaults = createDefaults();
        Assert.assertTrue(defaults.shallIncludeInsituPlatformTypes());
    }

    @Test
    public void when_defaults_then_stationaryFilterActive() {
        IoParameters defaults = createDefaults();
        Assert.assertTrue(defaults.shallIncludeStationaryPlatformTypes());
    }

    @Test
    public void when_defaults_then_remoteFilterInactive() {
        IoParameters defaults = createDefaults();
        Assert.assertFalse(defaults.shallIncludeRemotePlatformTypes());
    }

    @Test
    public void when_defaults_then_mobileFilterInactive() {
        final IoParameters parameters = IoParameters.createDefaults();
        Assert.assertFalse(parameters.shallIncludeMobilePlatformTypes());
    }

    @Test
    public void when_setMobile_then_mobileFilterActive() {
        final IoParameters parameters = IoParameters.createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "mobile");
        Assert.assertTrue(parameters.shallIncludeMobilePlatformTypes());
    }

    @Test
    public void when_setRemote_then_remoteFilterActive() {
        final IoParameters parameters = IoParameters.createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "remote");
        Assert.assertTrue(parameters.shallIncludeRemotePlatformTypes());
    }

    @Test
    public void when_setAll_then_insituFilterActive() {
        final IoParameters parameters = IoParameters.createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "all");
        Assert.assertTrue(parameters.shallIncludeInsituPlatformTypes());
    }

    @Test
    public void when_setAll_then_stationaryFilterActive() {
        final IoParameters parameters = IoParameters.createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "all");
        Assert.assertTrue(parameters.shallIncludeStationaryPlatformTypes());
    }

    @Test
    public void when_setAll_then_remoteFilterActive() {
        final IoParameters parameters = IoParameters.createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "all");
        Assert.assertTrue(parameters.shallIncludeRemotePlatformTypes());
    }


    @Test
    public void when_setAll_then_mobileFilterActive() {
        final IoParameters parameters = IoParameters.createDefaults()
                .extendWith(Parameters.FILTER_PLATFORM_TYPES, "all");
        Assert.assertTrue(parameters.shallIncludeMobilePlatformTypes());
    }

}
