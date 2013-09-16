/**
 * ﻿Copyright (C) 2013
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

import static org.n52.io.crs.CRSUtils.DEFAULT_CRS;
import static org.n52.io.crs.CRSUtils.createEpsgForcedXYAxisOrder;
import static org.n52.io.crs.CRSUtils.createEpsgStrictAxisOrder;

import org.n52.io.IoParameters;
import org.n52.io.crs.CRSUtils;
import org.n52.io.v1.data.StationOutput;
import org.n52.web.BadRequestException;
import org.n52.web.InternalServerException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Point;

public abstract class TransformationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationService.class);

    protected StationOutput[] transformStations(IoParameters query, StationOutput[] stations) {
        for (StationOutput stationOutput : stations) {
            transformInline(stationOutput, query);
        }
        return stations;
    }

    /**
     * @param station
     *        the station to transform.
     * @param query
     *        the query containing CRS and how to handle axes order.
     * @throws InternalServerException
     *         if transformation failed.
     * @throws BadRequestException
     *         if an invalid CRS has been passed in.
     */
    protected void transformInline(StationOutput station, IoParameters query) {
        String crs = query.getCrs();
        if (DEFAULT_CRS.equals(crs)) {
            return; // no need to transform
        }
        try {
            CRSUtils crsUtils = query.isForceXY()
                ? createEpsgForcedXYAxisOrder()
                : createEpsgStrictAxisOrder();
            Point point = crsUtils.convertToPointFrom(station.getGeometry());
            station.setGeometry(crsUtils.convertToGeojsonFrom(point, crs));
        }
        catch (TransformException e) {
            throw new InternalServerException("Could not transform to requested CRS: " + crs, e);
        }
        catch (FactoryException e) {
            throw new BadRequestException("Could not create CRS " + crs + ".", e);
        }
    }

}
