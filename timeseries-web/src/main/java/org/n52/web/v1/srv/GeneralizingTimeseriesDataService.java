package org.n52.web.v1.srv;

import static org.n52.io.generalize.DouglasPeuckerGeneralizer.createNonConfigGeneralizer;

import org.n52.io.format.TvpDataCollection;
import org.n52.io.generalize.Generalizer;
import org.n52.io.generalize.GeneralizerException;
import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.UndesignedParameterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        } catch (GeneralizerException e) {
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
