package org.n52.io;

import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;

public interface IoProcessChain<T extends Data> {

    DataCollection<T> getData();

    DataCollection<? extends Data> getProcessedData();

}
