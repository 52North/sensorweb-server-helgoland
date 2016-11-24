package org.n52.proxy.db.beans;

public class RelatedFeatureRoleEntity {
    
    private long relatedFeatureRoleId;
    private String relatedFeatureRole;

    public RelatedFeatureRoleEntity() {
    }

    public long getRelatedFeatureRoleId() {
        return this.relatedFeatureRoleId;
    }

    public void setRelatedFeatureRoleId(long relatedFeatureRoleId) {
        this.relatedFeatureRoleId = relatedFeatureRoleId;
    }

    public String getRelatedFeatureRole() {
        return this.relatedFeatureRole;
    }

    public void setRelatedFeatureRole(String relatedFeatureRole) {
        this.relatedFeatureRole = relatedFeatureRole;
    }
}
