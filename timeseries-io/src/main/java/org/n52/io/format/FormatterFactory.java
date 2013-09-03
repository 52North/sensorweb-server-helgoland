package org.n52.io.format;

public class FormatterFactory {
    
    private String format;
    
    private FormatterFactory(String format) {
        this.format = format;
    }
    
    public TimeseriesDataFormatter<?> create() {
        if ("highcharts".equals(format)) {
            return new HighchartFormatter();
        } else {
            return new TvpFormatter();
        }
    }
    
    public static FormatterFactory createFormatterFactory(String format) {
        return new FormatterFactory(format);
    }
}
