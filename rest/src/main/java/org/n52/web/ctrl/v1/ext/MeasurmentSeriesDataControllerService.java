/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.web.ctrl.v1.ext;

import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.measurement.img.MeasurementRenderingContext.createContextWith;
import static org.n52.sensorweb.spi.GeneralizingMeasurementDataService.composeDataService;
import static org.n52.io.measurement.img.MeasurementRenderingContext.createContextForSingleSeries;

import java.net.URI;

import org.n52.io.IoHandler;
import org.n52.io.MimeType;
import org.n52.io.measurement.MeasurementIoFactory;
import org.n52.io.measurement.format.FormatterFactory;
import org.n52.io.measurement.img.MeasurementRenderingContext;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.series.MeasurementData;
import org.n52.io.response.series.MeasurementSeriesOutput;
import org.n52.io.response.series.SeriesDataCollection;
import org.n52.io.series.RenderingContext;
import org.n52.sensorweb.spi.SeriesDataService;

public class MeasurmentSeriesDataControllerService extends SeriesDataControllerService {

    @Override
    public IoHandler<MeasurementData> getIoHandler(RequestStyledParameterSet parameters, IoParameters map, MimeType mimeType, URI rootResource) {
        String[] seriesIds = parameters.getSeriesIds();
        OutputCollection<MeasurementSeriesOutput> metadatas = getMetadataService().getParameters(seriesIds, map);
        MeasurementRenderingContext context = createContextWith(parameters, metadatas.getItems());
        return MeasurementIoFactory.createWith(map).forMimeType(mimeType).withServletContextRoot(rootResource).createIOHandler(context);
    }

    @Override
    public IoHandler<MeasurementData> getIoHandler(RequestParameterSet parameters, IoParameters map, MimeType mimeType, URI rootResource) {
      String seriesId = parameters.getSeriesIds()[0];
      MeasurementSeriesOutput metadata = (MeasurementSeriesOutput)getMetadataService().getParameter(seriesId, map);
      MeasurementRenderingContext context = createContextForSingleSeries(metadata, map);
      return MeasurementIoFactory.createWith(map).forMimeType(mimeType).withServletContextRoot(rootResource).createIOHandler(context);

    }

    @Override
    public IoHandler<MeasurementData> getIoHandler(RequestStyledParameterSet parameters, IoParameters map) {
        String[] seriesIds = parameters.getSeriesIds();
        OutputCollection<MeasurementSeriesOutput> metadatas = getMetadataService().getParameters(seriesIds, map);
        MeasurementRenderingContext context = createContextWith(parameters, metadatas.getItems());
        return MeasurementIoFactory.createWith(map).createIOHandler(context);
    }

    @Override
    public IoHandler<MeasurementData> getIoHandler(RequestSimpleParameterSet parameters, IoParameters map) {
        String seriesId = parameters.getSeriesIds()[0];
        MeasurementSeriesOutput metadata = (MeasurementSeriesOutput)getMetadataService().getParameter(seriesId, map);
        MeasurementRenderingContext context = createContextForSingleSeries(metadata, map);
        return MeasurementIoFactory.createWith(map).createIOHandler(context);
    }

    @Override
    public SeriesDataCollection<?> format(SeriesDataCollection seriesData, String format) {
        return FormatterFactory.createFormatterFactory(format).create().format(seriesData);
    }

    @Override
    public SeriesDataCollection getSeriesData(RequestSimpleParameterSet parameters) {
        return parameters.isGeneralize()
            ? composeDataService(getDataService()).getSeriesData(parameters)
            : getDataService().getSeriesData(parameters);
    }

}
