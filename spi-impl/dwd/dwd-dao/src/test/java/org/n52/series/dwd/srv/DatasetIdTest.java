package org.n52.series.dwd.srv;

import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetType;
import org.n52.io.response.dataset.dwd.DwdAlertDatasetOutput;
import org.n52.series.dwd.srv.DatasetOutputAdapter;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class DatasetIdTest {

    private DatasetOutputAdapter adapter = new DatasetOutputAdapter(null, null);
    
    private String prefix = "dwd-alert_";
    
    private String warnCell = "123456";
    
    private String phenomenon = "temperature";
    
    @Test
    public void testCreateId() {
        DwdAlertDatasetOutput output = createDwdAlertDatasetOutput();
        Assert.assertThat(output.getId().equals(createId()), Matchers.is(true));
    }
    
    @Test
    public void testParseId() {
        DwdAlertDatasetOutput output = createDwdAlertDatasetOutput();
        List<String> parseId = adapter.parseId(DatasetType.extractId(output.getId()));
        Assert.assertThat(parseId.size(), Matchers.is(2));
        Assert.assertThat(parseId.get(0).equals(warnCell), Matchers.is(true));
        Assert.assertThat(parseId.get(1).equals(phenomenon), Matchers.is(true));
    }
    
    private String createId() {
        StringBuilder builder = new StringBuilder();
        builder.append(DwdAlertDatasetOutput.DATASET_TYPE).append("_").append(warnCell).append("-").append(phenomenon);
        return builder.toString();
    }
    
    private DwdAlertDatasetOutput createDwdAlertDatasetOutput() {
        DwdAlertDatasetOutput output = new DwdAlertDatasetOutput();
        output.setId(adapter.createId(warnCell, phenomenon));
        return output;
    }
}
