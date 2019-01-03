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
package org.n52.io.response;

import java.util.Locale;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public enum GeometryType {

    PLATFORM_SITE,
    PLATFORM_TRACK,
    OBSERVED_STATIC,
    OBSERVED_DYNAMIC;

    private static final String SEPARATOR = "_";

    public String createId(String id) {
        return getGeometryType() + SEPARATOR + id;
    }

    public String getGeometryType() {
        return this.name().toLowerCase();
    }

    public static boolean isPlatformGeometryId(String id) {
        return startsWith("platform", id);
    }

    public static boolean isObservedGeometryId(String id) {
        return startsWith("observed", id);
    }

    private static boolean startsWith(String prefix, String id) {
        final String idPrefix = extractPrefix(id);
        if (!isKnownType(idPrefix)) {
            return false;
        }
        return id.toLowerCase().startsWith(prefix);
    }

    public static boolean isSiteId(String id) {
        return hasSuffix("site", id);
    }

    public static boolean isTrackId(String id) {
        return hasSuffix("track", id);
    }

    public static boolean isStaticId(String id) {
        return hasSuffix("static", id);
    }

    public static boolean isDynamicId(String id) {
        return hasSuffix("dynamic", id);
    }

    private static boolean hasSuffix(String suffix, String id) {
        final String idPrefix = extractPrefix(id);
        if (!isKnownType(idPrefix)) {
            return false;
        }
        final GeometryType geometryType = toInstance(idPrefix);
        return geometryType.getGeometryType().endsWith(suffix);
    }

    public static boolean isKnownType(String typeName) {
        for (GeometryType type : values()) {
            if (type.getGeometryType().equalsIgnoreCase(typeName)) {
                return true;
            }
        }
        return false;
    }

    public static String extractId(String id) {
        String replacedId = id;
        for (GeometryType geometryType : values()) {
            replacedId = replacedId.replaceAll(geometryType.getGeometryType() + SEPARATOR, "");
        }
        return replacedId;
    }

    private static String extractPrefix(String id) {
        for (GeometryType geometryType : GeometryType.values()) {
            final String prefix = geometryType.getGeometryType();
            if (id != null && id.toLowerCase().startsWith(prefix)) {
                return prefix;
            }
        }
        return id;
    }

    public static GeometryType toInstance(String id) {
        for (GeometryType geometryType : GeometryType.values()) {
            if (geometryType.name().toLowerCase(Locale.ROOT).startsWith(id)) {
                return geometryType;
            }
        }
        throw new IllegalArgumentException("no type for '" + id + "'.");
    }
}
