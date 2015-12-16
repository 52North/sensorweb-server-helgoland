/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
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
