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
package org.n52.io;

public interface Constants {

    String APPLICATION_JSON = "application/json";
    String APPLICATION_PDF = "application/pdf";
    String APPLICATION_ZIP = "application/zip";
    String IMAGE_PNG = "image/png";
    String TEXT_CSV = "text/csv";

    enum MimeType {

        APPLICATION_JSON(Constants.APPLICATION_JSON, "json"),
        APPLICATION_PDF(Constants.APPLICATION_PDF, "pdf"),
        APPLICATION_ZIP(Constants.APPLICATION_ZIP, "zip"),
        IMAGE_PNG(Constants.IMAGE_PNG, "png"),
        TEXT_CSV(Constants.TEXT_CSV, "csv");

        private final String mimeType;

        private final String formatName;

        MimeType(String mimeType, String formatName) {
            this.mimeType = mimeType;
            this.formatName = formatName;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getFormatName() {
            return formatName;
        }

        public static boolean isKnownMimeType(String value) {
            for (MimeType type : values()) {
                if (type.formatName.equalsIgnoreCase(value)
                        || type.mimeType.equalsIgnoreCase(value)) {
                    return true;
                }
            }
            return false;
        }

        public static MimeType toInstance(String value) {
            for (MimeType type : values()) {
                if (type.formatName.equalsIgnoreCase(value)
                        || type.mimeType.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("'" + value + "' is not of type " + MimeType.class.getName());
        }

        @Override
        public String toString() {
            return getMimeType();
        }

    }


}
