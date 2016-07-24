package org.n52.io.extension;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.n52.series.db.beans.MeasurementDataEntity;

public class MeasurementDataEntityTest {

    @Test
    public void when_noDataCollectionContainsValue_then_detectNoDataValue() {
        Collection<String> noDataValues = Arrays.asList(new String[] {"9999","-9999.9"});
        MeasurementDataEntity entity = new MeasurementDataEntity();
        entity.setValue(9999d);
        assertTrue(entity.isNoDataValue(noDataValues));
    }
}
