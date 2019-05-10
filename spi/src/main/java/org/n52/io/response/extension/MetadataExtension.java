/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.io.response.extension;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;

public abstract class MetadataExtension<T extends ParameterOutput> {

    public abstract String getExtensionName();

    /**
     * <p>
     * Gets the extra metadata as simple <code>key=value</code> mapping. The
     * value is of kind object, so the implementation is free to put any data
     * structure which might make sense to serialize.
     * </p>
     * <p>
     * Implementation is responsible to respect selected fields from the query
     * which can be obtained by {@link IoParameters#getFields() }.</p>
     *
     * @param output the actual parameter output to get extra metadata for.
     * @param parameters I/O parameters to fine grain extra metadata assembly.
     * @return the extra metadata.
     */
    public abstract Map<String, Object> getExtras(T output, IoParameters parameters);

    public Collection<String> getExtraMetadataFieldNames(T output) {
        return Collections.singletonList(getExtensionName());
    }

    protected boolean hasExtrasToReturn(ParameterOutput output, IoParameters parameters) {
        return parameters.getFields().isEmpty()
                || containsIgnoreCase(parameters.getFields());
    }

    private boolean containsIgnoreCase(Set<String> fields) {
        for (String field : fields) {
            final String fieldName = field.toLowerCase();
            final String extensionName = getExtensionName().toLowerCase();
            if (fieldName.equals(extensionName)) {
                return true;
            }
        }
        return false;
    }

    protected Map<String, Object> wrapSingleIntoMap(Object metadata) {
        Map<String, Object> extras = new HashMap<>();
        extras.put(getExtensionName(), metadata);
        return extras;
    }

}
