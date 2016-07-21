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
package org.n52.io.response.dataset.dwd;

public class DwdAlert {

    private String warning;
    private String description;
    private String instructions;
    private int type;
    private int level;
    private String state;
    private String stateShort;
    private int altitudeStart;
    private int altitudeEnd;
    private long validFrom;
    private long validUntil;

    public String getWarning() {
        return warning;
    }

    public DwdAlert setWarning(String warning) {
        this.warning = warning;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DwdAlert setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getInstructions() {
        return instructions;
    }

    public DwdAlert setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }

    public int getType() {
        return type;
    }

    public DwdAlert setType(int type) {
        this.type = type;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public DwdAlert setLevel(int level) {
        this.level = level;
        return this;
    }

    public String getState() {
        return state;
    }

    public DwdAlert setState(String state) {
        this.state = state;
        return this;
    }

    public String getStatreShort() {
        return stateShort;
    }

    public DwdAlert setStateShort(String stateShort) {
        this.stateShort = stateShort;
        return this;
    }

    public int getAltitudeStart() {
        return altitudeStart;
    }

    public DwdAlert setAltitudeStart(int altitudeStart) {
        this.altitudeStart = altitudeStart;
        return this;
    }

    public int getAltitudeEnd() {
        return altitudeEnd;
    }

    public DwdAlert setAltitudeEnd(int altitudeEnd) {
        this.altitudeEnd = altitudeEnd;
        return this;
    }

    public long getValidFrom() {
        return validFrom;
    }

    public DwdAlert setValidFrom(long validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public long getValidUntil() {
        return validUntil;
    }

    public DwdAlert setValidUntil(long validUntil) {
        this.validUntil = validUntil;
        return this;
    }
}
