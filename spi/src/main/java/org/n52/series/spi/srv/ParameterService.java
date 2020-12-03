/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.series.spi.srv;

import java.util.Comparator;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;

/**
 * A generic service to get arbitrary parameters from the underlying data access implementation. In general
 * the access can result either in compact or detailed outputs. However, each query can be controlled in more
 * detail by a parameter values hold by a {@link IoParameters}.
 *
 * @param <T>
 *        the actual parameter type.
 */
public abstract class ParameterService<T extends ParameterOutput> implements RawDataInfo {

    private RawDataService rawDataService;

    protected OutputCollection<T> createOutputCollection(List<T> results) {
        return new OutputCollection<T>(results) {
            @Override
            protected Comparator<T> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    protected OutputCollection<T> createOutputCollection(T result) {
        return new OutputCollection<T>(result) {
            @Override
            protected Comparator<T> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    protected OutputCollection<T> createOutputCollection() {
        return new OutputCollection<T>() {
            @Override
            protected Comparator<T> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    /**
     * @param query
     *        query parameters to control the output.
     * @return an output collection of expanded items.
     */
    public abstract OutputCollection<T> getExpandedParameters(IoParameters query);

    /**
     * @param query
     *        query parameters to control the output.
     * @return an output collection of compact items.
     */
    public abstract OutputCollection<T> getCondensedParameters(IoParameters query);

    /**
     * Gets the requested items with respect to the given query parameters. <b>Note</b>, that implementations
     * may be aware of parameters not specified by the official timeseries API. However, at least all
     * officially specified query parameters should be considered by all implementations.
     *
     * @param items
     *        a subset of item ids which are of interest.
     * @param query
     *        query parameters to control the output.
     * @return an output collection of expanded items which are of interest. Not known ids will be ignored.
     */
    public abstract OutputCollection<T> getParameters(String[] items, IoParameters query);

    /**
     * Gets the requested item with respect to the given query parameters. <b>Note</b>, that implementations
     * may be aware of parameters not specified by the official timeseries API. However, at least all
     * officially specified query parameters should be considered by all implementations.
     *
     * @param item
     *        the item id of interest.
     * @param query
     *        query parameters to control the output.
     * @return an expanded items of interest.
     */
    public abstract T getParameter(String item, IoParameters query);

    /**
     * If a resource exists or not.
     *
     * @param id
     *        the id of the resource.
     * @param parameters
     *        the query passed along.
     * @return <code>true</code> if the resource exists, <code>false</code> otherwise.
     * @since 2.0.0
     */
    public abstract boolean exists(String id, IoParameters parameters);

    /**
     * Check if raw data output is supported
     *
     * @return <code>true</code>, if raw data output is supported
     */
    @Override
    public boolean supportsRawData() {
        return rawDataService != null;
    }

    @Override
    public RawDataService getRawDataService() {
        return rawDataService;
    }

    public void setRawDataService(RawDataService rawDataService) {
        this.rawDataService = rawDataService;
    }
}
