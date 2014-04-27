/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.sensorweb.v1.spi;

import org.n52.io.IoParameters;

/**
 * A generic service to get arbitrary parameters from the underlying data access implementation. In general the access
 * can result either in compact or detailed outputs. However, each query can be controlled in more detail by a parameter
 * values hold by a {@link IoParameters}.
 *
 * @param <T> the actual parameter type.
 */
public interface ParameterService<T> {

    /**
     * @param query query parameters to control the output.
     * @return an array of expanded items.
     */
    T[] getExpandedParameters(IoParameters query);

    /**
     * @param query query parameters to control the output.
     * @return an array of compact items.
     */
    T[] getCondensedParameters(IoParameters query);

    /**
     * Gets the requested items with respect to default query settings. Use
     * {@link #getParameters(String[], IoParameters)} to control the output items.
     *
     * @param items a subset of item ids which are of interest.
     * @return an array of expanded items which are of interest. Not known ids will be ignored.
     */
    T[] getParameters(String[] items);

    /**
     * Gets the requested items with respect to the given query parameters. <b>Note</b>, that implementations may be
     * aware of parameters not specified by the official timeseries API. However, at least all officially specified
     * query parameters should be considered by all implementations.
     *
     * @param items a subset of item ids which are of interest.
     * @param query query parameters to control the output.
     * @return an array of expanded items which are of interest. Not known ids will be ignored.
     */
    T[] getParameters(String[] items, IoParameters query);

    /**
     * Gets the requested item with respect to default query settings. Use {@link #getParameter(String, IoParameters)}
     * to control the output item.
     *
     * @param item the item id of interest.
     * @return an expanded items of interest.
     */
    T getParameter(String item);

    /**
     * Gets the requested item with respect to the given query parameters. <b>Note</b>, that implementations may be
     * aware of parameters not specified by the official timeseries API. However, at least all officially specified
     * query parameters should be considered by all implementations.
     *
     * @param item the item id of interest.
     * @param query query parameters to control the output.
     * @return an expanded items of interest.
     */
    T getParameter(String item, IoParameters query);

}
