package org.n52.series.db.da.v1.ext;

import java.util.Collection;
import java.util.List;
import org.n52.io.response.v1.PhenomenonOutput;
import org.n52.io.response.v1.ext.SeriesMetadataOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.v1.DbQuery;
import org.n52.series.db.da.v1.ExtendedSessionAwareRepository;
import org.n52.series.db.da.v1.OutputAssembler;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class SeriesRepository extends ExtendedSessionAwareRepository implements OutputAssembler<SeriesMetadataOutput> {

    @Override
    public List<SeriesMetadataOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SeriesMetadataOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SeriesMetadataOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<SearchResult> searchFor(String queryString, String locale) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity<? extends I18nEntity>> found, String locale) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
