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
package org.n52.io.img;

import java.awt.Color;

import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.n52.io.style.BarStyle;

class BarRenderer implements Renderer {
    
    static final String BAR_CHART_TYPE = "bar";
    
    private XYBarRenderer renderer;
    
    private BarStyle style;

    private BarRenderer(BarStyle style) {
    	this.style = style;
    	this.renderer = new XYBarRenderer(style.getBarMargin());
    }

    @Override
    public String getRendererType() {
        return BAR_CHART_TYPE;
    }

    @Override
    public XYItemRenderer getXYRenderer() {
        return renderer;
        
    }

    @Override
    public void setColorForSeriesAt(int index) {
    	this.renderer.setSeriesPaint(index, Color.decode(style.getColor()));
    }

	public static BarRenderer createBarRenderer(BarStyle barStyle) {
		return new BarRenderer(barStyle);
	}
	
}

