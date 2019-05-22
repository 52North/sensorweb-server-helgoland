
package org.n52.io.handler.profile;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
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
import org.n52.io.response.dataset.profile.ProfileDataItem;
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
                List<DatasetOutput<ProfileValue< ? >>> metadatas = getMetadatas();
                DatasetOutput<ProfileValue< ? >> dataset = metadatas.get(0);

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
        metaHeader.append("Sensor: ")
                  .append(getPlatformLabel(metadata))
                  .append("\n");
        metaHeader.append("Unit: ")
                  .append(metadata.getUom())
                  .append("\n");

        return isTrajectory(metadata)
                ? createTrajectoryHeader(metaHeader)
                : createSimpleHeader(metadata, metaHeader);

    }

    private String[] createSimpleHeader(DatasetOutput<ProfileValue< ? >> metadata, StringBuilder metaHeader) {
        FeatureOutput feature = metadata.getFeature();
        metaHeader.append("Feature: ")
                  .append(getLabel(feature))
                  .append("\n");

        Geometry geometry = feature.getGeometry();
        metaHeader.append("Geometry: ")
                  .append(geometry.toText())
                  .append("\n");

        /*
         * Note: last line break will cause an empty first column
         */
        return new String[] {
            metaHeader.toString(),
            "time",
            "z-value",
            "value"
        };
    }

    private String[] createTrajectoryHeader(StringBuilder metaHeader) {
        return new String[] {
            // Note: first column after last line break
            metaHeader.append("geometry")
                      .toString(),
            "time",
            "z-value",
            "value"
        };
    }

    @Override
    protected void writeData(DatasetOutput<ProfileValue< ? >> metadata,
                             Data<ProfileValue< ? >> series,
                             OutputStream stream)
            throws IOException {
        for (ProfileValue< ? > profile : series.getValues()) {
            for (ProfileDataItem< ? > value : profile.getValue()) {
                String[] row = new String[getHeader(metadata).length];
                // metaHeader leaves first column empty
                row[0] = isTrajectory(metadata)
                        ? profile.getGeometry()
                                 .toString()
                        : "";
                row[1] = parseTime(profile);
                row[2] = formatVertical(value);
                row[3] = value.getFormattedValue();
                writeText(csvEncode(row), stream);
            }
        }
    }

    private String formatVertical(ProfileDataItem< ? > value) {
        BigDecimal vertical = value.getVertical();
        BigDecimal verticalFrom = value.getVerticalFrom();
        BigDecimal verticalTo = value.getVerticalTo();
        return vertical != null
                ? vertical.toString()
                : verticalFrom.toString() + "-" + verticalTo.toString();
    }

    @Override
    protected String getFilenameFor(DatasetOutput<ProfileValue< ? >> seriesMetadata) {
        return seriesMetadata.getId();
    }

}
