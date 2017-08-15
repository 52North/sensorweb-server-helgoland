
package org.n52.io.format;

import java.util.Map.Entry;

import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;

public class ResultTimeFormatter<I extends Data< ? extends AbstractValue< ? >>>
        implements DataFormatter<I, ResultTimeClassifiedData<AbstractValue< ? >>> {

    @Override
    public DataCollection<ResultTimeClassifiedData<AbstractValue< ? >>> format(DataCollection<I> toFormat) {
        DataCollection<ResultTimeClassifiedData<AbstractValue< ? >>> formatted = new DataCollection<>();
        for (Entry<String, I> entry : toFormat.getAllSeries()
                                              .entrySet()) {
            String datasetId = entry.getKey();
            formatted.addNewSeries(datasetId, createResultTimeData(entry.getValue()));
        }
        return formatted;
    }

    private ResultTimeClassifiedData<AbstractValue< ? >> createResultTimeData(Data< ? extends AbstractValue< ? >> data) {
        ResultTimeClassifiedData<AbstractValue< ? >> rtData = new ResultTimeClassifiedData<>();
        for (AbstractValue< ? > value : data.getValues()) {
            rtData.classifyValue(value);
        }
        return rtData;
    }

}
