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
package org.n52.io.extension;

import java.util.Collections;
import java.util.Map;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.da.ProcedureRepository;
import org.n52.io.response.extension.MetadataExtension;
import org.n52.series.db.dao.DbQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DescribeSensorExtension extends MetadataExtension<ParameterOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescribeSensorExtension.class);

    private static final String EXTENSION_NAME = "describeSensorURL";

    @Autowired
    private ProcedureRepository procedureRepository;

    public DescribeSensorExtension(){}

    private String createURL(ParameterOutput output, IoParameters parameters) {
        String procedureid = "";
        try {
            procedureid =  ((DatasetOutput)output).getSeriesParameters().getProcedure().getId();
            ProcedureOutput procedure = procedureRepository.getInstance(procedureid , DbQuery.createFrom(parameters));
            return createBaseUrl(output.getHrefBase())
                    + "service?service=SOS&version=2.0.0&request=DescribeSensor&procedure="
                    + procedure.getDomainId()
                    + "&procedureDescriptionFormat=http%3A%2F%2Fwww.opengis.net%2FsensorML%2F1.0.1";

        } catch (DataAccessException ex) {
            LOGGER.error("Could not find domainID for procedure with id '{}'", procedureid);
            return "";
        }
    }

    private String createBaseUrl(String base){

        //TODO(specki): Get correct external URL from ParameterController -> but avoid circular dependency with rest->io->rest
        String[] BaseUrl_split = base.split("/");
        String url = BaseUrl_split[0] + "/";
        for (int i = 1; i < BaseUrl_split.length - 2; i++){
            url += BaseUrl_split[i] + "/";
        }
        return url;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Map<String, Object> getExtras(ParameterOutput output, IoParameters parameters) {
        return hasExtrasToReturn(output, parameters)
                ? wrapSingleIntoMap(this.createURL(output, parameters))
                : Collections.<String, Object>emptyMap();
    }

    @Override
    public void addExtraMetadataFieldNames(ParameterOutput output) {
        output.addExtra(EXTENSION_NAME);
    }
}
