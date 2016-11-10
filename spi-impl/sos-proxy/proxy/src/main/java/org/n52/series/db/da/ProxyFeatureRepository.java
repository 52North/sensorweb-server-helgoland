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
import org.n52.io.response.FeatureOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.ProxySessionAwareRepository;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.dao.ProxyDbQuery;
import org.n52.series.db.dao.ProxyFeatureDao;
import org.n52.series.spi.search.FeatureSearchResult;
import org.n52.series.spi.search.SearchResult;
import org.n52.web.exception.ResourceNotFoundException;

/**
 *
 * @author jansch
 */
public class ProxyFeatureRepository extends ProxySessionAwareRepository implements ProxyOutputAssembler<FeatureOutput> {

    private final FeatureRepository repository;

    public ProxyFeatureRepository(FeatureRepository repository) {
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
    public List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity> found, ProxyDbQuery query) {
        return repository.convertToSearchResults(found, query);
    }

    @Override
    public List<FeatureOutput> getAllCondensed(ProxyDbQuery parameters) throws DataAccessException {
        return repository.getAllCondensed(parameters);
    }

    @Override
    public List<FeatureOutput> getAllExpanded(ProxyDbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            ProxyFeatureDao featureDao = new ProxyFeatureDao(session);
            List<FeatureOutput> results = new ArrayList<>();
            for (FeatureEntity featureEntity : featureDao.getAllInstances(parameters)) {
                results.add(createExpanded(featureEntity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public FeatureOutput getInstance(String id, ProxyDbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            ProxyFeatureDao featureDao = new ProxyFeatureDao(session);
            FeatureEntity result = featureDao.getInstance(parseId(id), parameters);
            if (result == null) {
                throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
            }
            return createExpanded(result, parameters);
        } finally {
            returnSession(session);
        }
    }

    private FeatureOutput createExpanded(FeatureEntity entity, ProxyDbQuery parameters) throws DataAccessException {
        FeatureOutput result = repository.createCondensed(entity, parameters);
        result.setService(createCondensedService(entity.getService()));
        return result;
    }

}
