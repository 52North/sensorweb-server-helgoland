package org.n52.series.ckan.beans;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.sql.Timestamp;
import static junit.framework.TestCase.assertTrue;
import org.joda.time.DateTime;
import org.junit.Test;

public class DescriptionFileTest {
    
    @Test
    public void shouldReturnCorrectLastModifiedDate() {
        CkanDataset dataset = new CkanDataset();
        long currentTimeMillis = System.currentTimeMillis();
        Timestamp lastmodified = new Timestamp(currentTimeMillis);
        dataset.setMetadataModified(lastmodified);
        DescriptionFile descriptionFile = new DescriptionFile(dataset, null, null);
        final DateTime actual = descriptionFile.getLastModified();
        assertTrue(actual.isEqual(currentTimeMillis));
    }
}
