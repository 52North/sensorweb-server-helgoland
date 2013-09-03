/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.io.geojson;

import java.util.HashMap;
import java.util.Map;

public class GeojsonFeature extends GeojsonObject {

    private static final long serialVersionUID = 863297394860249486L;

    private static final String GEOJSON_TYPE_FEATURE = "Feature";
    
    protected Map<String, Object> properties = null;
    
    private GeojsonPoint geometry; // XXX should be GeojsonGeometry, but generics are different here 
    
    public String getType() {
        return GEOJSON_TYPE_FEATURE;
    }
    
    public GeojsonPoint getGeometry() {
        return geometry;
    }

    public void setGeometry(GeojsonPoint geometry) {
        this.geometry = geometry;
    }
    
    public void addProperty(String property, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(property, value);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
}
