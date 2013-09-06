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
import static org.n52.io.crs.CRSUtils.createEpsgStrictAxisOrder;

import org.n52.io.crs.CRSUtils;
import org.n52.io.v1.data.StationOutput;
import org.n52.web.v1.ctrl.QueryMap;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Point;

public class TransformingStationService implements ParameterService<StationOutput> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformingStationService.class);
    
    private ParameterService<StationOutput> composedService;
    
    public TransformingStationService(ParameterService<StationOutput> toCompose) {
        this.composedService = toCompose;
    }

    @Override
    public StationOutput[] getExpandedParameters(QueryMap query) {
        StationOutput[] stations = composedService.getExpandedParameters(query);
        return transformStations(query, stations);
    }

    @Override
    public StationOutput[] getCondensedParameters(QueryMap query) {
        StationOutput[] stations = composedService.getCondensedParameters(query);
        return transformStations(query, stations);
    }

    @Override
    public StationOutput[] getParameters(String[] items) {
        return composedService.getParameters(items);
    }

    @Override
    public StationOutput[] getParameters(String[] items, QueryMap query) {
        StationOutput[] stations = composedService.getParameters(items, query);
        return transformStations(query, stations);
    }
    
    @Override
    public StationOutput getParameter(String item) {
        return composedService.getParameter(item);
    }

    @Override
    public StationOutput getParameter(String item, QueryMap query) {
        StationOutput station = composedService.getParameter(item, query);
        transformInline(station, query.getCrs());
        return station;
    }

    private StationOutput[] transformStations(QueryMap query, StationOutput[] stations) {
        for (StationOutput stationOutput : stations) {
            transformInline(stationOutput, query.getCrs());
        }
        return stations;
    }

    private void transformInline(StationOutput stationOutput, String targetCrs) {
        if (DEFAULT_CRS.equals(targetCrs)) {
            return; // no need to transform
        }
        try {
            CRSUtils crsUtils = createEpsgStrictAxisOrder(); // TODO future: strictXY parameter
            Point point = crsUtils.convertToPointFrom(stationOutput.getGeometry());
            stationOutput.setGeometry(crsUtils.convertToGeojsonFrom(point, targetCrs));
        } catch(TransformException e) {
            LOGGER.warn("Could not transform to requested CRS: {}", targetCrs, e);
        }
        catch (FactoryException e) {
            LOGGER.error("Could not create CRS {}.", targetCrs, e);
        }
    }

}
