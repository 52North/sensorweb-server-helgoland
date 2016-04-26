/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.io.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.n52.io.IntervalWithTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RequestParameterSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParameterSet.class);

    private final Map<String, JsonNode> parameters;

    // XXX refactor ParameterSet, DesignedParameterSet, UndesingedParameterSet and QueryMap

    protected RequestParameterSet() {
        parameters = new HashMap<>();
        parameters.put("timespan", IoParameters.getJsonNodeFrom(createDefaultTimespan()));
    }

    private String createDefaultTimespan() {
        DateTime now = new DateTime();
        DateTime lastWeek = now.minusWeeks(1);
        String interval = lastWeek
                .toString()
                .concat("/")
                .concat(now.toString());
        return new IntervalWithTimeZone(interval).toString();
    }

    /**
     * @return If timeseries data shall be generalized or not.
     */
    public boolean isGeneralize() {
        return getAsBoolean("generalize", false);
    }

    /**
     * @param generalize if output shall be generalized
     */
    public void setGeneralize(boolean generalize) {
        parameters.put("generalize", IoParameters.getJsonNodeFrom(generalize));
    }

    /**
     * Sets the timespan of interest (as <a href="http://en.wikipedia.org/wiki/ISO_8601#Time_intervals">ISO8601
     * interval</a> excluding the Period only version).
     *
     * @return the timespan in ISO-8601
     */
    @JsonProperty
    public String getTimespan() {
        return getAsString("timespan");
    }

    /**
     * @param timespan the timespan to set.
     */
    public void setTimespan(String timespan) {
        timespan = timespan != null
                ? validateTimespan(timespan)
                : createDefaultTimespan();
        parameters.put("timespan", IoParameters.getJsonNodeFrom(timespan));
    }

    /**
     * If image data shall be encoded in Base64 to be easily embedded in HTML by JS clients.
     *
     * @return if image shall be base64 encoded.
     */
    public boolean isBase64() {
        return getAsBoolean("base64", false);
	}

    /**
     * @param base64 If the image shall be base64 encoded.
     */
	public void setBase64(boolean base64) {
        parameters.put("base64", IoParameters.getJsonNodeFrom(base64));
	}

    /**
     * @return If reference values shall be appended to the timeseries data.
     */
	public boolean isExpanded() {
        return getAsBoolean("expanded", false);
    }

    /**
     * @param expanded verbose results.
     */
    public void setExpanded(boolean expanded) {
        parameters.put("expanded", IoParameters.getJsonNodeFrom(expanded));
    }

    /**
     * @return A language code to determine the requested locale. "en" is the default.
     */
    public String getLanguage() {
        return getAsString("language");
    }

    /**
     * @param language A language code to determine the requested locale.
     */
    public void setLanguage(String language) {
        language = !(language == null || language.isEmpty())
                ? language
                : "en";
        parameters.put("language", IoParameters.getJsonNodeFrom(language));
    }

    private String validateTimespan(String timespan) {
        return new IntervalWithTimeZone(timespan).toString();
    }

    public Set<String> availableParameters() {
        return Collections.unmodifiableSet(this.parameters.keySet());
    }

    public final boolean containsParameter(String parameter) {
        return this.parameters.containsKey(parameter)
                && this.parameters.get(parameter) != null;
    }

    public final Object getParameter(String parameter) {
        return parameters.get(parameter);
    }

    public final void setParameters(Map<String, JsonNode> parameters) {
        if (parameters != null) {
            this.parameters.clear();
            this.parameters.putAll(parameters);
        }
    }

    /**
     * Sets the value for the given parameter name. Overrides if already exists.
     *
     * @param parameter parameter name.
     * @param value the parameter's value.
     */
    public final void addParameter(String parameter, JsonNode value) {
        this.parameters.put(parameter.toLowerCase(), value);
    }

    public final <T> T getAs(Class<T> clazz, String parameter) {
        try {
            if ( !parameters.containsKey(parameter.toLowerCase())) {
                LOGGER.debug("parameter '{}' is not available.", parameter);
                return null;
            }
            ObjectMapper om = new ObjectMapper();
            return om.treeToValue(getAsJsonNode(parameter), clazz);
        } catch (IOException e) {
            LOGGER.error("No appropriate config for parameter '{}'.", parameter, e);
            return null;
        }
    }

    public final Object getAsObject(String parameter) {
        return this.parameters.get(parameter.toLowerCase());
    }

    public final Object getAsObject(String parameter, Object defaultValue) {
        return this.parameters.containsKey(parameter.toLowerCase())
                ? this.parameters.get(parameter.toLowerCase())
                : defaultValue;
    }

    public final JsonNode getAsJsonNode(String parameter) {
        return (JsonNode) this.parameters.get(parameter.toLowerCase());
    }

    public final String getAsString(String parameter) {
        return this.parameters.get(parameter.toLowerCase()).asText();
    }

    public final String getAsString(String parameter, String defaultValue) {
        return this.parameters.containsKey(parameter.toLowerCase())
                ? this.parameters.get(parameter.toLowerCase()).asText()
                : defaultValue;
    }

    public final int getAsInt(String parameter) {
        return this.parameters.get(parameter.toLowerCase()).asInt();
    }

    public final int getAsInt(String parameter, int defaultValue) {
        return this.parameters.containsKey(parameter.toLowerCase())
                ? this.parameters.get(parameter.toLowerCase()).asInt()
                : defaultValue;
    }

    public final boolean getAsBoolean(String parameter) {
        return this.parameters.get(parameter.toLowerCase()).asBoolean();
    }

    public final boolean getAsBoolean(String parameter, boolean defaultValue) {
        return this.parameters.containsKey(parameter.toLowerCase())
                ? this.parameters.get(parameter.toLowerCase()).asBoolean()
                : defaultValue;
    }

    public abstract String[] getTimeseries();

}
