package org.n52.proxy.connector;

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlObject;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.encode.EncoderRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSosConnector {

    @Autowired
    protected DecoderRepository decoderRepository;

    @Autowired
    protected EncoderRepository encoderRepository;

    public abstract String getHandlerName();

    public abstract ServiceConstellation getConstellation(DataSourceConfiguration config);

    public boolean matches(DataSourceConfiguration config) {
        if (config.getConnector() != null
                && (this.getClass().getSimpleName().equals(config.getConnector())
                || this.getClass().getName().equals(config.getConnector()))) {
            return true;
        } else {
            return canHandle(config);
        }
    }

    protected abstract boolean canHandle(DataSourceConfiguration config);

    protected HttpResponse sendRequest(XmlObject request, String uri) {
        return new SimpleHttpClient().executePost(uri, request);
    }

}
