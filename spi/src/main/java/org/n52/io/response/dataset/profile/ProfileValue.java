/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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
package org.n52.io.response.dataset.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.TimeOutputConverter;
import org.n52.io.response.dataset.AbstractValue;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class ProfileValue<T> extends AbstractValue<List<ProfileDataItem<T>>> implements Serializable {

    private static final long serialVersionUID = -7292181682632614697L;

    private VerticalExtentOutput verticalExtent;

    public ProfileValue() {
        // for serialization
    }

    @Override
    public List<ProfileDataItem<T>> getValue() {
        List<ProfileDataItem<T>> value = super.getValue();
        if (Objects.nonNull(value)) {
            List<ProfileDataItem<T>> profileValue = new ArrayList<>(value);
            Collections.sort(profileValue);
            return profileValue;
        } else {
            return null;
        }
    }

    public VerticalExtentOutput getVerticalExtent() {
        return verticalExtent;
    }

    public void setVerticalExtent(VerticalExtentOutput verticalExtent) {
        this.verticalExtent = verticalExtent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        return sb.append(" [ ")
                .append("timestart: ")
                .append((getTimestart() != null) ? getTimestart() : "null")
                .append(", ")
                .append("timestamp: ")
                .append((getTimestamp() != null) ? getTimestamp() : "null")
                .append(", ")
                .append("value: ")
                .append((getValue() != null) ? getValue() : "null")
                .append(" ]")
                .toString();
    }
}
