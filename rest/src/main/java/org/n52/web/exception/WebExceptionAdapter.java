/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.web.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class WebExceptionAdapter extends RuntimeException implements WebException {

    private static final long serialVersionUID = 8960179333452332350L;

    private List<String> details;

    public WebExceptionAdapter(String message, Throwable cause) {
        super(message, cause);
    }

    public WebExceptionAdapter(String message) {
        super(message);
    }

    @Override
    public WebExceptionAdapter addHint(String... hints) {
        if (hints != null) {
            Arrays.asList(hints)
                  .stream()
                  .forEach(d -> addHint(d));
        }
        return this;
    }

    @Override
    public WebExceptionAdapter addHint(String hint) {
        if (hint == null) {
            return this;
        }
        if (getHints() == null) {
            this.details = new ArrayList<>();
        }
        this.details.add(hint);
        return this;
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
