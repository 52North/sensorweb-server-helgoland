package org.n52.series.dwd.srv;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Test;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.DatasetType;
import org.n52.io.response.dataset.dwd.DwdAlertDatasetOutput;
import org.n52.series.dwd.beans.WarnCell;
import org.n52.series.dwd.rest.Alert;
import org.n52.series.dwd.rest.VorabInformationAlert;

public class DatasetOutputAdapterTest {
    
    @Test
    public void when_warnCellAndAlert_then_createOutput() {
        WarnCell warnCell = new WarnCell("109771000");
        Alert alert = new VorabInformationAlert();
        alert.setEvent("VORABINFORMATION HEFTIGER / ERGIEBIGER REGEN");

        DatasetOutputAdapter adapter = new DatasetOutputAdapter(null, null);
        DwdAlertDatasetOutput output = adapter.createCondensed(alert, warnCell, IoParameters.createDefaults());
        assertThat(DatasetType.extractId(output.getId()), is("109771000-VORABINFORMATION HEFTIGER - ERGIEBIGER REGEN"));
    }
    
    @Test
    public void when_incomingVorabinformationIdWithSpacesAndSlash_then_parseId() {
        DatasetOutputAdapter adapter = new DatasetOutputAdapter(null, null);
        List<String> parts = adapter.parseId("109771000-VORABINFORMATION HEFTIGER - ERGIEBIGER REGEN");
        assertThat(parts.get(0), is("109771000"));
        assertThat(parts.get(1), is("VORABINFORMATION HEFTIGER / ERGIEBIGER REGEN"));
    }
    
}
