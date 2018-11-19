package org.n52.io.response;

public class MeasuringOutput extends ParameterOutput {

    public static final String COLLECTION_PATH = "measuringPrograms";

    @Override
    protected String getCollectionName() {
        return COLLECTION_PATH;
    }
}
