
package org.n52.io.type.quantity;

import org.n52.io.format.ResultTimeClassifiedData;
import org.n52.io.format.ResultTimeFormatter;
import org.n52.io.handler.IoProcessChain;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.type.quantity.format.FormatterFactory;
import org.n52.io.type.quantity.generalize.GeneralizingQuantityService;
import org.n52.series.spi.srv.DataService;

final class QuantityIoProcessChain implements IoProcessChain<Data<QuantityValue>> {

    private final DataService<Data<QuantityValue>> dataService;

    private final IoParameters parameters;

    QuantityIoProcessChain(DataService<Data<QuantityValue>> dataService, IoParameters parameters) {
        this.dataService = dataService;
        this.parameters = parameters;
    }

    @Override
    public DataCollection<Data<QuantityValue>> getData() {
        boolean generalize = parameters.isGeneralize();
        DataService<Data<QuantityValue>> service = generalize
                ? new GeneralizingQuantityService(dataService)
                : dataService;
        return service.getData(parameters);
    }

    @Override
    public DataCollection< ? > getProcessedData() {
        return parameters.shallClassifyByResultTimes()
                ? formatAccordingToResultTimes()
                : formatValueOutputs();
    }

    private DataCollection<ResultTimeClassifiedData<AbstractValue< ? >>> formatAccordingToResultTimes() {
        return new ResultTimeFormatter<Data<QuantityValue>>().format(getData());
    }

    private DataCollection< ? > formatValueOutputs() {
        FormatterFactory factory = FormatterFactory.createFormatterFactory(parameters);
        DataCollection<Data<QuantityValue>> data = getData();
        return factory.create()
                      .format(data);
    }

}
