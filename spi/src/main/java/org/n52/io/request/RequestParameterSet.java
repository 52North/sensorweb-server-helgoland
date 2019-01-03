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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTimeZone;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.response.dataset.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serialization POJO for POST requests. All parameters will be passed to an {@link IoParameters} instance.
 */
abstract class RequestParameterSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParameterSet.class);

    private static final String PARAMETER_RESULT_TIME = "resultTime";

    private static final String DEFAULT_TIMEZONE = "UTC";

    private static final String DEFAULT_LOCALE = "en";

    private final Map<String, JsonNode> parameters;

    protected RequestParameterSet() {
        parameters = new HashMap<>();
        IntervalWithTimeZone defaultTimespan = IoParameters.createDefaultTimespan();
        parameters.put(Parameters.TIMESPAN, IoParameters.getJsonNodeFrom(defaultTimespan));
    }

    public String getOutputTimezone() {
        return getAsString(Parameters.OUTPUT_TIMEZONE, DEFAULT_TIMEZONE);
    }

    public void setOutputTimezone(String timezone) {
        Set<String> availableIDs = DateTimeZone.getAvailableIDs();
        DateTimeZone zone = availableIDs.contains(timezone)
                ? DateTimeZone.forID(timezone)
                : DateTimeZone.UTC;
        setParameter(Parameters.OUTPUT_TIMEZONE, IoParameters.getJsonNodeFrom(zone.getID()));
    }

    /**
     * @return If timeseries data shall be generalized or not.
     */
    public boolean isGeneralize() {
        return getAsBoolean(Parameters.GENERALIZE, false);
    }

    /**
     * @param generalize
     *        if output shall be generalized
     */
    public void setGeneralize(boolean generalize) {
        setParameter(Parameters.GENERALIZE, IoParameters.getJsonNodeFrom(generalize));
    }

    /**
     * Sets the timespan of interest (as
     * <a href="http://en.wikipedia.org/wiki/ISO_8601#Time_intervals">ISO8601 interval</a> excluding the
     * Period only version).
     *
     * @return the timespan in ISO-8601
     */
    @JsonProperty
    public String getTimespan() {
        return getAsString(Parameters.TIMESPAN);
    }

    /**
     * @param timespan
     *        the timespan to set.
     */
    public void setTimespan(String timespan) {
        IntervalWithTimeZone nonNullTimespan = timespan == null
                ? IoParameters.createDefaultTimespan()
                : validateTimespan(timespan);
        setParameter(Parameters.TIMESPAN, IoParameters.getJsonNodeFrom(nonNullTimespan.toString()));
    }

    /**
     * If image data shall be encoded in Base64 to be easily embedded in HTML by JS clients.
     *
     * @return if image shall be base64 encoded.
     */
    public boolean isBase64() {
        return getAsBoolean(Parameters.BASE_64, false);
    }

    /**
     * @param base64
     *        If the image shall be base64 encoded.
     */
    public void setBase64(boolean base64) {
        setParameter(Parameters.BASE_64, IoParameters.getJsonNodeFrom(base64));
    }

    /**
     * @return If reference values shall be appended to the timeseries data.
     */
    public boolean isExpanded() {
        return getAsBoolean(Parameters.EXPANDED, false);
    }

    /**
     * @return A language code to determine the requested locale. "en" is the default.
     * @deprecated use {@link #getLocale()}
     */
    @Deprecated
    public String getLanguage() {
        return getAsString(Parameters.LANGUAGE, DEFAULT_LOCALE);
    }

    /**
     * @param language
     *        A language code to determine the requested locale.
     * @deprecated use {@link #setLocale(String)}
     */
    @Deprecated
    public void setLanguage(String language) {
        String nonNullLanguage = !(language == null || language.isEmpty())
                ? language
                : DEFAULT_LOCALE;
        setParameter(Parameters.LANGUAGE, IoParameters.getJsonNodeFrom(nonNullLanguage));
    }

    /**
     * @param expanded
     *        verbose results.
     */
    public void setExpanded(boolean expanded) {
        setParameter(Parameters.EXPANDED, IoParameters.getJsonNodeFrom(expanded));
    }

    /**
     * @return A language code to determine the requested locale. "en" is the default.
     */
    public String getLocale() {
        return getAsString(Parameters.LOCALE);
    }

    /**
     * @param locale
     *        A language code to determine the requested locale.
     */
    public void setLocale(String locale) {
        String nonNullLanguage = !(locale == null || locale.isEmpty())
                ? locale
                : Parameters.DEFAULT_LOCALE;
        setParameter(Parameters.LOCALE, IoParameters.getJsonNodeFrom(nonNullLanguage));
    }

    /**
     * @return the result time.
     */
    public String getResultTime() {
        return getAsString(PARAMETER_RESULT_TIME);
    }

    /**
     * @param resultTime
     *        Optional parameter, to define a result time in the request.
     */
    public void setResultTime(String resultTime) {
        if (resultTime != null) {
            setParameter(PARAMETER_RESULT_TIME, IoParameters.getJsonNodeFrom(resultTime));
        }
    }

    private IntervalWithTimeZone validateTimespan(String timespan) {
        return new IntervalWithTimeZone(timespan);
    }

    public Set<String> availableParameterNames() {
        return Collections.unmodifiableSet(this.parameters.keySet());
    }

    public final boolean containsParameter(String parameter) {
        String lowerCasedParameter = parameter.toLowerCase();
        return this.parameters.containsKey(lowerCasedParameter)
                && this.parameters.get(lowerCasedParameter) != null;
    }

    public void removeParameter(String parameterName) {
        if (parameterName != null && !parameterName.isEmpty()) {
            parameters.remove(parameterName.toLowerCase());
        }
    }

    public final void setParameters(Map<String, JsonNode> parameters) {
        if (parameters != null) {
            this.parameters.clear();
            for (Map.Entry<String, JsonNode> entry : parameters.entrySet()) {
                setParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    public final void setParameter(String parameter, Object value) {
        setParameter(parameter, IoParameters.getJsonNodeFrom(value));
    }

    /**
     * Sets the value for the given parameter name. Overrides if already exists.
     *
     * @param parameterName
     *        parameter name.
     * @param value
     *        the parameter's value.
     */
    public final void setParameter(String parameterName, JsonNode value) {
        this.parameters.put(parameterName.toLowerCase(), value);
    }

    public final <T> T getAs(Class<T> clazz,
                             String parameterName,
                             T defaultValue) {
        try {
            if (!parameters.containsKey(parameterName.toLowerCase())) {
                return defaultValue;
            }
            ObjectMapper om = new ObjectMapper();
            return om.treeToValue(getParameterValue(parameterName), clazz);
        } catch (IOException e) {
            LOGGER.error("No appropriate config for parameter '{}'.",
                         parameterName,
                         e);
            return null;
        }
    }

    public final <T> T getAs(Class<T> clazz, String parameterName) {
        return getAs(clazz, parameterName, null);
    }

    public final JsonNode getParameterValue(String parameterName) {
        return this.parameters.get(parameterName.toLowerCase());
    }

    public final String[] getAsStringArray(String parameterName) {
        return getAsStringArray(parameterName, null);
    }

    public final String[] getAsStringArray(String parameterName,
                                           String[] defaultValue) {
        if (!parameters.containsKey(parameterName.toLowerCase())) {
            return defaultValue;
        }
        JsonNode parameterValue = getParameterValue(parameterName);
        return parameterValue.isArray()
                ? getAs(String[].class, parameterName, defaultValue)
                : new String[] {
                    getAsString(parameterName)
                };
    }

    public final String getAsString(String parameterName) {
        return getAsString(parameterName, null);
    }

    public final String getAsString(String parameterName, String defaultValue) {
        return this.parameters.containsKey(parameterName.toLowerCase())
                ? getParameterValue(parameterName).asText()
                : defaultValue;
    }

    public final Integer getAsInt(String parameterName) {
        return getAsInt(parameterName, null);
    }

    public final Integer getAsInt(String parameterName, Integer defaultValue) {
        return this.parameters.containsKey(parameterName.toLowerCase())
                ? (Integer) getParameterValue(parameterName).asInt()
                : defaultValue;
    }

    public final Boolean getAsBoolean(String parameterName) {
        return getAsBoolean(parameterName, null);
    }

    public final Boolean getAsBoolean(String parameterName, Boolean defaultValue) {
        return this.parameters.containsKey(parameterName.toLowerCase())
                ? (Boolean) getParameterValue(parameterName).asBoolean()
                : defaultValue;
    }

    public abstract String[] getDatasets();

    @Deprecated
    public String[] getTimeseriesIds() {
        return getDatasets();
    }

    public String getValueType() {
        String handleAs = getAsString(Parameters.HANDLE_AS_VALUE_TYPE);
        String[] datasetIds = getDatasets();
        return datasetIds.length > 0
                ? ValueType.extractType(datasetIds[0], handleAs)
                : ValueType.DEFAULT_VALUE_TYPE;
    }

    public IoParameters toParameters() {
        return IoParameters.createFromSingleJsonValueMap(parameters)
                           .replaceWith(Parameters.DATASETS, getDatasets());
    }

    @Override
    public String toString() {
        return "RequestParameterSet{" + "parameters=" + parameters + '}';
    }

}
