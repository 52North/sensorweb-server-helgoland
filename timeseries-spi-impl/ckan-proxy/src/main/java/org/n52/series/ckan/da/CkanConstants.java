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
package org.n52.series.ckan.da;

import java.nio.charset.Charset;

public interface CkanConstants {
    
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public interface SchemaDescriptor {
        public static final String SCHEMA_DESCRIPTOR = "schema_descriptor";
        public static final String VERSION = "schema_descriptor_version";
        public static final String ID = "schema_descriptor_id";
        public static final String DESCRIPTION = "schema_descriptor_description";
        public static final String RESOURCE_TYPE = "resource_type";
    }
    
    public interface ResourceType {
        public static final String CSV_OBSERVATIONS_COLLECTION = "csv-observations-collection";
        public static final String PLATFORMS = "platforms";
        public static final String OBSERVATIONS = "observations";
    }
    
    public interface MemberProperty {
        
        public static final String RESOURCE_NAME = "resource_name";
        @Deprecated
        public static final String RESOURCEID = "resourceId";
        public static final String RESOURCE_TYPE = "resource_type";
        @Deprecated
        public static final String RESOURCETYPE = "resourcetype";
        public static final String HEADER_ROWS = "headerrows";
        public static final String FIELD_ID = "field_id";
        @Deprecated
        public static final String FIELDID = "fieldId";
        public static final String SHORT_NAME = "short_name";
        @Deprecated
        public static final String FIELD_SHORTNAME = "shortName";
        public static final String MEMBER_FIELD_LONG_NAME = "long_name";
        @Deprecated
        public static final String FIELD_LONGNAME = "longName";
        public static final String FIELD_DESCRIPTION = "description";
        public static final String FIELD_TYPE = "field_type";
        @Deprecated
        public static final String FIELDTYPE = "fieldType";
    }
    
    public interface KnownFieldId {
        public static final String STATION_ID = "Stations_id";
        public static final String CRS = "crs";
        public static final String LATITUDE = "geoBreite";
        public static final String LONGITUDE = "geoLaenge";
        public static final String ALTITUDE = "Stationshoehe";
        public static final String STATION_NAME = "Stationsname";
        public static final String FIRST_DATE = "von_datum";
        public static final String LAST_DATE = "bis_datum";
        
        public static final String RESULT_TIME = "MESS_DATUM";
    }
    
    public interface KnownFieldProperty {
        public static final String PHENOMENON = "phenomenon";
        public static final String UOM = "uom";
    }
    
}
