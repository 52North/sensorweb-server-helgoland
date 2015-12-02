package org.n52.series.db.da;

import org.n52.sensorweb.spi.ParameterService;

public interface ShutdownParameterService<T> extends ParameterService<T> {
    
    public void shutdown();
}
