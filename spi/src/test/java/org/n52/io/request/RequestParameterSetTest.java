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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;

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
        parameterset.setParameter("myParameter", jsonFactory.textNode("value"));
        assertThat(parameterset.containsParameter("myParameter"), is(true));
    }

    @Test
    public void when_addingCamelCasedParameter_then_caseInsensitiveAccess() {
        RequestParameterSet parameterset = createDummyParameterSet();
        parameterset.setParameter("myParameter", jsonFactory.textNode("value"));
        assertThat(parameterset.containsParameter("myparameter"), is(true));
    }

    @Test
    public void when_datasetsAvailable_then_accessibleViaParameterName() {
        RequestSimpleParameterSet request = new RequestSimpleParameterSet();
        request.setParameter(Parameters.DATASETS, jsonFactory.arrayNode()
                                                             .add("foo")
                                                             .add("bar"));
        String[] actualValues = request.getAs(String[].class, "datasets");
        assertThat(Arrays.asList(actualValues), contains("foo", "bar"));
    }

    @Test
    public void when_addingSingleValueStringArray_then_accessibleViaParameterName() {
        RequestSimpleParameterSet request = new RequestSimpleParameterSet();
        request.setParameter(Parameters.DATASETS, jsonFactory.arrayNode()
                                                             .add("foo"));
        String[] actualValues = request.getAs(String[].class, "datasets");
        assertThat(Arrays.asList(actualValues), contains("foo"));
    }

    @Test
    public void when_addingStringArray_then_accessibleViaParameterName() {
        RequestParameterSet parameterset = createDummyParameterSet();
        JsonNode array = jsonFactory.arrayNode().add("foo").add("bar");
        parameterset.setParameter("myParameter", array);
        assertThat(Arrays.asList(parameterset.getAsStringArray("myparameter")), contains("foo", "bar"));
    }

    @Test
    public void when_datasetsAvailable_then_accessibleViaGetter() {
        RequestSimpleParameterSet request = new RequestSimpleParameterSet();
        request.setParameter(Parameters.DATASETS, jsonFactory.arrayNode()
                                                             .add("foo")
                                                             .add("bar"));
        assertThat(Arrays.asList(request.getDatasets()), contains("foo", "bar"));
    }

}
