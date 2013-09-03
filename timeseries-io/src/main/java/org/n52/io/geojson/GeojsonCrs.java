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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GeojsonCrs extends GeojsonObject {
    
    private static final long serialVersionUID = 5964748458745655509L;

    private static final String TYPE_NAME = "name";
    
    private static final String TYPE_LINK = "link";
    
    private Map<String, String> properties;

    private String type = TYPE_NAME;
    
    GeojsonCrs() {
        this.properties = new HashMap<String, String>();
    }
    
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
    
    @JsonIgnore
    public String getName() {
        return properties.get("name");
    }
    
    @JsonIgnore
    public String getHRef() {
        return properties.get("href");
    }
    
    @JsonIgnore
    public String getLinkType() {
        return properties.get("type");
    }
    
    public static GeojsonCrs createNamedCRS(String name) {
        if (name == null) {
            throw new NullPointerException("Argument 'name' must not be null.");
        }
        GeojsonCrs namedCrs = new GeojsonCrs();
        namedCrs.addProperty("name", name);
        namedCrs.setType(TYPE_NAME);
        return namedCrs;
    }
    
    public static GeojsonCrs createLinkedCRS(String url, String type) {
        GeojsonCrs linkedCrs = new GeojsonCrs();
        linkedCrs.addProperty("type", type);
        linkedCrs.addProperty("href", url);
        linkedCrs.setType(TYPE_LINK);
        return linkedCrs;
    }
    
    
    
}
