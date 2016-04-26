/*
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
package org.n52.sensorweb.spi;

import org.n52.sensorweb.spi.SeriesDataService;
import static org.n52.io.request.IoParameters.createFromQuery;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.generalize.Generalizer;
import org.n52.io.generalize.GeneralizerException;
import static org.n52.io.generalize.GeneralizerFactory.createGeneralizer;
import org.n52.io.response.TimeseriesData;
import org.n52.io.request.RequestSimpleParameterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composes a {@link SeriesDataService} instance to generalize requested timeseries data.
 */
public class GeneralizingTimeseriesDataService implements SeriesDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralizingTimeseriesDataService.class);

    private final SeriesDataService composedService;

    public GeneralizingTimeseriesDataService(SeriesDataService toCompose) {
        this.composedService = toCompose;
    }

    @Override
    public TvpDataCollection getSeriesData(RequestSimpleParameterSet parameters) {
        TvpDataCollection ungeneralizedData = composedService.getSeriesData(parameters);
        try {
            Generalizer generalizer = createGeneralizer(createFromQuery(parameters));
            TvpDataCollection generalizedData = generalizer.generalize(ungeneralizedData);
            if (LOGGER.isDebugEnabled()) {
                logGeneralizationAmount(ungeneralizedData, generalizedData);
            }
            return generalizedData;
        }
        catch (GeneralizerException e) {
            LOGGER.error("Could not generalize timeseries collection. Returning original data.", e);
            return ungeneralizedData;
        }
    }

    private void logGeneralizationAmount(TvpDataCollection ungeneralizedData,
                                         TvpDataCollection generalizedData) {
        for (String timeseriesId : ungeneralizedData.getAllTimeseries().keySet()) {
            TimeseriesData originalTimeseries = ungeneralizedData.getTimeseries(timeseriesId);
            TimeseriesData generalizedTimeseries = generalizedData.getTimeseries(timeseriesId);
            int originalAmount = originalTimeseries.getValues().length;
            int generalizedAmount = generalizedTimeseries.getValues().length;
            LOGGER.debug("Generalized timeseries: {} (#{} --> #{}).", timeseriesId, originalAmount, generalizedAmount);
        }
    }

    public static SeriesDataService composeDataService(SeriesDataService toCompose) {
        return new GeneralizingTimeseriesDataService(toCompose);
    }

    @Override
    public boolean supportsRawData() {
        return false;
    }

    @Override
    public RawDataService getRawDataService() {
        return null;
    }
    
    

}
