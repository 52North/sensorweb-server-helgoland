/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.sensorweb.spi.v1;

import com.vividsolutions.jts.geom.Geometry;
import org.n52.web.exception.BadQueryParameterException;
import static org.n52.io.crs.CRSUtils.DEFAULT_CRS;
import static org.n52.io.crs.CRSUtils.createEpsgForcedXYAxisOrder;
import static org.n52.io.crs.CRSUtils.createEpsgStrictAxisOrder;

import org.n52.io.request.IoParameters;
import org.n52.io.crs.CRSUtils;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Point;
import org.n52.io.geojson.old.GeojsonFeature;
import org.n52.io.geojson.old.GeojsonPoint;

public abstract class TransformationService {

    /**
     * @param feature the feature to transform.
     * @param query the query containing CRS and how to handle axes order.
     * @throws BadQueryParameterException if an invalid CRS has been passed in.
     */
    protected void transformInline(GeojsonFeature feature, IoParameters query) {
        String crs = query.getCrs();
        if (DEFAULT_CRS.equals(crs)) {
            return; // no need to transform
        }
        try {
            CRSUtils crsUtils = query.isForceXY()
                    ? createEpsgForcedXYAxisOrder()
                    : createEpsgStrictAxisOrder();
            GeojsonPoint geojsonPoint = (GeojsonPoint) feature.getGeometry();
            Point point = crsUtils.convertToPointFrom(geojsonPoint);
            feature.setGeometry(crsUtils.convertToGeojsonFrom(point, crs));
        } catch (TransformException e) {
            throw new RuntimeException("Could not transform to requested CRS: " + crs, e);
        } catch (FactoryException e) {
            throw new BadQueryParameterException("Could not create CRS " + crs + ".", e);
        }
    }

}
