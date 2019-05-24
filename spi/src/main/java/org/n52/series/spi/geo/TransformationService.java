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

package org.n52.series.spi.geo;

import org.locationtech.jts.geom.Geometry;
import org.n52.io.crs.CRSUtils;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.StationOutput;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationService.class);

    /**
     * @param station
     *        the station to transform
     * @param parameters
     *        the query containing CRS and how to handle axes order
     */
    protected void transformInline(StationOutput station, IoParameters parameters) {
        String crs = parameters.getCrs();
        if (CRSUtils.DEFAULT_CRS.equals(crs)) {
            // no need to transform
            return;
        }
        Geometry geometry = transform(station.getGeometry(), parameters);
        station.setValue(StationOutput.GEOMETRY, geometry, parameters, station::setGeometry);
    }

    public Geometry transform(Geometry geometry, IoParameters query) {
        String crs = query.getCrs();
        if (CRSUtils.DEFAULT_CRS.equals(crs)) {
            // no need to transform
            return geometry;
        }
        return transformGeometry(query, geometry, crs);
    }

    private Geometry transformGeometry(IoParameters query,
                                       Geometry geometry,
                                       String crs)
            throws RuntimeException {
        try {
            CRSUtils crsUtils = query.isForceXY()
                    ? CRSUtils.createEpsgForcedXYAxisOrder()
                    : CRSUtils.createEpsgStrictAxisOrder();
            return geometry != null
                    ? crsUtils.transformInnerToOuter(geometry, crs)
                    : geometry;
        } catch (TransformException e) {
            throwRuntimeException(crs, e);
        } catch (FactoryException e) {
            LOGGER.debug("Couldn't create geometry factory", e);
        }
        return geometry;
    }

    private void throwRuntimeException(String crs, TransformException e) throws RuntimeException {
        throw new RuntimeException("Could not transform to requested CRS: " + crs, e);
    }

}
