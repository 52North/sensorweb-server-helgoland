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

package org.n52.io.report;

import static org.n52.io.I18N.getDefaultLocalizer;
import static org.n52.io.I18N.getMessageLocalizer;

import java.util.Locale;

import org.n52.io.I18N;
import org.n52.io.IOHandler;
import org.n52.io.img.RenderingContext;
import org.n52.io.v1.data.TimeseriesMetadataOutput;

public abstract class ReportGenerator implements IOHandler {

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
