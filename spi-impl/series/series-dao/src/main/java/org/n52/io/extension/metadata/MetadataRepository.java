/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.io.extension.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.series.db.da.SessionAwareRepository;

class MetadataRepository extends SessionAwareRepository {

    List<String> getFieldNames(String id) {
        Session session = getSession();
        try {
            DatabaseMetadataDao dao = new DatabaseMetadataDao(session);
            return dao.getMetadataNames(parseId(id));
        } finally {
            returnSession(session);
        }
    }

    Map<String, Object> getExtras(ParameterOutput output, IoParameters parameters) {
        Session session = getSession();
        try {
            DatabaseMetadataDao dao = new DatabaseMetadataDao(session);
            final Set<String> fields = parameters.getFields();
            return fields == null
                    ? convertToOutputs(dao.getAllFor(parseId(output.getId())))
                    : convertToOutputs(dao.getSelected(parseId(output.getId()), fields));
        } finally {
            returnSession(session);
        }
    }

    private Map<String, Object> convertToOutputs(List<MetadataEntity<?>> allInstances) {
        if (allInstances == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> outputs = new HashMap<>();
        for (MetadataEntity<?> entity : allInstances) {
            outputs.put(entity.getName(), entity.toOutput());
        }
        return outputs;
    }

}