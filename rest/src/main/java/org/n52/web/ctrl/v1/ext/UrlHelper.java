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
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.EXT_COLLECTION_SERVICES);
    }

    public String getCategoriesHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.EXT_COLLECTION_CATEGORIES);
    }

    public String getOfferingsHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.EXT_COLLECTION_OFFERINGS);
    }

    public String getFeaturesHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.EXT_COLLECTION_FEATURES);
    }

    public String getProceduresHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.EXT_COLLECTION_PROCEDURES);
    }

    public String getPhenomenaHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.EXT_COLLECTION_PHENOMENA);
    }

    public String getPlatformsHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_PLATFORMS);
    }

    public String getSeriesHrefBaseUrl(String hrefBase) {
        return getVersionLessHrefBaseURl(hrefBase).concat(ExtUrlSettings.COLLECTION_SERIES);
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
