package org.n52.io.format;


/**
 * Represents the identity transformation. Input is equal to output.
 */
public class TvpFormatter implements TimeseriesDataFormatter<TvpDataCollection> {

    @Override
    public TvpDataCollection format(TvpDataCollection toFormat) {
        return toFormat;
    }

}
