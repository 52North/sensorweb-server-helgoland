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

package org.n52.web.v1.srv;

import org.n52.web.v1.ctrl.QueryMap;

public interface ParameterService<T> {

    /**
     * @param query
     *        query parameters to control the output.
     * @return an array of expanded items.
     */
    T[] getExpandedParameters(QueryMap query);

    /**
     * @param query
     *        query parameters to control the output.
     * @return an array of compact items.
     */
    T[] getCondensedParameters(QueryMap query);

    /**
     * Gets the requested items with respect to default query settings. Use
     * {@link #getParameters(String[], QueryMap)} to control the output items.
     * 
     * @param items
     *        a subset of item ids which are of interest.
     * @return an array of expanded items which are of interest. Not known ids will be ignored.
     */
    T[] getParameters(String[] items);

    /**
     * Gets the requested items with respect to the given query parameters. <b>Note</b>, that implementations
     * may be aware of parameters not specified by the official timeseries API. However, at least all
     * officially specified query parameters should be considered by all implementations.
     * 
     * @param items
     *        a subset of item ids which are of interest.
     * @param query
     *        query parameters to control the output.
     * @return an array of expanded items which are of interest. Not known ids will be ignored.
     */
    T[] getParameters(String[] items, QueryMap query);

    /**
     * Gets the requested item with respect to default query settings. Use
     * {@link #getParameter(String, QueryMap)} to control the output item.
     * 
     * @param item
     *        the item id of interest.
     * @return an expanded items of interest.
     */
    T getParameter(String item);

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
    T getParameter(String item, QueryMap query);

}
