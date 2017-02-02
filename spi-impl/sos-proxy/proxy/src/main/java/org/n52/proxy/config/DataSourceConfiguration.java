package org.n52.proxy.config;

public class DataSourceConfiguration {

    private String itemName;
    private String url;
    private String version;
    private String connector;
    private DataSourceJobConfiguration job;

    public DataSourceJobConfiguration getJob() {
        return job;
    }

    public void setJob(DataSourceJobConfiguration job) {
        this.job = job;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    @Override
    public String toString() {
        return "DataSourceConfiguration{" + "itemName=" + itemName + ", url=" + url + ", version=" + version + ", connector=" + connector + "}";
    }

}
