package org.n52.series.db.srv.v1.ext;

import org.n52.io.format.TvpDataCollection;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.v1.ext.SeriesMetadataOutput;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.sensorweb.spi.SeriesDataService;
import org.n52.series.db.da.v1.ext.SeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class SeriesAccessService extends ParameterService<SeriesMetadataOutput> implements SeriesDataService {

    @Autowired
    private SeriesRepository seriesRepository;

    @Override
    public OutputCollection<SeriesMetadataOutput> getExpandedParameters(IoParameters query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OutputCollection<SeriesMetadataOutput> getCondensedParameters(IoParameters query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OutputCollection<SeriesMetadataOutput> getParameters(String[] items) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OutputCollection<SeriesMetadataOutput> getParameters(String[] items, IoParameters query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SeriesMetadataOutput getParameter(String item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SeriesMetadataOutput getParameter(String item, IoParameters query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TvpDataCollection getSeriesData(RequestSimpleParameterSet parameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
