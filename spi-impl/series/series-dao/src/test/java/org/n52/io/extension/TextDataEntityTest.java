package org.n52.io.extension;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.n52.series.db.beans.TextDataEntity;

public class TextDataEntityTest {

    @Test
    public void when_noDataCollectionContainsValue_then_detectNoDataValue() {
        Collection<String> noDataValues = Arrays.asList(new String[] {"blubb","-9999.9"});
        TextDataEntity entity = new TextDataEntity();
        entity.setValue("blubb");
        assertTrue(entity.isNoDataValue(noDataValues));
    }
}
