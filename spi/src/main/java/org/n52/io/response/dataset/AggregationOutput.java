/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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
package org.n52.io.response.dataset;

import java.math.BigDecimal;

import org.n52.io.response.OptionalOutput;
import org.n52.io.response.SelfSerializedOutput;

public class AggregationOutput<V extends AbstractValue<?>> extends SelfSerializedOutput {

    private OptionalOutput<V> min;
    private OptionalOutput<V> max;
    private OptionalOutput<Long> count;
    private OptionalOutput<BigDecimal> avg;


    public V getMin() {
        return getIfSerialized(min);
    }

    public AggregationOutput<V> setMin(OptionalOutput<V> min) {
        this.min = min;
        return this;
    }

    public V getMax() {
        return getIfSerialized(max);
    }

    public AggregationOutput<V> setMax(OptionalOutput<V> max) {
        this.max = max;
        return this;
    }

    /**
     * @return the count
     */
    public Long getCount() {
        return getIfSerialized(count);
    }

    /**
     * @param count the count to set
     * @return this
     */
    public AggregationOutput<V> setCount(OptionalOutput<Long> count) {
        this.count = count;
        return this;
    }

    /**
     * @return the avg
     */
    public BigDecimal getAvg() {
        return getIfSerialized(avg);
    }

    /**
     * @param avg the avg to set
     * @return this
     */
    public AggregationOutput<V> setAvg(OptionalOutput<BigDecimal> avg) {
        this.avg = avg;
        return this;
    }

    public boolean isEmpty() {
        return min != null && min.isAbsent() && max != null && max.isAbsent() && count != null && count.isAbsent()
                && avg != null && avg.isAbsent();
    }
}
