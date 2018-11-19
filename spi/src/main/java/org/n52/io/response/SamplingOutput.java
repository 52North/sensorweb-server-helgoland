package org.n52.io.response;

public class SamplingOutput extends ParameterOutput {

    public static final String COLLECTION_PATH = "samplings";

    @Override
    protected String getCollectionName() {
        return COLLECTION_PATH;
    }
}
