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
     * Subpath identifying the extension.
     */
    static final String EXT = "/ext";

    /**
     * Subpath identifying the api version and extension.
     */
//    static final String API_VERSION_EXTENSION_PATH = API_VERSION_PATH + EXT;
    static final String API_VERSION_EXTENSION_PATH = API_VERSION_PATH;


    /**
     * Subpath identifying a collection of services available.
     */
    static final String EXT_COLLECTION_SERVICES = API_VERSION_EXTENSION_PATH + "/services";

    /**
     * Subpath identifying a collection of categories available.
     */
    String EXT_COLLECTION_CATEGORIES = API_VERSION_EXTENSION_PATH + "/categories";

    /**
     * Subpath identifying a collection of offerings available.
     */
    String EXT_COLLECTION_OFFERINGS = API_VERSION_EXTENSION_PATH + "/offerings";

    /**
     * Subpath identifying a collection of features available.
     */
    String EXT_COLLECTION_FEATURES = API_VERSION_EXTENSION_PATH + "/features";

    /**
     * Subpath identifying a collection of procedures available.
     */
    String EXT_COLLECTION_PROCEDURES = API_VERSION_EXTENSION_PATH + "/procedures";

    /**
     * Subpath identifying a collection of phenomenons available.
     */
    String EXT_COLLECTION_PHENOMENA = API_VERSION_EXTENSION_PATH + "/phenomena";

    /**
     * Subpath identifying a collection of platforms available.
     */
    String COLLECTION_PLATFORMS = API_VERSION_PATH + "/platforms";

    /**
     * Subpath identifying a collection of series metadata available.
     */
    String COLLECTION_SERIES = API_VERSION_PATH + "/series";

    /**
     * Subpath identifying a collection of series metadata available.
     */
    String COLLECTION_GEOMETRIES = API_VERSION_PATH + "/geometries";


}
