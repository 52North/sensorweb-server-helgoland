package org.n52.sensorweb.spi;

/**
 * @since 2.0.0
 */
public interface RawDataInfo {
    
    /**
     * Check if raw data output is supported
     *
     * @return <code>true</code>, if raw data output is supported
     */
    public boolean supportsRawData();
    
    public RawDataService getRawDataService();
    
}
