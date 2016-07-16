package org.n52.io;

import org.n52.io.response.dataset.Data;

public abstract class CsvIoHandler<T extends Data> implements IoHandler<T> {
    
    // TODO 
    
    protected abstract String[] getHeader();
    
}
