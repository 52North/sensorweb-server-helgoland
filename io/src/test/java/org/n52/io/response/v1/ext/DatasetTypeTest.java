package org.n52.io.response.v1.ext;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class DatasetTypeTest {

    @Test
    public void when_datasetId_then_extractDatasetType() {
        Assert.assertThat(DatasetType.extractType("text_234"), Matchers.is("text"));
    }

    @Test
    public void when_datasetId_then_extractId() {
        Assert.assertThat(DatasetType.extractId("text_234"), Matchers.is("234"));
    }
}
