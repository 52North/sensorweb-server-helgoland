/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.web.v1.ctrl;

/**
 * The {@link RestfulUrls} serves as markup interface, so that each controller instance uses the same URL
 * subpaths.<br/>
 * <br/>
 * <b>Note:</b> Do not code against this type.
 */
public interface RestfulUrls {
    
    /**
     * The base URL to be used as RESTful entry point.
     */
    static final String API_VERSION_PATH = "/v1";

    /**
     * Subpath identifying a collection of services availabe.
     */
    static final String COLLECTION_SERVICES = API_VERSION_PATH + "/services";

    /**
     * Subpath identifying a collection of categories availabe.
     */
    static final String COLLECTION_CATEGORIES = API_VERSION_PATH + "/categories";

    /**
     * Subpath identifying a collection of offerings available.
     */
    static final String COLLECTION_OFFERINGS = API_VERSION_PATH + "/offerings";

    /**
     * Subpath identifying a collection of features available.
     */
    static final String COLLECTION_FEATURES = API_VERSION_PATH + "/features";

    /**
     * Subpath identifying a collection of procedures available.
     */
    static final String COLLECTION_PROCEDURES = API_VERSION_PATH + "/procedures";

    /**
     * Subpath identifying a collection of phenomenons available.
     */
    static final String COLLECTION_PHENOMENA = API_VERSION_PATH + "/phenomena";

    /**
     * Subpath identifying a collection of stations available.
     */
    static final String COLLECTION_STATIONS = API_VERSION_PATH + "/stations";

    /**
     * Subpath identifying a collection of timeseries metadata available.
     */
    static final String COLLECTION_TIMESERIES = API_VERSION_PATH + "/timeseries";

    /**
     * Subpath identifying the search.
     */
    static final String SEARCH = API_VERSION_PATH + "/search";
}
