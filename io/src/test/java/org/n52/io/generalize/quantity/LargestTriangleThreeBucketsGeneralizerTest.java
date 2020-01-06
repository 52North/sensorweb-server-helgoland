/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.io.generalize.quantity;

import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.util.Random;

import org.hamcrest.core.Is;
import org.joda.time.DateTime;
import org.junit.Test;
import org.n52.io.TvpDataCollection;
import org.n52.io.request.IoParameters;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.type.quantity.generalize.Generalizer;
import org.n52.io.type.quantity.generalize.GeneralizerException;
import org.n52.io.type.quantity.generalize.LargestTriangleThreeBucketsGeneralizer;

public class LargestTriangleThreeBucketsGeneralizerTest {

    private final Random random = new Random();

    @Test
    public void when_quotientHasNonterminatingDecimals_then_noArithmeticExceptionIsThrown()
            throws GeneralizerException {
        // https://github.com/52North/series-rest-api/issues/446

        TvpDataCollection<Data<QuantityValue>> collection = new TvpDataCollection<>();
        collection.addNewSeries("test", getData(10000));

        long threshold = 100L;
        IoParameters defaults = IoParameters.createDefaults().extendWith("threshold", Long.toString(threshold));
        Generalizer<Data<QuantityValue>> generalizer = new LargestTriangleThreeBucketsGeneralizer(defaults);
        DataCollection<Data<QuantityValue>> generalizedData = generalizer.generalize(collection);
        assertThat(generalizedData.getSeries("test").size(), Is.is(threshold));
    }

    private Data<QuantityValue> getData(int maxValues) {
        BigDecimal startValue = BigDecimal.valueOf(0);
        QuantityValue current = createQuantityValue(DateTime.now(), startValue);

        Data<QuantityValue> data = new Data<>();
        for (int i = 0; i < maxValues; i++) {
            data.addNewValue(current);
            current = getNextDataValue(current);
        }
        return data;
    }

    private QuantityValue getNextDataValue(QuantityValue value) {
        return createQuantityValue(value.getTimestamp().getDateTime().plusMillis(1),
                BigDecimal.valueOf(random.nextDouble()));
    }

    private QuantityValue createQuantityValue(DateTime time, BigDecimal value) {
        QuantityValue quantityValue = new QuantityValue();
        quantityValue.setTimestamp(new TimeOutput(time));
        quantityValue.setValue(value);
        return quantityValue;
    }
}

