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


public class BarStyle extends Style {

	private static final String BAR_INTERVAL = "interval";
	
	private static final String BAR_WIDTH = "width";
    
	private static final String DEFAULT_BAR_INTERVAL = "byDay";
	
	private static final double DEFAULT_BAR_WIDTH = 0.8;
    
    public String getBarInterval() {
    	if (hasProperty(BAR_INTERVAL)) {
    		return getPropertyAsString(BAR_INTERVAL);
    	}
    	return DEFAULT_BAR_INTERVAL;
    }
    
    public double getBarMargin() {
    	// API parameter width is more intuitive than margin parameter internally used by jFreeChart 
		if (hasProperty(BAR_WIDTH)) {
			return 1.0 - getPropertyAsDouble(BAR_WIDTH);
		}
		return DEFAULT_BAR_WIDTH;
	}
    
    public static BarStyle createFrom(StyleProperties options) {
        BarStyle barStyleOptions = new BarStyle();
        barStyleOptions.setProperties(options.getProperties());
        return barStyleOptions;
    }
    
}
