package org.n52.proxy.config;

public class DataSourceJobConfiguration {

    private String cronExpression;
    private boolean enabled;
    private boolean triggerAtStartup;

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isTriggerAtStartup() {
        return triggerAtStartup;
    }

    public void setTriggerAtStartup(boolean triggerAtStartup) {
        this.triggerAtStartup = triggerAtStartup;
    }

}
