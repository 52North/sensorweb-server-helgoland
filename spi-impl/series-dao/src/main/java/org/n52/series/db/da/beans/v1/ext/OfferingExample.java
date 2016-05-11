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
import org.n52.io.response.v1.CategoryOutput;
import org.n52.io.response.v1.OfferingOutput;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public enum OfferingExample {

    PRECIPITATION,
    RV_SONNE_MISSION_1,
    RV_SONNE_MISSION_2,
    IMAGERY;

    private final String id = UUID.randomUUID().toString();

    public OfferingOutput getCondensed() {
        switch (this) {
            case PRECIPITATION:
                return createCondensed("Precipitation");
            case IMAGERY:
                return createCondensed("Imagery");
            case RV_SONNE_MISSION_1:
                return createCondensed("RV Sonne, Mission 1");
            case RV_SONNE_MISSION_2:
                return createCondensed("RV Sonne, Mission 2");
            default:
        }
        return null;
    }

    public OfferingOutput getOutput() {
        OfferingOutput output = getCondensed();
        output.setDomainId(name());
        return output;
    }

    public OfferingOutput createCondensed(String label) {
        OfferingOutput condensed = new OfferingOutput();
        condensed.setId(id);
        condensed.setLabel(label);
        condensed.setHrefBase(ExampleConstants.BASE_URL + "/categories/ext/" + condensed.getId());
        return condensed;
    }
}
