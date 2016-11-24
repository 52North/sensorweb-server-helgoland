package org.n52.proxy.db.beans;

import java.util.HashSet;
import java.util.Set;

import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.ServiceEntity;


public class RelatedFeatureEntity {
    
    public static final String FEATURE = "feature";
    public final static String SERVICE = "service";
    
    private long relatedFeatureId;
    private FeatureEntity feature;
    private ServiceEntity service;
    private Set<RelatedFeatureRoleEntity> relatedFeatureRoles = new HashSet<RelatedFeatureRoleEntity>(0);
    private Set<OfferingEntity> offerings = new HashSet<OfferingEntity>(0);

    public RelatedFeatureEntity() {
    }

    public long getRelatedFeatureId() {
        return this.relatedFeatureId;
    }

    public void setRelatedFeatureId(long relatedFeatureId) {
        this.relatedFeatureId = relatedFeatureId;
    }

    public FeatureEntity getFeature() {
        return this.feature;
    }

    public void setFeature(FeatureEntity feature) {
        this.feature = feature;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }
    
    public Set<RelatedFeatureRoleEntity> getRelatedFeatureRoles() {
        return this.relatedFeatureRoles;
    }

    public void setRelatedFeatureRoles(Set<RelatedFeatureRoleEntity> relatedFeatureRoles) {
        this.relatedFeatureRoles = relatedFeatureRoles;
    }

    public Set<OfferingEntity> getOfferings() {
        return this.offerings;
    }

    @SuppressWarnings("unchecked")
    public void setOfferings(final Object offerings) {
        if (offerings instanceof Set<?>) {
            this.offerings = (Set<OfferingEntity>) offerings;
        } else {
            getOfferings().add((OfferingEntity)offerings);
        }
    }
}
