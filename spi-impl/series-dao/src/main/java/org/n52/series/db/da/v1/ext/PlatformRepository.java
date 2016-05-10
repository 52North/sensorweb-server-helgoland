package org.n52.series.db.da.v1.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.hibernate.Session;
import org.n52.io.response.v1.ext.PlatformOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.ext.PlatformEntity;
import org.n52.series.db.da.dao.v1.ext.PlatformDao;
import org.n52.series.db.da.v1.DbQuery;
import org.n52.series.db.da.v1.ExtendedSessionAwareRepository;
import org.n52.series.db.da.v1.OutputAssembler;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class PlatformRepository extends ExtendedSessionAwareRepository implements OutputAssembler<PlatformOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformRepository.class);

    @Override
    public List<PlatformOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<PlatformOutput> results = new ArrayList<>();
            for (PlatformEntity entity : getAllInstances(parameters, session)) {
                results.add(createCondensed(entity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<PlatformOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<PlatformOutput> results = new ArrayList<>();
            for (PlatformEntity entity : getAllInstances(parameters, session)) {
                results.add(createExpanded(entity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public PlatformOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            PlatformEntity result = getInstance(parseId(id), parameters, session);
            return createExpanded(result, parameters);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public Collection<SearchResult> searchFor(String queryString, String locale) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity<? extends I18nEntity>> found, String locale) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private PlatformEntity getInstance(Long id, DbQuery parameters, Session session) throws DataAccessException {
        PlatformDao dao = new PlatformDao(session);
        PlatformEntity result = dao.getInstance(id, parameters);
        if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
        return result;
    }

    private List<PlatformEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
        List<PlatformEntity> platforms = new ArrayList<>();
        platforms.addAll(getAllStationary(parameters, session));
        platforms.addAll(getAllMobile(parameters, session));
        return platforms;
    }

    private List<PlatformEntity> getAllStationary(DbQuery parameters, Session session) throws DataAccessException {
        List<PlatformEntity> platforms = new ArrayList<>();
        platforms.addAll(getAllStationaryInsitu(parameters, session));
        platforms.addAll(getAllStationaryRemote(parameters, session));
        return platforms;
    }

    private List<PlatformEntity> getAllStationaryRemote(DbQuery parameters, Session session) throws DataAccessException {
        LOGGER.warn("not implemented yet.");
        return Collections.emptyList();
    }

    private List<PlatformEntity> getAllStationaryInsitu(DbQuery parameters, Session session) throws DataAccessException {
        LOGGER.warn("not implemented yet.");
        return Collections.emptyList();
    }

    private List<PlatformEntity> getAllMobile(DbQuery parameters, Session session) throws DataAccessException {
        List<PlatformEntity> platforms = new ArrayList<>();
        platforms.addAll(getAllMobileInsitu(parameters, session));
        platforms.addAll(getAllMobileRemote(parameters, session));
        return platforms;
    }

    private List<PlatformEntity> getAllMobileInsitu(DbQuery parameters, Session session) throws DataAccessException {
        LOGGER.warn("not implemented yet.");
        return Collections.emptyList();
    }

    private List<PlatformEntity> getAllMobileRemote(DbQuery parameters, Session session) throws DataAccessException {
        LOGGER.warn("not implemented yet.");
        return Collections.emptyList();
    }

    private PlatformOutput createExpanded(PlatformEntity entity, DbQuery parameters) {
        PlatformOutput output = createCondensed(entity, parameters);

        // TODO
        return output;
    }

    private PlatformOutput createCondensed(PlatformEntity entity, DbQuery parameters) {
        LOGGER.warn("not implemented yet.");
        return new PlatformOutput(null);
    }

}
