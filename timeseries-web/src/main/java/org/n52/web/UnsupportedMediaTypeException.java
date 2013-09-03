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
package org.n52.web;

import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = UNSUPPORTED_MEDIA_TYPE)
public class UnsupportedMediaTypeException extends RuntimeException implements WebException {

    private static final long serialVersionUID = -299285770822168789L;

    private List<String> details;

    @Deprecated
	public UnsupportedMediaTypeException() {
		super();
	}

	public UnsupportedMediaTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedMediaTypeException(String message) {
		super(message);
	}


	@Override
    public void addHint(String details) {
        if (details == null) {
            return;
        }
        if (getHints() == null) {
            this.details = new ArrayList<String>();
        }
        this.details.add(details);
    }

    @Override
    public String[] getHints() {
        return details == null ? null : details.toArray(new String[0]);
    }

    @Override
    public Throwable getThrowable() {
        return this;
    }
    
}
