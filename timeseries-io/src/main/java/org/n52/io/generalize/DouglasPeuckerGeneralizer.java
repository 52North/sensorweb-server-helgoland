
package org.n52.io.generalize;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Properties;

import org.n52.io.format.TvpDataCollection;
import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a generalizer using the Douglas-Peucker Algorithm
 * 
 * Characteristic measurement values are picked depending on a given tolerance value. Values that differ less
 * than this tolerance value from an ideal line between some minima and maxima will be dropped.
 */
public class DouglasPeuckerGeneralizer implements Generalizer {

    public static final Logger LOGGER = LoggerFactory.getLogger(DouglasPeuckerGeneralizer.class);

    /**
     * Config-key for {@link #maxEntries} of entries.
     */
    public static final String MAX_ENTRIES = "MAX_ENTRIES";

    /**
     * Config-key for {@link #reductionRate}.
     */
    public static final String REDUCTION_RATE = "REDUCTION_RATE";

    /**
     * Config-key for the {@link #toleranceValue}.
     */
    public static String TOLERANCE_VALUE = "TOLERANCE_VALUE";

    /**
     * {@link #maxEntries} is the value for the maximum points the generalizer will handle, otherwise an
     * exception will be thrown; -1 is unlimited
     */
    protected int maxEntries = -1; // fallback default

    /**
     * estimated reduction rate for this use case, where {@link #reductionRate} = 3 means the time series is
     * reduced to 1/3 of it's size; -1 means there is no proper empirical value
     */
    protected int reductionRate = -1; // fallback default

    /**
     * Absolute tolerance value.
     */
    protected double toleranceValue = 0.1; // fallback default

    private TvpDataCollection dataToGeneralize;

    public static Generalizer createNonConfigGeneralizer(TvpDataCollection data) {
        return new DouglasPeuckerGeneralizer(data, new Properties());
    }

    public static Generalizer createGeneralizer(TvpDataCollection data, Properties configuration) {
        return new DouglasPeuckerGeneralizer(data, configuration);
    }

    /**
     * Creates a new instance. Use static constructors for instantiation.
     * 
     * @param data
     *        timeseries data collection to generalize.
     * @param configuration
     *        Configuration properties. If <code>null</code> a fallback configuration will be used.
     */
    private DouglasPeuckerGeneralizer(TvpDataCollection data, Properties configuration) {
        this.dataToGeneralize = data;
        configure(configuration);
    }

    private void configure(Properties configuration) {
        try {
            maxEntries = configuration.containsKey(MAX_ENTRIES) ? parseInt(configuration.getProperty(MAX_ENTRIES)) : -1;
            reductionRate = configuration.containsKey(REDUCTION_RATE) ? parseInt(configuration.getProperty(REDUCTION_RATE))
                                                                     : -1;
            toleranceValue = configuration.containsKey(TOLERANCE_VALUE) ? parseDouble(configuration.getProperty(TOLERANCE_VALUE))
                                                                       : 0.1;
        }
        catch (NumberFormatException ne) {
            LOGGER.error("Error while reading properties!  Using fallback defaults.", ne);
            throw new IllegalStateException("Error while reading properties! Using fallback defaults.");
        }
    }

    @Override
    public TvpDataCollection generalize() throws GeneralizerException {
        TvpDataCollection generalizedDataCollection = new TvpDataCollection();
        for (String timeseriesId : dataToGeneralize.getAllTimeseries().keySet()) {
            TimeseriesData timeseries = dataToGeneralize.getTimeseries(timeseriesId);
            generalizedDataCollection.addNewTimeseries(timeseriesId, generalize(timeseries));
        }
        return generalizedDataCollection;
    }

    private TimeseriesData generalize(TimeseriesData timeseries) throws GeneralizerException {
        TimeseriesValue[] originalValues = timeseries.getValues();
        if (originalValues.length < 3 || toleranceValue <= 0) {
            return timeseries;
        }

        if (maxEntries != -1 && originalValues.length > maxEntries) {
            throw new GeneralizerException("Maximum number of entries exceeded (" + originalValues.length + ">" + maxEntries + ")!");
        }

        TimeseriesData generalizedTimeseries = new TimeseriesData();
        TimeseriesValue[] generalizedValues = recursiveGeneralize(timeseries);
        generalizedTimeseries.addValues(generalizedValues);

        // add first element if new list is empty
        if (generalizedValues.length == 0 && originalValues.length > 0) {
            generalizedTimeseries.addValues(originalValues[0]);
        }

        // add the last one if not already contained!
        if (generalizedValues.length > 0 && originalValues.length > 0) {
            TimeseriesValue lastOriginialValue = originalValues[originalValues.length - 1];
            TimeseriesValue lastGeneralizedValue = generalizedValues[generalizedValues.length - 1];
            if (lastGeneralizedValue.getTimestamp() != lastOriginialValue.getTimestamp()) {
                generalizedTimeseries.addValues(lastOriginialValue);
            }
        }
        return generalizedTimeseries;
    }

    private TimeseriesValue[] recursiveGeneralize(TimeseriesData timeseries) {
        TimeseriesValue[] values = timeseries.getValues();
        TimeseriesValue startValue = getFirstValue(timeseries);
        TimeseriesValue endValue = getLastValue(timeseries);
        Line2D.Double line = createTendencyLine(startValue, endValue);

        // find the point of maximum distance to the line
        int index = 0;
        double maxDist = 0;
        double distance;
        
        // start and end value are not mentioned
        for (int i = 1; i < values.length - 1; i++) {
            TimeseriesValue timeseriesValue = values[i];
            distance = calculateDistance(line, timeseriesValue);
            if (distance > maxDist) {
                index = i;
                maxDist = distance;
            }
        }

        if (maxDist < toleranceValue) {
            return timeseries.getValues();
        } else {
            // split and handle both parts separately
            TimeseriesData generalizedData = new TimeseriesData();
            TimeseriesData firstPartToBeGeneralized = new TimeseriesData();
            TimeseriesData restPartToBeGeneralized = new TimeseriesData();
            firstPartToBeGeneralized.addValues(Arrays.copyOfRange(values, 0, index));
            restPartToBeGeneralized.addValues(Arrays.copyOfRange(values, index + 1, values.length));
            generalizedData.addValues(recursiveGeneralize(firstPartToBeGeneralized));
            generalizedData.addValues(recursiveGeneralize(restPartToBeGeneralized));
            return generalizedData.getValues();
        }

    }

    private double calculateDistance(Line2D.Double line, TimeseriesValue timeseriesValue) {
        return line.ptLineDist(createPoint(timeseriesValue));
    }

    private Point2D.Double createPoint(TimeseriesValue timeseriesValue) {
        Long timestamp = timeseriesValue.getTimestamp();
        double value = timeseriesValue.getValue();

        Point2D.Double p = new Point2D.Double();
        p.setLocation(timestamp, value);
        return p;
    }

    private Line2D.Double createTendencyLine(TimeseriesValue start, TimeseriesValue end) {
        Long startTime = start.getTimestamp();
        double startValue = start.getValue();
        Long endTime = end.getTimestamp();
        double endValue = end.getValue();
        return new Line2D.Double(startTime, startValue, endTime, endValue);
    }

    private TimeseriesValue getFirstValue(TimeseriesData timeseries) {
        TimeseriesValue[] values = timeseries.getValues();
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Timeseries must not be empty.");
        }
        return values[0];
    }

    private TimeseriesValue getLastValue(TimeseriesData timeseries) {
        TimeseriesValue[] values = timeseries.getValues();
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Timeseries must not be empty.");
        }
        return values[values.length - 1];
    }

}
