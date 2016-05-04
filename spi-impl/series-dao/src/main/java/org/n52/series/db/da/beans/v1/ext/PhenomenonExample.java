/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.da.beans.v1.ext;

import java.util.UUID;
import org.n52.io.response.v1.PhenomenonOutput;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public enum PhenomenonExample {

    PRECIPITATION_HEIGHT,
    AIR_TEMPERATURE,
    SALINITY,
    RADIANCE;

    private String id = UUID.randomUUID().toString();

    public PhenomenonOutput getCondensed() {
        switch (this) {
            case PRECIPITATION_HEIGHT:
                return createCondensed("Precipitation Height");
            case AIR_TEMPERATURE:
                return createCondensed("Air Temperature");
            case SALINITY:
                return createCondensed("Salinity");
            case RADIANCE:
                return createCondensed("urn:ogc:def:property:OGC:1.0:radiance");
            default:
        }
        return null;
    }

    public PhenomenonOutput getOutput() {
        PhenomenonOutput output = getCondensed();
        output.setDomainId(output.getLabel().replace(" ", "_"));
        return output;
    }

    public PhenomenonOutput createCondensed(String label) {
        PhenomenonOutput condensed = new PhenomenonOutput();
        condensed.setId(id);
        condensed.setLabel(label);
        condensed.setHref(ExampleConstants.BASE_URL + "/phenomena/ext/" + condensed.getId());
        return condensed;
    }
}
