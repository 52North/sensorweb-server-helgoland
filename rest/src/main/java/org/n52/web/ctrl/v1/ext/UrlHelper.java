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
package org.n52.web.ctrl.v1.ext;

public class UrlHelper {

    public String getServicesHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_SERVICES_DEFAULT);
    }

    public String getCategoriesHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_CATEGORIES_ALL);
    }

    public String getOfferingsHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_OFFERINGS_ALL);
    }

    public String getFeaturesHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_FEATURES_ALL);
    }

    public String getProceduresHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_PROCEDURES_ALL);
    }

    public String getPhenomenaHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_PHENOMENA_ALL);
    }

    public String getPlatformsHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_PLATFORMS);
    }

    public String getSeriesHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_DATASETS);
    }

    public String getGeometriesHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_GEOMETRIES);
    }

    public String getVersionLessHrefBaseURl(String hrefBase) {
        if (hrefBase.contains(ExtUrlSettings.API_VERSION_PATH)) {
            return hrefBase.substring(0, hrefBase.lastIndexOf(ExtUrlSettings.API_VERSION_PATH));
        }
        return hrefBase;
    }

    public String getRootHrefBaseURl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.API_VERSION_PATH);
    }

}
