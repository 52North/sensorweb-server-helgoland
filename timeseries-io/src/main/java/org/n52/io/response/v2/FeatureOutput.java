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
package org.n52.io.response.v2;

import com.vividsolutions.jts.geom.Geometry;
import org.n52.io.response.CollatorComparable;
import java.text.Collator;
import org.n52.io.geojson.GeoJSONException;
import org.n52.io.geojson.GeoJSONFeature;


public class FeatureOutput extends GeoJSONFeature implements CollatorComparable<FeatureOutput> {

    private static final long serialVersionUID = -2868469756939569521L;
    
    public FeatureOutput() throws GeoJSONException {
        super(GeoJSONType.Feature.name());
    }

    public FeatureOutput(String type) throws GeoJSONException {
        super(type);
    }
    
    public FeatureOutput(String type, Geometry geometry) throws GeoJSONException {
        super(type, geometry);
    }

    @Override
    public int compare(Collator collator, FeatureOutput o) {
        if (collator == null) {
            collator = Collator.getInstance();
        }
        String thisLabel = (String) getProperty(LABEL);
        String otherLabel = (String) o.getProperty(LABEL);
        return collator.compare(thisLabel.toLowerCase(), otherLabel.toLowerCase());
    }

}
