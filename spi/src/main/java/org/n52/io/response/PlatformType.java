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
package org.n52.io.response;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public enum PlatformType {

    // TODO make more fine granular:
    // MOBILE
    // INSITU
    // STATIONARY
    // REMOTE

    STATIONARY_INSITU,
    STATIONARY_REMOTE,
    MOBILE_INSITU,
    MOBILE_REMOTE;

    public static final String PLATFORM_TYPE_MOBILE = "mobile";

    public static final String PLATFORM_TYPE_STATIONARY = "stationary";

    public static final String PLATFORM_TYPE_INSITU = "insitu";

    public static final String PLATFORM_TYPE_REMOTE = "remote";

    public static final String PLATFORM_TYPE_ALL = "all";

    public String getPlatformType() {
        return name().toLowerCase();
    }

    public String createId(Long id) {
        return getPlatformType() + "_" + Long.toString(id);
    }

    public String createId(String id) {
        return getPlatformType() + "_" + id;
    }

    public boolean isStationary() {
        return this == STATIONARY_INSITU
                || this == STATIONARY_REMOTE;
    }

    public boolean isMobile() {
        return this == PlatformType.MOBILE_INSITU
                || this == MOBILE_REMOTE;
    }

    /**
     * @param id
     *        the id id to extract the type prefix from
     * @return the platform type
     * @throws IllegalArgumentException
     *         if prefix is unknown
     */
    public static PlatformType extractType(String id) {
        if (isStationaryId(id)) {
            return isInsitu(id)
                    ? STATIONARY_INSITU
                    : STATIONARY_REMOTE;
        }
        if (isMobileId(id)) {
            return isInsitu(id)
                    ? MOBILE_INSITU
                    : MOBILE_REMOTE;
        }

        throwUnknownTypeException(id);
        return null;
    }

    public static String extractId(String id) {
        if (isStationaryId(id)) {
            return isInsitu(id)
                    ? extractId(STATIONARY_INSITU, id)
                    : extractId(STATIONARY_REMOTE, id);
        } else if (isMobileId(id)) {
            return isInsitu(id)
                    ? extractId(MOBILE_INSITU, id)
                    : extractId(MOBILE_REMOTE, id);
        } else {
            return id;
        }
    }

    private static String extractId(PlatformType type, String id) {
        final int maxLength = type.getPlatformType()
                                  .length()
                + 1;
        return id.length() >= maxLength
                ? id.substring(maxLength)
                : id;
    }

    public static boolean isStationaryId(String id) {
        return startsWith(PLATFORM_TYPE_STATIONARY, id);
    }

    public static boolean isMobileId(String id) {
        return startsWith(PLATFORM_TYPE_MOBILE, id);
    }

    private static boolean startsWith(String prefix, String id) {
        final String idPrefix = extractPrefix(id);
        if (!isKnownType(idPrefix)) {
            return false;
        }
        return id.toLowerCase()
                 .startsWith(prefix);
    }

    public static boolean isRemoteId(String id) {
        return hasSuffix(PLATFORM_TYPE_REMOTE, id);
    }

    public static boolean isInsitu(String id) {
        return hasSuffix(PLATFORM_TYPE_INSITU, id);
    }

    private static boolean hasSuffix(String suffix, String id) {
        final String idPrefix = extractPrefix(id);
        if (!isKnownType(idPrefix)) {
            return false;
        }
        final PlatformType geometryType = toInstance(idPrefix);
        return geometryType.getPlatformType()
                           .endsWith(suffix);
    }

    public static boolean isKnownType(String typeName) {
        for (PlatformType type : values()) {
            if (type.getPlatformType()
                    .equalsIgnoreCase(typeName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOfKnownType(String id) {
        return isKnownType(extractPrefix(id));
    }

    private static String extractPrefix(String id) {
        for (PlatformType type : PlatformType.values()) {
            final String prefix = type.getPlatformType();
            if (id != null && id.toLowerCase()
                                .startsWith(prefix)) {
                return prefix;
            }
        }
        return id;
    }

    public static PlatformType toInstance(String typeName) {
        for (PlatformType type : values()) {
            if (type.getPlatformType()
                    .equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        throwUnknownTypeException(typeName);
        return null;
    }

    public static PlatformType toInstance(boolean mobile, boolean insitu) {
        if (mobile) {
            return insitu
                    ? MOBILE_INSITU
                    : MOBILE_REMOTE;
        } else {
            return insitu
                    ? STATIONARY_INSITU
                    : STATIONARY_REMOTE;
        }
    }

    private static void throwUnknownTypeException(String id) {
        throw new IllegalArgumentException("no type for '" + id + "'.");
    }

}
