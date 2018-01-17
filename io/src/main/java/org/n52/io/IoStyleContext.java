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
package org.n52.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.n52.io.quantity.QuantityIoFactory;
import org.n52.io.request.IoParameters;
import org.n52.io.request.StyleProperties;
import org.n52.io.response.dataset.DatasetOutput;

public final class IoStyleContext {

    private final Map<String, StyleMetadata> styleMetadatas;

    private IoStyleContext(Map<String, StyleMetadata> metadatas) {
        this.styleMetadatas = metadatas == null
                ? Collections.emptyMap()
                : metadatas;
    }

    public static IoStyleContext createEmpty() {
        return new IoStyleContext(Collections.emptyMap());
    }

    /**
     * @param parameters
     *        the style definitions.
     * @param metadatas
     *        the metadata for each dataset.
     * @throws NullPointerException
     *         if any of the given arguments is <code>null</code>.
     * @throws IllegalStateException
     *         if amount of datasets described by the given arguments is not in sync.
     * @return a rendering context to be used by {@link QuantityIoFactory} to create an {@link IoHandler}.
     */
    public static IoStyleContext createContextWith(IoParameters parameters,
                                                   List< ? extends DatasetOutput< ?>> metadatas) {
        if (parameters == null || metadatas == null) {
            throw new NullPointerException("Designs and metadatas cannot be null.!");
        }

        final Map<String, StyleProperties> styles = new HashMap<>(parameters.getReferencedStyles());
        associateBackwardsCompatibleSingleStyle(parameters, metadatas, styles);
        return new IoStyleContext(collectStyleMetadatas(metadatas, styles));
    }

    private static void associateBackwardsCompatibleSingleStyle(IoParameters parameters,
                                                                List< ? extends DatasetOutput< ? >> metadatas,
                                                                Map<String, StyleProperties> styles) {
        if (styles.isEmpty() && metadatas.size() == 1) {
            // no referenced styles are given so associate
            // backwards compatible single style
            DatasetOutput< ? > metadata = metadatas.get(0);
            styles.put(metadata.getId(), parameters.getSingleStyle());
        }
    }

    private static Map<String, StyleMetadata> collectStyleMetadatas(List< ? extends DatasetOutput< ? >> metadatas,
                                                                    final Map<String, StyleProperties> styles) {
        return metadatas.stream()
                        .map(e -> {
                            return new StyleMetadata().setDatasetMetadata(e)
                                                      .setDatasetId(e.getId())
                                                      .setStyleProperties(styles.get(e.getId()));
                        })
                        .collect(Collectors.toMap(StyleMetadata::getDatasetId, Function.identity()));
    }

    public List<DatasetOutput< ? >> getAllDatasetMetadatas() {
        return styleMetadatas.values()
                             .stream()
                             .map(e -> e.getDatasetMetadata())
                             .collect(Collectors.toList());
    }

    public Optional<StyleMetadata> getStyleMetadataFor(String datasetId) {
        return Optional.of(styleMetadatas.get(datasetId));
    }

    public StyleProperties getReferenceDatasetStyleOptions(String datasetId, String referenceDatasetId) {
        Optional<StyleMetadata> styleMetadata = getStyleMetadataFor(datasetId);
        return styleMetadata.isPresent()
                ? getReferenceDatasetStyleOptions(styleMetadata.get(), referenceDatasetId)
                : null;
    }

    private StyleProperties getReferenceDatasetStyleOptions(StyleMetadata styleMetadata, String referenceDatasetId) {
        StyleProperties styleProperties = styleMetadata.getStyleProperties();
        Map<String, StyleProperties> properties = styleProperties.getReferenceValueStyleProperties();
        return properties.containsKey(referenceDatasetId)
                ? properties.get(referenceDatasetId)
                : null;
    }

    public static class StyleMetadata {
        private String datasetId;
        private DatasetOutput< ? > datasetMetadata;
        private StyleProperties styleProperties;

        public String getDatasetId() {
            return datasetId;
        }

        public StyleMetadata setDatasetId(String datasetId) {
            this.datasetId = datasetId;
            return this;
        }

        public DatasetOutput< ? > getDatasetMetadata() {
            return datasetMetadata;
        }

        public StyleMetadata setDatasetMetadata(DatasetOutput< ? > datasetMetadata) {
            this.datasetMetadata = datasetMetadata;
            return this;
        }

        public StyleProperties getStyleProperties() {
            return styleProperties;
        }

        public StyleMetadata setStyleProperties(StyleProperties styleProperties) {
            this.styleProperties = styleProperties;
            return this;
        }
    }

}
