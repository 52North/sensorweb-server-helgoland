/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
    static final String DEFAULT_PATH = "/v1";

    /**
     * Subpath identifying a collection of services availabe.
     */
    static final String COLLECTION_SERVICES = "services";
    
    /**
     * Subpath identifying a collection of categories availabe.
     */
    static final String COLLECTION_CATEGORIES = "categories";
    
    /**
     * Subpath identifying a collection of offerings available.
     */
    static final String COLLECTION_OFFERINGS = "offerings";

    /**
     * Subpath identifying a collection of features available.
     */
    static final String COLLECTION_FEATURES = "features";
    
    /**
     * Subpath identifying a collection of procedures available.
     */
    static final String COLLECTION_PROCEDURES = "procedures";

    /**
     * Subpath identifying a collection of phenomenons available.
     */
    static final String COLLECTION_PHENOMENA = "phenomena";

    /**
     * Subpath identifying a collection of stations available.
     */
    static final String COLLECTION_STATIONS = "stations";
    
    /**
     * Subpath identifying a collection of timeseries metadata available.
     */
    static final String COLLECTION_TIMESERIES = "timeseries";

}
