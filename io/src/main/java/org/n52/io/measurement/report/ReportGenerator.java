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
package org.n52.io.measurement.report;

import static org.n52.io.I18N.getDefaultLocalizer;
import static org.n52.io.I18N.getMessageLocalizer;

import java.util.List;

import org.n52.io.I18N;
import org.n52.io.IoHandler;
import org.n52.io.measurement.img.MeasurementRenderingContext;
import org.n52.io.response.series.MeasurementData;
import org.n52.io.response.series.MeasurementSeriesOutput;

public abstract class ReportGenerator implements IoHandler<MeasurementData> {

    protected I18N i18n = getDefaultLocalizer();

    private MeasurementRenderingContext context;

    /**
     * @param context the rendering context.
     * @param language the ISO639 locale to be used.
     */
    public ReportGenerator(MeasurementRenderingContext context, String language) {
        if (language != null) {
            i18n = getMessageLocalizer(language);
        }
        this.context = context;
    }

    public MeasurementRenderingContext getContext() {
        return context;
    }

    protected List<MeasurementSeriesOutput> getSeriesMetadatas() {
        return getContext().getSeriesMetadatas();
    }

}
