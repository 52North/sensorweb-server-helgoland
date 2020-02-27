/**
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.n52.io.IoParameters.createDefaults;
import static org.n52.io.IoParameters.createFromQuery;
import static org.n52.io.IoParameters.createFromSingleValueMap;
import static org.n52.io.IoParameters.getJsonNodeFrom;
import static org.n52.io.v1.data.UndesignedParameterSet.createForSingleTimeseries;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;
import org.n52.io.IoParameters;
import org.n52.io.crs.BoundingBox;
import org.n52.io.v1.data.ParameterSet;
import org.n52.io.v1.data.UndesignedParameterSet;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class IOParametersTest {
    
    private File getAlternativeConfigFile() throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/").toURI());
        return root.resolve("test-config.json").toFile();
    }
    
    @Test
    public void when_defaultTimezone_then_timezoneIsUTC() {
        IoParameters config = IoParameters.createDefaults();
        String timezone = config.getOutputTimezone();
        assertThat(DateTimeZone.forID(timezone), is(DateTimeZone.UTC));
    }

    @Test
    public void when_jsonBbox_then_parsingSpatialFilter() throws ParseException {
        Map<String, String> map = Collections.singletonMap("bbox", "{\"ll\":{\"type\":\"Point\",\"coordinates\":[6.7,51.7]},\"ur\":{\"type\":\"Point\",\"coordinates\":[7.9,51.9]}}");
        IoParameters parameters = createFromSingleValueMap(map);
        BoundingBox actual = parameters.getSpatialFilter();
        WKTReader wktReader = new WKTReader();
        Geometry ll = wktReader.read("POINT (6.7 51.7)");
        Geometry ur = wktReader.read("POINT(7.9 51.9)");
        Assert.assertTrue(actual.getLowerLeft().equals(ll));
        Assert.assertTrue(actual.getUpperRight().equals(ur));
    }

    @Test
    public void when_creationViaFromSingleValuedMap_then_keysGetLowerCased() {
        Map<String, String> map = new HashMap<>();
        map.put("camelCased", "value");
        map.put("UPPERCASED", "value");
        IoParameters parameters = IoParameters.createFromSingleValueMap(map);
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
        IoParameters parameters = IoParameters.createFromMultiValueMap(map);
        Assert.assertTrue(parameters.containsParameter("camelCased"));
        Assert.assertTrue(parameters.containsParameter("camelcased"));
        Assert.assertTrue(parameters.containsParameter("UPPERCASED"));
        Assert.assertTrue(parameters.containsParameter("uppercased"));
    }

    @Test
    public void when_creationViaParameterSet_then_keysGetLowerCased() {
        ParameterSet request = new UndesignedParameterSet();
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
        ParameterSet set = createForSingleTimeseries("1", defaults);
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
    public void when_extendingMultiple_then_availableFromSet() {
        IoParameters defaults = createDefaults();
        IoParameters extended = defaults.extendWith("test", "value1", "value2");
        assertThat(extended.getValuesOf("test").size(), is(2));
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
    public void testExpandWithNextValuesBeyondInterval() {
        IoParameters parameters = createDefaults();
        Assert.assertTrue(parameters.isExpandWithNextValuesBeyondInterval());
    }
    
    @Test
    public void testCache() {
        IoParameters parameters = createDefaults();
        Assert.assertTrue(parameters.hasCache());
    }
    
    @Test
    public void testGetCache() {
        IoParameters parameters = createDefaults();
        JsonNode cache = parameters.getCache();
        Assert.assertTrue(cache.has("stations"));
        Assert.assertTrue(cache.get("stations").asLong(0) == 1440);
    }
    
}
