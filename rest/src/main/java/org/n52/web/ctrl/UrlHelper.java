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
package org.n52.web.ctrl;

public class UrlHelper {

    public String getServicesHrefBaseUrl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.COLLECTION_SERVICES);
    }

    public String getCategoriesHrefBaseUrl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.COLLECTION_CATEGORIES);
    }

    public String getOfferingsHrefBaseUrl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.COLLECTION_OFFERINGS);
    }

    public String getFeaturesHrefBaseUrl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.COLLECTION_FEATURES);
    }

    public String getProceduresHrefBaseUrl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.COLLECTION_PROCEDURES);
    }

    public String getPhenomenaHrefBaseUrl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.COLLECTION_PHENOMENA);
    }

    public String getPlatformsHrefBaseUrl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.COLLECTION_PLATFORMS);
    }

    public String getDatasetsHrefBaseUrl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.COLLECTION_DATASETS);
    }

    public String getGeometriesHrefBaseUrl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.COLLECTION_GEOMETRIES);
    }

    public String getRootHrefBaseURl(String hrefBase) {
        return constructHref(hrefBase, UrlSettings.API_VERSION_PATH);
    }

    public String constructHref(String hrefBase, String path) {
        if (hrefBase == null || hrefBase.isEmpty()) {
            // backwards compatible relative link
            return ".".concat(stripVersionFromPath(path)); 
        }
        hrefBase = hrefBase.endsWith("/")
                ? hrefBase.substring(0, hrefBase.length() - 1)
                : hrefBase;
        return hrefBase.contains(UrlSettings.API_VERSION_PATH)
            ? stripVersionFromHrefBase(hrefBase).concat(path)
            : hrefBase.concat(path);
    }

    protected String stripVersionFromHrefBase(String hrefBase) {
        int versionIndex = hrefBase.lastIndexOf(UrlSettings.API_VERSION_PATH);
        return versionIndex >= 0
                ? hrefBase.substring(0, versionIndex)
                : hrefBase;
    }
    
    protected String stripVersionFromPath(String path) {
        int versionIndex = path.indexOf(UrlSettings.API_VERSION_PATH);
        return versionIndex >= 0
                ? path.substring(UrlSettings.API_VERSION_PATH.length())
                : path;
    }

}
