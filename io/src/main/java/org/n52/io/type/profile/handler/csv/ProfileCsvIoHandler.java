
package org.n52.io.type.profile.handler.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.n52.io.IoParseException;
import org.n52.io.handler.CsvIoHandler;
import org.n52.io.handler.IoProcessChain;
import org.n52.io.request.IoParameters;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetParameters;
import org.n52.io.response.dataset.profile.ProfileValue;

public class ProfileCsvIoHandler extends CsvIoHandler<ProfileValue< ? >> {

    public ProfileCsvIoHandler(IoParameters parameters,
                               IoProcessChain<Data<ProfileValue< ? >>> processChain,
                               List< ? extends DatasetOutput<ProfileValue< ? >>> seriesMetadatas) {
        super(parameters, processChain, seriesMetadatas);
    }

    @Override
    public void encodeAndWriteTo(DataCollection<Data<ProfileValue< ? >>> data, OutputStream stream)
            throws IoParseException {
        try {
            if (isZipOutput() || data.size() > 1) {
                writeAsZipStream(data, stream);
            } else if (data.size() == 1) {
                List<DatasetOutput<ProfileValue<?>>> metadatas = getMetadatas();
                DatasetOutput<ProfileValue<?>> dataset = metadatas.get(0);

                writeHeader(dataset, stream);
                writeData(dataset, data.getSeries(dataset.getId()), stream);
            } else {
                writeText("nodata", stream);
            }
        } catch (IOException e) {
            throw new IoParseException("Could not write CSV to output stream.", e);
        }
    }

    @Override
    protected String[] getHeader(DatasetOutput<ProfileValue< ? >> metadata) {
        StringBuilder metaHeader = new StringBuilder();
        DatasetParameters datasetParameters = metadata.getDatasetParameters(true);
        metaHeader.append("Phenomenon: ")
                  .append(getLabel(datasetParameters.getPhenomenon()))
                  .append("\n");

        FeatureOutput feature = metadata.getFeature();
        metaHeader.append("Feature: ")
                  .append(getLabel(feature))
                  .append("\n");

        metaHeader.append("Sensor: ")
                  .append(getPlatformLabel(metadata))
                  .append("\n");

        metaHeader.append("Unit: ")
                  .append(metadata.getUom())
                  .append("\n");

        Geometry geometry = feature.getGeometry();
        metaHeader.append("Location: ")
                  .append(geometry.toText())
                  .append("\n");

        return new String[] {
            metaHeader.toString(),

            // TODO verticalExtent to be encoded in each value?!

            "42"
        };
    }

    @Override
    protected void writeData(DatasetOutput<ProfileValue< ? >> metadata,
                             Data<ProfileValue< ? >> series,
                             OutputStream stream)
            throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getFilenameFor(DatasetOutput<ProfileValue< ? >> seriesMetadata) {
        // TODO Auto-generated method stub
        return "";
    }

}
