package org.n52.io.extension;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.n52.series.db.beans.CountDataEntity;

public class CountDataEntityTest {

    @Test
    public void when_noDataCollectionContainsValue_then_detectNoDataValue() {
        Collection<String> noDataValues = Arrays.asList(new String[] {"9999","-9999.9"});
        CountDataEntity entity = new CountDataEntity();
        entity.setValue(9999);
        assertTrue(entity.isNoDataValue(noDataValues));
    }
}
