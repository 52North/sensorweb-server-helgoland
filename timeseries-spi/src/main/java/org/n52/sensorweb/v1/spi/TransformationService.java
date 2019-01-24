/**
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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
import org.n52.io.crs.CRSUtils;
import org.n52.io.v1.data.StationOutput;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public abstract class TransformationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationService.class);

    /**
     * @param query the query parameters
     * @param stations the stations to transform
     * @return all transformed stations matching the query
     * @throws BadQueryParameterException if an invalid CRS has been passed in
     */
    protected StationOutput[] transformStations(IoParameters query, StationOutput[] stations) {
        if (stations != null) {
            for (StationOutput stationOutput : stations) {
                transformInline(stationOutput, query);
            }
        }
        return stations;
    }

    /**
     * @param station the station to transform
     * @param parameters the parameters containing CRS and how to handle axes order
     * @throws BadQueryParameterException if an invalid CRS has been passed in
     */
    protected void transformInline(StationOutput station, IoParameters parameters) {
//        String crs = query.getCrs();
//        if (DEFAULT_CRS.equals(crs)) {
//            return; // no need to transform
//        }
//        try {
//            CRSUtils crsUtils = query.isForceXY()
//                    ? createEpsgForcedXYAxisOrder()
//                    : createEpsgStrictAxisOrder();
//            Point point = crsUtils.convertToPointFrom(station.getGeometry());
//            station.setGeometry(crsUtils.convertToGeojsonFrom(point, crs));
//        } catch (TransformException e) {
//            throw new RuntimeException("Could not transform to requested CRS: " + crs, e);
//        } catch (FactoryException e) {
//            throw new BadQueryParameterException("Could not create CRS " + crs + ".", e);
//        }
        String crs = parameters.getCrs();
        if (CRSUtils.DEFAULT_CRS.equals(crs)) {
             // no need to transform
            return;
        }
        Geometry geometry = transform(station.getGeometry(), parameters);
        station.setGeometry(geometry);
    }

    public Geometry transform(Geometry geometry, IoParameters query) {
        String crs = query.getCrs();
        if (CRSUtils.DEFAULT_CRS.equals(crs)) {
             // no need to transform
            return geometry;
        }
        return transformGeometry(query, geometry, crs);
    }

    private Geometry transformGeometry(IoParameters query, Geometry geometry,
            String crs) throws RuntimeException {
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
