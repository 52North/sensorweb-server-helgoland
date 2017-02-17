/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.da;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.spi.search.SearchResult;

public interface OutputAssembler<T> {

    List<T> getAllCondensed(DbQuery parameters) throws DataAccessException;

    List<T> getAllCondensed(DbQuery parameters, Session session) throws DataAccessException;

    List<T> getAllExpanded(DbQuery parameters) throws DataAccessException;

    List<T> getAllExpanded(DbQuery parameters, Session session) throws DataAccessException;

    T getInstance(String id, DbQuery parameters) throws DataAccessException;

    T getInstance(String id, DbQuery parameters, Session session) throws DataAccessException;

    Collection<SearchResult> searchFor(IoParameters parameters);

    List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, DbQuery query);

    boolean exists(String id, DbQuery query) throws DataAccessException;

}
