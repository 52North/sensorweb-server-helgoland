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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class RequestParameterSetTest {

    private JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    private RequestParameterSet createDummyParameterSet() {
        return new RequestParameterSet() {
            @Override
            public String[] getDatasets() {
                return null;
            }
        };
    }

    @Test
    public void when_stringParameterNotPresent_then_returnNullInsteadOfExceptions() {
        RequestParameterSet parameterset = createDummyParameterSet();
        assertThat(parameterset.getAsString("notthere"), nullValue(String.class));
    }

    @Test
    public void when_stringParameterNotPresent_then_returnDefault() {
        RequestParameterSet parameterset = createDummyParameterSet();
        assertThat(parameterset.getAsString("notthere", "value"), is("value"));
    }

    @Test
    public void when_booleanParameterNotPresent_then_returnNullInsteadOfExceptions() {
        RequestParameterSet parameterset = createDummyParameterSet();
        assertThat(parameterset.getAsBoolean("notthere"), nullValue(Boolean.class));
    }
    @Test
    public void when_intParameterNotPresent_then_returnDefault() {
        RequestParameterSet parameterset = createDummyParameterSet();
        assertThat(parameterset.getAsInt("notthere", -99), is(-99));
    }
    @Test
    public void when_intParameterNotPresent_then_returnNullInsteadOfExceptions() {
        RequestParameterSet parameterset = createDummyParameterSet();
        assertThat(parameterset.getAsInt("notthere"), nullValue(Integer.class));
    }

    @Test
    public void when_booleanParameterNotPresent_then_returnDefault() {
        RequestParameterSet parameterset = createDummyParameterSet();
        assertThat(parameterset.getAsBoolean("notthere", false), is(false));
    }

    @Test
    public void when_addingCamelCasedParameter_then_caseSensitiveAccess() {
        RequestParameterSet parameterset = createDummyParameterSet();
        parameterset.addParameter("myParameter", jsonFactory.textNode("value"));
        assertThat(parameterset.containsParameter("myParameter"), is(true));
    }

    @Test
    public void when_addingCamelCasedParameter_then_caseInsensitiveAccess() {
        RequestParameterSet parameterset = createDummyParameterSet();
        parameterset.addParameter("myParameter", jsonFactory.textNode("value"));
        assertThat(parameterset.containsParameter("myparameter"), is(true));
    }

    @Test
    public void when_datasetsAvailable_then_accessibleViaParameterName() {
        RequestSimpleParameterSet parameters = IoParameters.createDefaults()
            .extendWith(Parameters.DATASETS, "foo", "bar")
            .toSimpleParameterSet();
        assertThat(Arrays.asList(parameters.getAs(String[].class, "datasets")), contains("foo", "bar"));
    }

    @Test
    public void when_addingSingleValueStringArray_then_accessibleViaParameterName() {
        RequestParameterSet parameters = IoParameters.createDefaults()
                .extendWith(Parameters.DATASETS, "foo")
                .toSimpleParameterSet();
        assertThat(Arrays.asList(parameters.getDatasets()), contains("foo"));
    }

    @Test
    public void when_addingStringArray_then_accessibleViaParameterName() {
        RequestParameterSet parameterset = createDummyParameterSet();
        JsonNode array = jsonFactory.arrayNode().add("foo").add("bar");
        parameterset.addParameter("myParameter", array);
        assertThat(Arrays.asList(parameterset.getAsStringArray("myparameter")), contains("foo", "bar"));
    }

    @Test
    public void when_datasetsAvailable_then_accessibleViaGetter() {
        RequestSimpleParameterSet parameters = IoParameters.createDefaults()
            .extendWith(Parameters.DATASETS, "foo", "bar")
            .toSimpleParameterSet();
        assertThat(Arrays.asList(parameters.getDatasets()), contains("foo", "bar"));
    }

    @Test
    public void testJsonObjectInGeneralConfig() {
        IoParameters parameters = IoParameters.createDefaults();
        RequestSimpleParameterSet set = parameters.toSimpleParameterSet();
        GeneralizerConfig config = set.getAs(GeneralizerConfig.class, "generalizer");
        Assert.assertNotNull(config);
        Assert.assertThat(config.getDefaultGeneralizer(), Matchers.is("lttb"));
    }

    @Test
    public void testNotAvailableJsonObjectInGeneralConfig() {
        IoParameters parameters = IoParameters.createDefaults();
        RequestSimpleParameterSet set = parameters.toSimpleParameterSet();
        GeneralizerConfig config = set.getAs(GeneralizerConfig.class, "doesnotexist");
        Assert.assertNull(config);
    }

    private static class GeneralizerConfig {

        // json serializing object

        private String defaultGeneralizer;
        private String noDataGapThreshold;

        public String getDefaultGeneralizer() {
            return defaultGeneralizer;
        }

        @SuppressWarnings("unused")
        public void setDefaultGeneralizer(String defaultGeneralizer) {
            this.defaultGeneralizer = defaultGeneralizer;
        }

        @SuppressWarnings("unused")
        public String getNoDataGapThreshold() {
            return noDataGapThreshold;
        }

        @SuppressWarnings("unused")
        public void setNoDataGapThreshold(String noDataGapThreshold) {
            this.noDataGapThreshold = noDataGapThreshold;
        }
    }



}
