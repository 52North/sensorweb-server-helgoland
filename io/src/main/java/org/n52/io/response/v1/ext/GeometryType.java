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
package org.n52.io.response.v1.ext;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public enum GeometryCategory {

    PLATFORM_SITE("platformLocations/sites"),
    PLATFORM_TRACK("platformLocations/tracks"),
    STATIC_OBSERVERATION("observedGeometries/static"),
    DYNAMIC_OBSERVATION("observedGeometries/dynamic");

    private final String category;

    private GeometryCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public String getTypeName() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return getCategory();
    }

    public static boolean isSiteId(String id) {
        return id.startsWith(PLATFORM_SITE.getCategory());
    }

    public static boolean isTrackId(String id) {
        return id.startsWith(PLATFORM_TRACK.getCategory());
    }

    public static boolean isStaticId(String id) {
        return id.endsWith(STATIC_OBSERVERATION.getCategory());
    }

    public static boolean isDynamic(String id) {
        return id.endsWith(DYNAMIC_OBSERVATION.getCategory());
    }

    public static String extractId(String id) {
        if (isSiteId(id)) {
            return id.substring(PLATFORM_SITE.getCategory().length() + 1);
        } else if (isTrackId(id)) {
            return id.substring(PLATFORM_TRACK.getCategory().length() + 1);
        } else if (isStaticId(id)) {
            return id.substring(STATIC_OBSERVERATION.getCategory().length() + 1);
        } else if (isDynamic(id)) {
            return id.substring(DYNAMIC_OBSERVATION.getCategory().length() + 1);
        }
        return id;
    }
}
