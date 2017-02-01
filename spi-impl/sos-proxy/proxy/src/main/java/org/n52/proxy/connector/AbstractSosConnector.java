package org.n52.proxy.connector;

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlObject;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.encode.EncoderRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSosConnector {

    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    @Autowired
    protected DecoderRepository decoderRepository;

    @Autowired
    protected EncoderRepository encoderRepository;

    public abstract String getHandlerName();

    public abstract ServiceConstellation getConstellation(DataSourceConfiguration config);

    public abstract boolean canHandle(DataSourceConfiguration config);

    protected HttpResponse sendRequest(XmlObject request, String uri) {
        return httpClient.executePost(uri, request);
    }

}
