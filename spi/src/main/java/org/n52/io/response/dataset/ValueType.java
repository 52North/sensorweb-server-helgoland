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
package org.n52.io.response.dataset;

public class ValueType {

    public static final String DEFAULT_VALUE_TYPE = "quantity";

    private static final String SEPERATOR = "_";

    public static String extractType(String id) {
        return extractType(id, null);
    }

    public static String extractType(String id, String defaultValue) {
        String fallback = defaultValue == null || defaultValue.isEmpty()
                ? DEFAULT_VALUE_TYPE
                : defaultValue;
        if (id == null || id.isEmpty()) {
            return fallback;
        }
        int separatorIndex = id.indexOf(SEPERATOR);
        return separatorIndex >= 0
                ? id.substring(0, separatorIndex)
                : fallback;
    }

    public static String extractId(String id) {
        if (id == null || id.isEmpty()) {
            return id;
        }
        return id.substring(id.indexOf(SEPERATOR) + 1);
    }

    public static String createId(String type, String id) {
        if (id == null) {
            throw new NullPointerException("Cannot create from null id.");
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Cannot create from empty id.");
        }
        return assertNotNullOrEmpty(type)
                ? type.toLowerCase().concat(SEPERATOR).concat(id)
                : id;
    }

    private static boolean assertNotNullOrEmpty(String type) {
        return !(type == null || type.isEmpty());
    }
}
