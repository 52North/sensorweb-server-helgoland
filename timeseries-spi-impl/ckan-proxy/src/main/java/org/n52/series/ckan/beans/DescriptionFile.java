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
package org.n52.series.ckan.beans;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.io.File;
import java.sql.Timestamp;
import org.joda.time.DateTime;

public class DescriptionFile {
    
    private final CkanDataset dataset;
    
    private final File file;
    
    private final SchemaDescriptor schemaDescription;
    
    public DescriptionFile(CkanDataset dataset, File file, SchemaDescriptor node) {
        this.dataset = dataset;
        this.file = file;
        this.schemaDescription = node;
    }
    
    public CkanDataset getDataset() {
        return dataset;
    }

    public File getFile() {
        return file;
    }

    public SchemaDescriptor getSchemaDescription() {
        return schemaDescription;
    }
    
    public DateTime getLastModified() {
        return new DateTime(dataset.getMetadataModified());
    }
    
    public boolean isNewerThan(CkanDataset dataset) {
        if (dataset == null) {
            return false;
        }
        Timestamp probablyNewer = dataset.getMetadataModified();
        Timestamp current = this.dataset.getMetadataModified();
        return this.dataset.getId().equals(dataset.getId())
                ? current.after(probablyNewer)
                : false;
        
    }
    
}
