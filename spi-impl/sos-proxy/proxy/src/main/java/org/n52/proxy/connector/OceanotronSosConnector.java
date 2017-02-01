package org.n52.proxy.connector;

import org.n52.proxy.config.DataSourceConfiguration;

public class OceanotronSosConnector extends AbstractSosConnector {

    @Override
    public String getHandlerName() {
        return "oceanotron handler with decoder: " + this.decoderRepository.toString();
    }

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canHandle(DataSourceConfiguration config) {
        return false;
    }

}
