package org.n52.io;

import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.dataset.Data;

public abstract class CsvIoHandler<T extends Data> extends IoHandler<T> {

    public CsvIoHandler(RequestSimpleParameterSet request, IoProcessChain<T> processChain) {
        super(request, processChain);
    }

    // TODO

    protected abstract String[] getHeader();

}
