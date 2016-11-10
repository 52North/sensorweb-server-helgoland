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
import org.n52.io.response.CategoryOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.ProxySessionAwareRepository;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.dao.ProxyDbQuery;
import org.n52.series.spi.search.SearchResult;

/**
 *
 * @author jansch
 */
public class ProxyCategoryRepository extends ProxySessionAwareRepository implements ProxyOutputAssembler<CategoryOutput> {

    private final CategoryRepository repository;

    public ProxyCategoryRepository(CategoryRepository repository) {
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
    public List<CategoryOutput> getAllCondensed(ProxyDbQuery parameters) throws DataAccessException {
        return repository.getAllCondensed(parameters);
    }

    @Override
    public List<CategoryOutput> getAllExpanded(ProxyDbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<CategoryOutput> results = new ArrayList<>();
            for (CategoryEntity categoryEntity : repository.getAllInstances(parameters, session)) {
                results.add(createExpanded(categoryEntity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public CategoryOutput getInstance(String id, ProxyDbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            CategoryEntity entity = repository.getInstance(parseId(id), parameters, session);
            if (entity != null) {
                return createExpanded(entity, parameters);
            }
            return null;
        } finally {
            returnSession(session);
        }
    }

    private CategoryOutput createExpanded(CategoryEntity entity, ProxyDbQuery parameters) throws DataAccessException {
        CategoryOutput result = repository.createCondensed(entity, parameters);
        result.setService(createCondensedService(entity.getService()));
        return result;
    }
}
