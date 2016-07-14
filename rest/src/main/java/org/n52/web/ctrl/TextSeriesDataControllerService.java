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
package org.n52.web.ctrl;

import static org.n52.io.text.TextObservationRenderingContext.createContextForSingleSeries;
import static org.n52.io.text.TextObservationRenderingContext.createContextWith;

import java.net.URI;

import org.n52.io.IoHandler;
import org.n52.io.MimeType;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.series.DataCollection;
import org.n52.io.response.series.text.TextObservationData;
import org.n52.io.response.series.text.TextObservationSeriesOutput;
import org.n52.io.text.TextObservationIoFactory;
import org.n52.io.text.TextObservationRenderingContext;
import org.n52.io.text.format.FormatterFactory;

public class TextSeriesDataControllerService extends DatasetServiceBundle {

    @Override
    public IoHandler<TextObservationData> getIoHandler(RequestStyledParameterSet parameters, IoParameters map, MimeType mimeType, URI rootResource) {
        String[] seriesIds = parameters.getSeriesIds();
        OutputCollection<TextObservationSeriesOutput> metadatas = getMetadataService().getParameters(seriesIds, map);
        TextObservationRenderingContext context = createContextWith(parameters, metadatas.getItems());
        return TextObservationIoFactory.createWith(map).forMimeType(mimeType).withServletContextRoot(rootResource).createIOHandler(context);
    }

    @Override
    public IoHandler<TextObservationData> getIoHandler(RequestParameterSet parameters, IoParameters map, MimeType mimeType, URI rootResource) {
      String seriesId = parameters.getSeriesIds()[0];
      TextObservationSeriesOutput metadata = (TextObservationSeriesOutput)getMetadataService().getParameter(seriesId, map);
      TextObservationRenderingContext context = createContextForSingleSeries(metadata, map);
      return TextObservationIoFactory.createWith(map).forMimeType(mimeType).withServletContextRoot(rootResource).createIOHandler(context);

    }

    @Override
    public IoHandler<TextObservationData> getIoHandler(RequestStyledParameterSet parameters, IoParameters map) {
        String[] seriesIds = parameters.getSeriesIds();
        OutputCollection<TextObservationSeriesOutput> metadatas = getMetadataService().getParameters(seriesIds, map);
        TextObservationRenderingContext context = createContextWith(parameters, metadatas.getItems());
        return TextObservationIoFactory.createWith(map).createIOHandler(context);
    }

    @Override
    public IoHandler<TextObservationData> getIoHandler(RequestSimpleParameterSet parameters, IoParameters map) {
        String seriesId = parameters.getSeriesIds()[0];
        TextObservationSeriesOutput metadata = (TextObservationSeriesOutput)getMetadataService().getParameter(seriesId, map);
        TextObservationRenderingContext context = createContextForSingleSeries(metadata, map);
        return TextObservationIoFactory.createWith(map).createIOHandler(context);
    }

    @Override
    public DataCollection<?> format(DataCollection seriesData, String format) {
        return FormatterFactory.createFormatterFactory(format).create().format(seriesData);
    }

    @Override
    public DataCollection getSeriesData(RequestSimpleParameterSet parameters) {
        return getDataService().getData(parameters);
    }

}
