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
package org.n52.web.ctrl;

/**
 * <p>
 * The {@link UrlSettings} serves as markup interface, so that each controller
 * instance uses the same URL subpaths.</p>
 *
 * <p>
 * <b>Note:</b> Do not code against this type.</p>
 */
public interface UrlSettings {

    /**
     * Subpath identifying the search.
     */
    String SEARCH = "/search";

    /**
     * Subpath identifying a collection of services availabe.
     */
    String COLLECTION_SERVICES = "/services";

    /**
     * Subpath identifying a collection of categories availabe.
     */
    String COLLECTION_CATEGORIES = "/categories";

    /**
     * Subpath identifying a collection of offerings available.
     */
    String COLLECTION_OFFERINGS = "/offerings";

    /**
     * Subpath identifying a collection of features available.
     */
    String COLLECTION_FEATURES = "/features";

    /**
     * Subpath identifying a collection of procedures available.
     */
    String COLLECTION_PROCEDURES = "/procedures";

    /**
     * Subpath identifying a collection of phenomenons available.
     */
    String COLLECTION_PHENOMENA = "/phenomena";

    /**
     * Subpath identifying a collection of stations available.
     */
    String COLLECTION_STATIONS = "/stations";

    /**
     * Subpath identifying a collection of timeseries metadata available.
     */
    String COLLECTION_TIMESERIES = "/timeseries";

    /**
     * Subpaths identifying platforms collections available.
     */
    String COLLECTION_PLATFORMS = "/platforms";

    /**
     * Subpaths identifying datasets collections available.
     */
    String COLLECTION_DATASETS = "/datasets";

    /**
     * Subpaths identifying geometries collections available.
     */
    String COLLECTION_GEOMETRIES = "/geometries";
}
