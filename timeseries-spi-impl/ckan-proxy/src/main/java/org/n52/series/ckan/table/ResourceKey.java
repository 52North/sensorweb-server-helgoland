package org.n52.series.ckan.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.n52.series.ckan.beans.ResourceMember;

public class ResourceKey {
    
    private final String keyId;
    
    private final List<ResourceMember> members;

    public ResourceKey(String keyId, ResourceMember member) {
        this.members = new ArrayList<>();
        this.members.add(member);
        this.keyId = keyId;
    }

    public String getKeyId() {
        return keyId;
    }

    public List<ResourceMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(ResourceMember member) {
        members.add(member);
    }
    
    public void addMembers(Collection<ResourceMember> members) {
        members.addAll(members);
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceKey other = (ResourceKey) obj;
        if (!Objects.equals(this.keyId, other.keyId)) {
            return false;
        }
        if (!Objects.equals(this.members, other.members)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ResourceKey{" + "keyId=" + keyId + ", members=" + members + '}';
    }

}
