/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.io.style;

import org.n52.io.request.StyleProperties;

public class LineStyle extends Style {

    private static final String WIDTH = "width";

    private static final String LINE_TYPE = "lineType";

    private static final String DEFAULT_LINE_TYPE = "solid";

    private static final int DEFAULT_DASH_GAP_WIDTH = 2;

    private static final int DEFAULT_LINE_WIDTH = 2;

    private static final int DEFAULT_DOT_WIDTH = 2;

    public int getDotWidth() {
        if (hasProperty(WIDTH)) {
            return getPropertyAsInt(WIDTH);
        }
        return DEFAULT_DOT_WIDTH;
    }

    public int getDashGapWidth() {
        if (hasProperty(WIDTH)) {
            return getPropertyAsInt(WIDTH);
        }
        return DEFAULT_DASH_GAP_WIDTH;
    }

    public int getLineWidth() {
        if (hasProperty(WIDTH)) {
            return getPropertyAsInt(WIDTH);
        }
        return DEFAULT_LINE_WIDTH;
    }

    public String getLineType() {
        if (hasProperty(LINE_TYPE)) {
            return getPropertyAsString(LINE_TYPE);
        }
        return DEFAULT_LINE_TYPE;
    }

    public static LineStyle createLineStyle(StyleProperties options) {
        if (options == null) {
            return createDefaultLineStyle();
        }
        LineStyle lineStyleOptions = new LineStyle();
        lineStyleOptions.setProperties(options.getProperties());
        return lineStyleOptions;
    }

    public static LineStyle createDefaultLineStyle() {
        return new LineStyle();
    }

}
