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
import org.n52.io.crs.CRSUtils;
import org.n52.io.response.v2.test.GeometryCategory;
import org.n52.io.response.v2.test.GeometryInfo;
import org.n52.io.response.v2.test.PlatformItemOutput;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public enum GeometryExample {

    SITE_WEBCAM,
    SITE_BEVER_TALSPERRE,
    TRACK_1_RV_SONNE_MISSION_1,
    TRACK_2_RV_SONNE_MISSION_1,
    TRACK_1_RV_SONNE_MISSION_2,
    TRACK_2_RV_SONNE_MISSION_2,
    STATIC_OBSERVATION_WEBCAM;

    private static final CRSUtils crsUtils = CRSUtils.createEpsgForcedXYAxisOrder();

    public GeometryInfo getOutput(PlatformItemOutput platform) {
        GeometryInfo output = getCondensed();
        output.setPlatform(platform);
        switch (this) {
            case SITE_WEBCAM:
                output.setGeometry(crsUtils.parseWkt("POINT[5.3, 60.1]"));
                break;
            case SITE_BEVER_TALSPERRE:
                output.setGeometry(crsUtils.parseWkt("POINT[7.3, 52.7]"));
                break;
            case TRACK_1_RV_SONNE_MISSION_1:
                output.setGeometry(crsUtils.parseWkt("LINESTRING[[3,52],[1.9,50.7,0],[5.3,58.8,0],[6.8,51.9,0],[5.7,54,0]]"));
                break;
            case TRACK_2_RV_SONNE_MISSION_1:
                output.setGeometry(crsUtils.parseWkt("LINESTRING[[2.9,51.9,0],[1.8,50.6,0],[5.2,58.7,0],[6.7,51.8,0],[5.6,53.9,0]]"));
                break;
            case TRACK_1_RV_SONNE_MISSION_2:
                output.setGeometry(crsUtils.parseWkt("LINESTRING[[1.9,55.9,0],[1.3,52.6,0],[5.8,55.7,0],[8.7,53.8,0],[5.7,50.9,0]]"));
                break;
            case TRACK_2_RV_SONNE_MISSION_2:
                output.setGeometry(crsUtils.parseWkt("LINESTRING[[1.8,55.8,0],[1.2,52.5,0],[5.7,55.6,0],[8.6,53.7,0],[5.6,50.8,0]]"));
                break;
            case STATIC_OBSERVATION_WEBCAM:
                output.setGeometry(crsUtils.parseWkt("Polygon[[5.9,53.9,0],[5.8,53.6,0],[5.7,53.7,0],[5.7,53.8,0],[5.6,53.9,0],[5.9,53.9,0]]"));
                break;
            default:
        }
        return output;
    }

    public GeometryInfo getCondensed() {
        switch (this) {
            case SITE_BEVER_TALSPERRE:
                return createCondensed(GeometryCategory.PLATFORM_SITE);
            case TRACK_1_RV_SONNE_MISSION_1:
            case TRACK_2_RV_SONNE_MISSION_1:
            case TRACK_1_RV_SONNE_MISSION_2:
            case TRACK_2_RV_SONNE_MISSION_2:
                return createCondensed(GeometryCategory.PLATFORM_TRACK);
            case STATIC_OBSERVATION_WEBCAM:
                return createCondensed(GeometryCategory.STATIC_OBSERVERATION);
            default:
        }
        return null;
    }

    public static GeometryInfo createCondensed(GeometryCategory category) {
        GeometryInfo condensed = new GeometryInfo(category);
        condensed.setId(UUID.randomUUID().toString());
        condensed.setHref(ExampleConstants.BASE_URL + "/" + category + "/" + condensed.getId());
        return condensed;
    }

}
