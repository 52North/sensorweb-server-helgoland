/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.n52.io.request.IoParameters.createDefaults;
import static org.n52.io.request.IoParameters.createFromMultiValueMap;
import static org.n52.io.request.IoParameters.createFromSingleValueMap;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.crs.BoundingBox;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class IoParametersTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void when_ioParseExceptionActionIsRuntimeException_then_exeptionIsThrown() {
        IoParameters defaults = IoParameters.createDefaults();
        defaults.setParseExceptionHandle((parameter, e) -> {
            throw new IllegalArgumentException(parameter, e);
        });
        thrown.expect(IllegalArgumentException.class);
        defaults.extendWith(Parameters.OFFSET, "invalid value")
                .getOffset();
    }

    private File getAlternativeConfigFile() throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/")
                                        .toURI());
        return root.resolve("test-config.json")
                   .toFile();
    }

    @Test
    public void when_defaultTimezone_then_timezoneIsUTC() {
        IoParameters config = IoParameters.createDefaults();
        String timezone = config.getOutputTimezone();
        assertThat(DateTimeZone.forID(timezone), is(DateTimeZone.UTC));
    }

    @Test
    public void when_jsonBbox_then_parsingSpatialFilter() throws ParseException {
        Map<String,
            String> map = Collections.singletonMap("bbox",
                                                   "{\"ll\":{\"type\":\"Point\",\"coordinates\":[6.7,51.7]},\"ur\":{\"type\":\"Point\",\"coordinates\":[7.9,51.9]}}");
        IoParameters parameters = createFromSingleValueMap(map);
        BoundingBox actual = parameters.getSpatialFilter();
        WKTReader wktReader = new WKTReader();
        Geometry ll = wktReader.read("POINT (6.7 51.7)");
        Geometry ur = wktReader.read("POINT(7.9 51.9)");
        Assert.assertTrue(actual.getLowerLeft()
                                .equals(ll));
        Assert.assertTrue(actual.getUpperRight()
                                .equals(ur));
    }

    @Test
    public void when_geoJsonBboxWithTrimmableValues_then_parsingSpatialFilter() throws ParseException {
        Map<String, String> map = Collections.singletonMap("bbox", "6.7, 51.7,  7.9, 51.9");
        IoParameters parameters = createFromSingleValueMap(map);
        BoundingBox actual = parameters.getSpatialFilter();
        WKTReader wktReader = new WKTReader();
        Geometry ll = wktReader.read("POINT (6.7 51.7)");
        Geometry ur = wktReader.read("POINT(7.9 51.9)");
        Assert.assertTrue(actual.getLowerLeft()
                                .equals(ll));
        Assert.assertTrue(actual.getUpperRight()
                                .equals(ur));
    }


    @Test
    public void when_geoJsonBboxNegativeValues_then_parsingSpatialFilter() throws ParseException {
        Map<String, String> map = Collections.singletonMap("bbox", "-180,-90,180,90");
        IoParameters parameters = createFromSingleValueMap(map);
        BoundingBox actual = parameters.getSpatialFilter();
        WKTReader wktReader = new WKTReader();
        Geometry ll = wktReader.read("POINT (-180 -90)");
        Geometry ur = wktReader.read("POINT(180 90)");
        Assert.assertTrue(actual.getLowerLeft()
                                .equals(ll));
        Assert.assertTrue(actual.getUpperRight()
                                .equals(ur));
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
    public void when_defaults_then_valuesFromDefaultConfigFile() {
        IoParameters parameters = createDefaults();
        assertThat(parameters.getWidth(), is(2000));
    }

    @Test
    public void when_createdWithConfig_then_widthIsOfAppropriateValue() throws URISyntaxException {
        IoParameters parameters = createDefaults(getAlternativeConfigFile());
        assertThat(parameters.getWidth(), is(1000));
    }

    @Test
    public void testBooleanValue() {
        IoParameters parameters = createDefaults();
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
        assertThat(extended.getValuesOf("test")
                           .size(),
                   is(2));
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
    public void when_defaults_then_backwardCompatible() {
        FilterResolver filterResolver = createDefaults().getFilterResolver();
        assertThat(filterResolver.shallBehaveBackwardsCompatible(), is(true));
    }

    @Test
    public void when_timespanWithNow_then_normalizeWithDateString() {
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("YYYY-MM-dd");
        String now = dateFormat.print(new DateTime());

        IoParameters parameters = createDefaults().extendWith(Parameters.TIMESPAN, "PT4h/now");
        IntervalWithTimeZone expected = new IntervalWithTimeZone("PT4h/" + now);
        assertThat(parameters.getNormalizedTimespan(dateFormat), is(expected.toString()));
    }

    @Test
    public void when_singleFilter_then_filterPresentViaMultipleGetter() {
        IoParameters parameters = createDefaults().extendWith(Parameters.PROCEDURE, "foo");
        assertThat(parameters.getProcedures(), containsInAnyOrder("foo"));
    }

    @Test
    public void when_singleAndMultipleFilter_then_filterGetsMerged() {
        IoParameters parameters = createDefaults().extendWith(Parameters.PROCEDURE, "foo")
                                                  .extendWith(Parameters.PROCEDURES, "foo", "bar");
        assertThat(parameters.getProcedures(), containsInAnyOrder("foo", "bar"));
    }

    @Test
    public void when_backwardsCompatibleParameters_then_indicateBackwardsCompatibility() {
        IoParameters backwardsCompatibleParameters = IoParameters.createDefaults()
                                                                 .respectBackwardsCompatibility();
        MatcherAssert.assertThat(backwardsCompatibleParameters.shallBehaveBackwardsCompatible(), is(true));
    }

    @Test
    public void when_backwardsCompatibleParameters_then_extendingParametersWillStayBackwardsCompatible() {
        IoParameters defaults = IoParameters.createDefaults();
        MatcherAssert.assertThat(defaults.shallBehaveBackwardsCompatible(), is(false));
    }

}
