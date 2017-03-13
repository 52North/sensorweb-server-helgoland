package org.n52.series.api.v1.db.srv;

import org.n52.io.IoParameters;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.UndesignedParameterSet;

public class MergingTimeseriesAccessService extends TimeseriesAccessService {

    @Override
    public TvpDataCollection getTimeseriesData(UndesignedParameterSet parameters) {
        // TODO Auto-generated method stub
        return super.getTimeseriesData(parameters);
    }

    @Override
    public TimeseriesMetadataOutput[] getExpandedParameters(IoParameters query) {
        // TODO Auto-generated method stub
        return super.getExpandedParameters(query);
    }

    @Override
    public TimeseriesMetadataOutput[] getCondensedParameters(IoParameters query) {
        // TODO Auto-generated method stub
        return super.getCondensedParameters(query);
    }

    @Override
    public TimeseriesMetadataOutput[] getParameters(String[] items) {
        // TODO Auto-generated method stub
        return super.getParameters(items);
    }

    @Override
    public TimeseriesMetadataOutput[] getParameters(String[] items, IoParameters query) {
        // TODO Auto-generated method stub
        return super.getParameters(items, query);
    }

    @Override
    public TimeseriesMetadataOutput getParameter(String item) {
        // TODO Auto-generated method stub
        return super.getParameter(item);
    }

    @Override
    public TimeseriesMetadataOutput getParameter(String item, IoParameters query) {
        // TODO Auto-generated method stub
        return super.getParameter(item, query);
    }

    
}
