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
package org.n52.io.style;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Aggregates common style options of multiple timeseries to render them on one
 * chart.
 */
public abstract class Style {

    public static final String PARAMETER_COLOR = "color";

    public static final String PARAMETER_INTERVAL = "interval";

    public static final String VALUE_INTERVAL_BY_MONTH = "byMonth";

    public static final String VALUE_INTERVAL_BY_WEEK = "byWeek";

    public static final String VALUE_INTERVAL_BY_DAY = "byDay";

    public static final String VALUE_INTERVAL_BY_HOUR = "byHour";

    private Map<String, String> properties = new HashMap<>();

    /**
     * @return a 6-digit hex color. If not set a random color will be returned.
     */
    public String getColor() {
        if (hasProperty(PARAMETER_COLOR)) {
            return getPropertyAsString(PARAMETER_COLOR);
        }
        return getRandomHexColor();
    }

    private String getRandomHexColor() {
        String redHex = getNextFormattedRandomNumber();
        String yellowHex = getNextFormattedRandomNumber();
        String blueHex = getNextFormattedRandomNumber();
        return "#" + redHex + yellowHex + blueHex;
    }

    private String getNextFormattedRandomNumber() {
        Random random = new Random(System.currentTimeMillis());
        String randomHex = Integer.toHexString(random.nextInt(256));
        if (randomHex.length() == 1) {
            // ensure two digits
            randomHex = "0" + randomHex;
        }
        return randomHex;
    }

    Object getProperty(String property) {
        return properties == null ? null : properties.get(property);
    }

    String getPropertyAsString(String property) {
        return (String) getProperty(property);
    }

    /**
     * @param property the property name.
     * @return the property value as double.
     * @throws NullPointerException if properties were not set (which means that
     * default values should be expected).
     * @throws NumberFormatException if property value is not a double.
     * @see #hasProperty(String)
     */
    double getPropertyAsDouble(String property) {
        if (properties == null) {
            throwUnknownPropertyException(property);
        }
        return Double.parseDouble(properties.get(property));
    }

    /**
     * @param property the property name.
     * @return the property value as int.
     * @throws NullPointerException if properties were not set (which means that
     * default values should be expected).
     * @throws NumberFormatException if property value is not an integer.
     * @see #hasProperty(String)
     */
    int getPropertyAsInt(String property) {
        if (properties == null) {
            throwUnknownPropertyException(property);
        }
        return Integer.parseInt(properties.get(property));
    }

    /**
     * @param property the property name.
     * @return the property value as boolean.
     * @throws NullPointerException if properties were not set (which means that
     * default values should be expected).
     * @see #hasProperty(String)
     */
    boolean getPropertyAsBoolean(String property) {
        if (properties == null) {
            throwUnknownPropertyException(property);
        }
        return Boolean.parseBoolean(properties.get(property));
    }

    Object[] getPropertyAsArray(String property) {
        return (Object[]) getProperty(property);
    }

    boolean hasProperty(String property) {
        return !shallExpectDefault() && properties.containsKey(property);
    }

    private boolean shallExpectDefault() {
        return properties == null;
    }

    Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets the style properties. If not set or <code>null</code> the default
     * properties are used. However, default styles can be random values and
     * should not be expected to be reproducable.
     *
     * @param properties style options. If <code>null</code> or not set default
     * values are chose.
     */
    void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    private void throwUnknownPropertyException(String property) {
        throw new NullPointerException("No property with name '" + property + "'.");
    }

}
