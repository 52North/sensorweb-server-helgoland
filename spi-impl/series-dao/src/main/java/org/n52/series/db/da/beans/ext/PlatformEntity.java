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
package org.n52.series.db.da.beans.ext;

import org.n52.io.response.v1.ext.PlatformType;
import org.n52.series.db.da.beans.DescribableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @since 2.0.0
 */
public class PlatformEntity extends DescribableEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformEntity.class);

    private boolean mobile = false;

    private boolean insitu = true;

    public PlatformType getPlatformType() {
        return PlatformType.toInstance(mobile, insitu);
    }

//    private PlatformType platformType;
//    public void setPlatformType(String platformType) {
//        this.platformType = PlatformType.isKnownType(platformType)
//                ? PlatformType.toInstance(platformType)
//                : null;
//    }
/*
    public PlatformType getPlatformType() {
        PlatformType type = platformType;
        if (series == null) {
            return PlatformType.STATIONARY_INSITU;
        }
        for (AbstractSeriesEntity entity : series) {
            final FeatureEntity feature = entity.getFeature();
            final String concept = feature.getFeatureConcept();
            if (!PlatformType.isKnownType(concept)) {
                LOGGER.warn("unknown feature concept for feature '{}': {}", feature.getPkid(), concept);
                continue;
            }
            // TODO log warning when not consistent?!
//            PlatformType tmp = PlatformType.toInstance(concept);
//            if (type != null && tmp != type) {
//                LOGGER.warn("Different platform type referenced: {} vs. {}", tmp, type);
//            } else {
//                type = tmp;
//            }
            type = PlatformType.toInstance(concept);
            break;
        }
        return type == null
                ? PlatformType.STATIONARY_INSITU
                : type;
    }
     */
    public boolean isMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public boolean isInsitu() {
        return insitu;
    }

    public void setInsitu(boolean insitu) {
        this.insitu = insitu;
    }
}
