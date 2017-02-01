package org.n52.proxy.connector;

import org.n52.proxy.config.DataSourcesConfig;

public class OceanotronSosConnector extends AbstractSosConnector {

    @Override
    public String getHandlerName() {
        return "oceanotron handler with decoder: " + this.decoderRepository.toString();
    }

    @Override
    public ServiceConstellation getConstellation(DataSourcesConfig.DataSourceConfig config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canHandle(DataSourcesConfig.DataSourceConfig config) {
        return false;
    }

}
