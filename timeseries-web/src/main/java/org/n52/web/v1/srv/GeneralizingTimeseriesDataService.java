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

import static org.n52.io.generalize.DouglasPeuckerGeneralizer.createNonConfigGeneralizer;

import org.n52.io.format.TvpDataCollection;
import org.n52.io.generalize.Generalizer;
import org.n52.io.generalize.GeneralizerException;
import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.UndesignedParameterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composes a {@link TimeseriesDataService} instance to generalize requested timeseries data. 
 */
public class GeneralizingTimeseriesDataService implements TimeseriesDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralizingTimeseriesDataService.class);

    private TimeseriesDataService composedService;

    public GeneralizingTimeseriesDataService(TimeseriesDataService toCompose) {
        this.composedService = toCompose;
    }

    @Override
    public TvpDataCollection getTimeseriesData(UndesignedParameterSet parameters) {
        TvpDataCollection ungeneralizedData = composedService.getTimeseriesData(parameters);
        try {
            Generalizer generalizer = createNonConfigGeneralizer(ungeneralizedData);
            TvpDataCollection generalizedData = generalizer.generalize();
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

    public static TimeseriesDataService composeDataService(TimeseriesDataService toCompose) {
        return new GeneralizingTimeseriesDataService(toCompose);
    }

}
