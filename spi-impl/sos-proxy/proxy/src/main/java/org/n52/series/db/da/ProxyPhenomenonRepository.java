/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.series.db.da;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.PhenomenonOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.ProxySessionAwareRepository;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.dao.ProxyDbQuery;
import org.n52.series.db.dao.ProxyPhenomenonDao;
import org.n52.series.spi.search.PhenomenonSearchResult;
import org.n52.series.spi.search.SearchResult;
import org.n52.web.exception.ResourceNotFoundException;

/**
 *
 * @author jansch
 */
public class ProxyPhenomenonRepository extends ProxySessionAwareRepository implements ProxyOutputAssembler<PhenomenonOutput> {

    private final PhenomenonRepository repository;

    public ProxyPhenomenonRepository(PhenomenonRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean exists(String id, ProxyDbQuery parameters) throws DataAccessException {
        return repository.exists(id, parameters);
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
        return repository.searchFor(parameters);
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, ProxyDbQuery query) {
        return repository.convertToSearchResults(found, query);
    }

    @Override
    public List<PhenomenonOutput> getAllCondensed(ProxyDbQuery parameters) throws DataAccessException {
        return repository.getAllCondensed(parameters);
    }

    @Override
    public List<PhenomenonOutput> getAllExpanded(ProxyDbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<PhenomenonOutput> results = new ArrayList<>();
            for (PhenomenonEntity phenomenonEntity : repository.getAllInstances(parameters, session)) {
                results.add(createExpanded(phenomenonEntity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public PhenomenonOutput getInstance(String id, ProxyDbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            PhenomenonEntity result = repository.getInstance(parseId(id), parameters, session);
            return createExpanded(result, parameters);
        } finally {
            returnSession(session);
        }
    }

    private PhenomenonOutput createExpanded(PhenomenonEntity entity, ProxyDbQuery parameters) throws DataAccessException {
        PhenomenonOutput result = repository.createCondensed(entity, parameters);
        result.setService(createCondensedService(entity.getService()));
        return result;
    }
}
