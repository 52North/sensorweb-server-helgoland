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

public interface CkanConstants {

    public static final String SCHEMA_DESCRIPTOR = "schema_descriptor";
    
    public static final String SCHEMA_DESCRIPTOR_VERSION = "schema_descriptor_version";
    
    public static final String SCHEMA_DESCRIPTOR_ID = "schema_descriptor_id";
    
    public static final String SCHEMA_DESCRIPTOR_DESCRIPTION = "schema_descriptor_description";
    
    public static final String RESOURCE_TYPE_CSV_OBSERVATIONS_COLLECTION = "csv-observations-collection";
    
    public static final String RESOURCE_TYPE_PLATFORMS = "platforms";
    
    public static final String RESOURCE_TYPE_OBSERVATIONS = "observations";
    
    public static String MEMBER_RESOURCE_NAME = "resource_name";
    
    @Deprecated
    public static String MEMBER_RESOURCEID = "resourceId";
    
    public static String MEMBER_RESOURCE_TYPE = "resource_type";
    
    @Deprecated
    public static String MEMBER_RESOURCETYPE = "resourcetype";
    
    public static String MEMBER_HEADER_ROWS = "headerrows";
    
    public static String MEMBER_FIELD_ID = "field_id";
    
    @Deprecated
    public static String MEMBER_FIELDID = "fieldId";
    
    public static String MEMBER_FIELD_SHORT_NAME = "short_name";
    
    @Deprecated
    public static String MEMBER_FIELD_SHORTNAME = "shortName";
    
    public static String MEMBER_FIELD_LONG_NAME = "long_name";
    
    @Deprecated
    public static String MEMBER_FIELD_LONGNAME = "longName";
    
    public static String MEMBER_FIELD_DESCRIPTION = "description";
    
    public static String MEMBER_FIELD_TYPE = "field_type";
    
    @Deprecated
    public static String MEMBER_FIELDTYPE = "fieldType";
}
