package org.n52.proxy.db.beans;

import org.n52.series.db.beans.ServiceEntity;

public class ProxyServiceEntity extends ServiceEntity {

    private String connector;

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

}
