package org.n52.io.v1.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TimeseriesMetadata implements Serializable {

    private static final long serialVersionUID = 7422416308386483575L;
    
    private Map<String, TimeseriesData> referenceValues = new HashMap<String, TimeseriesData>();
    
    // TODO handle metadata
    
}
