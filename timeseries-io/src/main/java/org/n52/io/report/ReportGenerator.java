/**
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.io.report;

import static org.n52.io.I18N.getDefaultLocalizer;
import static org.n52.io.I18N.getMessageLocalizer;

import java.util.Locale;

import org.n52.io.I18N;
import org.n52.io.IoHandler;
import org.n52.io.img.RenderingContext;
import org.n52.io.v1.data.TimeseriesMetadataOutput;

public abstract class ReportGenerator implements IoHandler {

    protected I18N i18n = getDefaultLocalizer();

    private RenderingContext context;

    /**
     * @param locale
     *        the ISO639 locale to be used.
     * 
     * @see Locale
     */
    public ReportGenerator(RenderingContext context, String language) {
        if (language != null) {
            i18n = getMessageLocalizer(language);
        }
        this.context = context;
    }
    

    public RenderingContext getContext() {
        return context;
    }

    protected TimeseriesMetadataOutput[] getTimeseriesMetadatas() {
        return getContext().getTimeseriesMetadatas();
    }

}
