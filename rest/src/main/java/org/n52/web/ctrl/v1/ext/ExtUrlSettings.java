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

import org.n52.web.ctrl.v1.RestfulUrls;

/**
 * <p>
 * Serves as markup interface, so that each controller instance uses the same
 * URL subpaths.</p>
 *
 * <p>
 * <b>Note:</b> Do not code against this type.</p>
 */
public interface ExtUrlSettings extends RestfulUrls {

    /**
     * Subpaths identifying services collections available.
     */
    static final String COLLECTION_SERVICES_ALL = API_VERSION_PATH + "/services/all";
    static final String COLLECTION_SERVICES_MOBILE = API_VERSION_PATH + "/services/mobile";
    static final String COLLECTION_SERVICES_INSITU = API_VERSION_PATH + "/services/insitu";
    static final String COLLECTION_SERVICES_REMOTE = API_VERSION_PATH + "/services/remote";
    static final String COLLECTION_SERVICES_STATIONARY = API_VERSION_PATH + "/services/stationary";

    /**
     * Subpaths identifying categories collections available.
     */
    static final String COLLECTION_CATEGORIES_ALL = API_VERSION_PATH + "/categories/all";
    static final String COLLECTION_CATEGORIES_MOBILE = API_VERSION_PATH + "/categories/mobile";
    static final String COLLECTION_CATEGORIES_INSITU = API_VERSION_PATH + "/categories/insitu";
    static final String COLLECTION_CATEGORIES_REMOTE = API_VERSION_PATH + "/categories/remote";
    static final String COLLECTION_CATEGORIES_STATIONARY = API_VERSION_PATH + "/categories/stationary";

    /**
     * Subpaths identifying offerings collections available.
     */
    static final String COLLECTION_OFFERINGS_ALL = API_VERSION_PATH + "/offerings/all";
    static final String COLLECTION_OFFERINGS_MOBILE = API_VERSION_PATH + "/offerings/mobile";
    static final String COLLECTION_OFFERINGS_INSITU = API_VERSION_PATH + "/offerings/insitu";
    static final String COLLECTION_OFFERINGS_REMOTE = API_VERSION_PATH + "/offerings/remote";
    static final String COLLECTION_OFFERINGS_STATIONARY = API_VERSION_PATH + "/offerings/stationary";

    /**
     * Subpaths identifying features collections available.
     */
    static final String COLLECTION_FEATURES_ALL = API_VERSION_PATH + "/features/all";
    static final String COLLECTION_FEATURES_MOBILE = API_VERSION_PATH + "/features/mobile";
    static final String COLLECTION_FEATURES_INSITU = API_VERSION_PATH + "/features/insitu";
    static final String COLLECTION_FEATURES_REMOTE = API_VERSION_PATH + "/features/remote";
    static final String COLLECTION_FEATURES_STATIONARY = API_VERSION_PATH + "/features/stationary";

    /**
     * Subpaths identifying procedures collections available.
     */
    static final String COLLECTION_PROCEDURES_ALL = API_VERSION_PATH + "/procedures/all";
    static final String COLLECTION_PROCEDURES_MOBILE = API_VERSION_PATH + "/procedures/mobile";
    static final String COLLECTION_PROCEDURES_INSITU = API_VERSION_PATH + "/procedures/insitu";
    static final String COLLECTION_PROCEDURES_REMOTE = API_VERSION_PATH + "/procedures/remote";
    static final String COLLECTION_PROCEDURES_STATIONARY = API_VERSION_PATH + "/procedures/stationary";

    /**
     * Subpaths identifying phenomena collections available.
     */
    static final String COLLECTION_PHENOMENA_ALL = API_VERSION_PATH + "/phenomena/all";
    static final String COLLECTION_PHENOMENA_MOBILE = API_VERSION_PATH + "/phenomena/mobile";
    static final String COLLECTION_PHENOMENA_INSITU = API_VERSION_PATH + "/phenomena/insitu";
    static final String COLLECTION_PHENOMENA_REMOTE = API_VERSION_PATH + "/phenomena/remote";
    static final String COLLECTION_PHENOMENA_STATIONARY = API_VERSION_PATH + "/phenomena/stationary";

    /**
     * Subpaths identifying platforms collections available.
     */
    static final String COLLECTION_PLATFORMS = API_VERSION_PATH + "/platforms";
//    static final String COLLECTION_PLATFORMS_ALL = API_VERSION_PATH + "/platforms/all";
//    static final String COLLECTION_PLATFORMS_MOBILE = API_VERSION_PATH + "/platforms/mobile";
//    static final String COLLECTION_PLATFORMS_INSITU = API_VERSION_PATH + "/platforms/insitu";
//    static final String COLLECTION_PLATFORMS_REMOTE = API_VERSION_PATH + "/platforms/remote";
//    static final String COLLECTION_PLATFORMS_STATIONARY = API_VERSION_PATH + "/platforms/stationary";

    /**
     * Subpaths identifying datasets collections available.
     */
    static final String COLLECTION_DATASETS = API_VERSION_PATH + "/datasets";
//    static final String COLLECTION_DATASETS_ALL = API_VERSION_PATH + "/datasets/all";
//    static final String COLLECTION_DATASETS_MOBILE = API_VERSION_PATH + "/datasets/mobile";
//    static final String COLLECTION_DATASETS_INSITU = API_VERSION_PATH + "/datasets/insitu";
//    static final String COLLECTION_DATASETS_REMOTE = API_VERSION_PATH + "/datasets/remote";
//    static final String COLLECTION_DATASETS_STATIONARY = API_VERSION_PATH + "/datasets/stationary";

    /**
     * Subpaths identifying geometries collections available.
     */
    static final String COLLECTION_GEOMETRIES = API_VERSION_PATH + "/geometries";

}
