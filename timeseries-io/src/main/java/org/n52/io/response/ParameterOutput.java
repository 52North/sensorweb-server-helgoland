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
package org.n52.io.response;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class ParameterOutput implements CollatorComparable<ParameterOutput> {

    /**
     * Takes the labels to compare.
     * 
     * @param <T> the actual type.
     * @return a label comparing {@link Comparator}
     */
    public static <T extends ParameterOutput> Comparator<T> defaultComparator() {
        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        };
    }

    private String id;

    private String domainId;

    private String label;
    
    private String license;

    private List<String> extras;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the domain id of the parameter, e.g. a natural id (not arbitrarily generated) or the original id actually
     * being used by proxied data sources.
     *
     * @return the domain id.
     */
    public String getDomainId() {
        return domainId;
    }

    /**
     * Sets the domain id of the parameter, e.g. a natural (not arbitrarily generated) id or the original id actually
     * being used by proxied data sources.
     *
     * @param domainId the domain id of the parameter.
     */
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }
    
    /**
     * Check if the domainId is set and not empty
     * 
     * @return <code>true</code> if domainId is set and not empty
     */
    public boolean isSetDomainId() {
        return getDomainId() != null && !getDomainId().isEmpty();
    }

    /**
     * @return the label or the id if label is not set.
     */
    public String getLabel() {
        // ensure that label is never null
        return label == null ? id : label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * @return a list of extra identifiers available via /&lt;resource&gt;/extras
     *
     * TODO make queryable, for example: ../extras?get=myFancyAddon1,myFancyAddon2
     */
    public String[] getExtras() {
        if (extras != null) {
            return extras.toArray(new String[0]);
        }
        return null;
    }

    public void addExtra(String extra) {
        if (extras == null) {
            extras = new ArrayList<>();
        }
        extras.add(extra);
    }

    @Override
    public int compare(Collator collator, ParameterOutput o) {
        if (collator == null) {
            collator = Collator.getInstance();
        }
        return collator.compare(getLabel().toLowerCase(), o.getLabel().toLowerCase());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((domainId == null) ? 0 : domainId.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParameterOutput other = (ParameterOutput) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.domainId == null) ? (other.domainId != null) : !this.domainId.equals(other.domainId)) {
            return false;
        }
        if ((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
            return false;
        }
        return true;
    }

}
