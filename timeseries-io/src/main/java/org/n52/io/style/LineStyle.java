/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.io.style;

import org.n52.io.v1.data.StyleProperties;

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
