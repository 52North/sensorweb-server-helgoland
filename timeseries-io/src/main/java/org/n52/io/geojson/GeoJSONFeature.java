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
package org.n52.io.geojson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Geometry;
import java.util.Comparator;

public class GeoJSONFeature extends GeoJSONObject {
    
    public static <T extends GeoJSONFeature> Comparator<T> defaultComparator() {
        return new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    if (o1 == null || o2 == null) {
                        throw new NullPointerException("comparing null value(s)!");
                    }
                    String label1 = getLabelOf(o1);
                    String label2 = getLabelOf(o2);
                    return label1.compareTo(label2);
                }

                private String getLabelOf(GeoJSONFeature feature) {
                    return feature.hasProperty(LABEL)
                            ? (String) feature.getProperty(LABEL)
                            : "";
                }
            };
    }
    
    private String id;
    
    @JsonIgnore
    private Geometry geometry;
    
    public GeoJSONFeature(String type, Geometry geometry) throws GeoJSONException {
        super(type);
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    
}
