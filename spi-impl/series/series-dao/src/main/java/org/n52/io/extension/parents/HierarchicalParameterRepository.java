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
package org.n52.io.extension.parents;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.HierarchicalParameterOutput;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetType;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.da.PlatformRepository;
import org.n52.series.db.da.ProcedureRepository;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.dao.DatasetDao;
import org.n52.series.db.dao.DbQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

class HierarchicalParameterRepository extends SessionAwareRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(HierarchicalParameterRepository.class);

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private PlatformRepository platformRepository;

    Map<String, Set<HierarchicalParameterOutput>> getExtras(String platformId, IoParameters parameters) {
        Session session = getSession();
        try {
            DbQuery dbQuery = getDbQuery(parameters);
            Map<String, Set<HierarchicalParameterOutput>> extras = new HashMap<>();

            PlatformOutput platform = platformRepository.getInstance(platformId, dbQuery);
            DatasetDao<DatasetEntity<?>> dao = new DatasetDao<>(session);
            for (DatasetOutput dataset : platform.getDatasets()) {
                String datasetId = DatasetType.extractId(dataset.getId());
                DatasetEntity<?> instance = dao.getInstance(Long.parseLong(datasetId), dbQuery);

//                // ugly hack to ensure parents get initialized
//                String procedureId = Long.toString(instance.getProcedure().getPkid());
//                DbQuery query = getDbQuery(dbQuery.getParameters()
//                        .extendWith(Parameters.PROCEDURES, procedureId));
//                ProcedureOutput output = procedureRepository.getAllExpanded(query)
//                        .stream()
//                        .filter(e -> e.getId().equals(procedureId))
//                        .limit(1)
//                        .collect(Collectors.toList())
//                        .get(0);

                ProcedureEntity entity = instance.getProcedure();
                Set<? extends HierarchicalParameterOutput> parents = entity.hasParents()
                        ? new HashSet<>(entity.getParents().stream().map(e -> createCondensed(new ProcedureOutput(), e, dbQuery)).collect(Collectors.toSet()))
                        : Collections.singleton(createCondensed(new ProcedureOutput(), entity, dbQuery));
                if ( !extras.containsKey("procedures")) {
                    extras.put("procedures", new HashSet<>());
                }
                extras.get("procedures").addAll(parents);
            }

            return extras;
        } catch (NumberFormatException e) {
            LOGGER.debug("Could not convert id '{}' to long.", platformId, e);
        } catch (DataAccessException e) {
            LOGGER.error("Could not query hierarchical parameters for dataset with id '{}'", platformId, e);
        } finally {
            returnSession(session);
        }
        return Collections.emptyMap();
    }

}
