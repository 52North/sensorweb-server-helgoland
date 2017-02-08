package org.n52.proxy.connector;

import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlObject;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.encode.EncoderRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSosConnector {

    @Autowired
    protected DecoderRepository decoderRepository;

    @Autowired
    protected EncoderRepository encoderRepository;

    public String getConnectorName() {
        return getClass().getName();
    }

    public boolean matches(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        if (config.getConnector() != null){
            return this.getClass().getSimpleName().equals(config.getConnector())
                    || this.getClass().getName().equals(config.getConnector());
        } else {
            return canHandle(config, capabilities);
        }
    }

    protected HttpResponse sendRequest(XmlObject request, String uri) {
        return new SimpleHttpClient().executePost(uri, request);
    }

    protected abstract boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities);

    public abstract ServiceConstellation getConstellation(DataSourceConfiguration config, GetCapabilitiesResponse capabilities);

    public abstract List<MeasurementDataEntity> getObservations(MeasurementDatasetEntity seriesEntity, DbQuery query);

}
