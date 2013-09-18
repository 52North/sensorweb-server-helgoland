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

import org.n52.io.IoParameters;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.web.ResourceNotFoundException;

public class TransformingTimeseriesService extends TransformationService implements ParameterService<TimeseriesMetadataOutput> {

    private ParameterService<TimeseriesMetadataOutput> composedService;

    public TransformingTimeseriesService(ParameterService<TimeseriesMetadataOutput> toCompose) {
        this.composedService = toCompose;
    }
    
    @Override
    public TimeseriesMetadataOutput[] getExpandedParameters(IoParameters query) {
        TimeseriesMetadataOutput[] metadata = composedService.getExpandedParameters(query);
        return transformStations(query, metadata);
    }

    @Override
    public TimeseriesMetadataOutput[] getCondensedParameters(IoParameters query) {
        TimeseriesMetadataOutput[] metadata = composedService.getCondensedParameters(query);
        return transformStations(query, metadata);
    }

    @Override
    public TimeseriesMetadataOutput[] getParameters(String[] items) {
        TimeseriesMetadataOutput[] metadata = composedService.getParameters(items);
        return transformStations(IoParameters.createDefaults(), metadata);
    }

    @Override
    public TimeseriesMetadataOutput[] getParameters(String[] items, IoParameters query) {
        TimeseriesMetadataOutput[] metadata = composedService.getParameters(items, query);
        return transformStations(query, metadata);
    }

    @Override
    public TimeseriesMetadataOutput getParameter(String item) {
        TimeseriesMetadataOutput metadata = composedService.getParameter(item, IoParameters.createDefaults());
        transformInline(metadata.getStation(), IoParameters.createDefaults());
        return metadata;
    }

    @Override
    public TimeseriesMetadataOutput getParameter(String timeseriesId, IoParameters query) {
        TimeseriesMetadataOutput metadata = composedService.getParameter(timeseriesId, query);
        if (metadata == null) {
            throw new ResourceNotFoundException("The timeseries with id '" + timeseriesId + "' was not found.");
        }
        transformInline(metadata.getStation(), query);
        return metadata;
    }
    
    private TimeseriesMetadataOutput[] transformStations(IoParameters query, TimeseriesMetadataOutput[] metadata) {
        for (TimeseriesMetadataOutput timeseriesMetadata : metadata) {
            transformInline(timeseriesMetadata.getStation(), query);
        }
        return metadata;
    }

}
