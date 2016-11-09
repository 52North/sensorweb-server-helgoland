/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.series.db.da;

import java.util.Collection;
import java.util.List;
import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.CategoryOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.SessionAwareRepository;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.ProxyCategoryDao;
import org.n52.series.spi.search.SearchResult;

/**
 *
 * @author jansch
 */
public class ProxyCategoryRepository extends SessionAwareRepository implements OutputAssembler<CategoryOutput>{
    private final CategoryRepository repository;

    public ProxyCategoryRepository(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CategoryOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        return repository.getAllCondensed(parameters);
    }

    @Override
    public List<CategoryOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        return repository.getAllExpanded(parameters);
    }

    @Override
    public CategoryOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        return  repository.getInstance(id, parameters);
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
        return repository.searchFor(parameters);
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, DbQuery query) {
        return repository.convertToSearchResults(found, query);
    }

    @Override
    public boolean exists(String id, DbQuery query) throws DataAccessException {
        return repository.exists(id, query);
    }

//    protected List<CategoryTEntity> getAllInstances(DbQuery parameters, Session session) throws DataAccessException {
//        return createDao(session).getAllInstances(parameters);
//    }

    private ProxyCategoryDao createDao(Session session) {
        return new ProxyCategoryDao(session);
    }

    private CategoryOutput createCondensed(CategoryEntity entity, DbQuery parameters) {
        CategoryOutput result = new CategoryOutput();
        result.setId(Long.toString(entity.getPkid()));
        result.setLabel(entity.getLabelFrom(parameters.getLocale()));
        result.setDomainId(entity.getDomainId());
        checkForHref(result, parameters);
        return result;
    }

    private void checkForHref(CategoryOutput result, DbQuery parameters) {
        if (parameters.getHrefBase() != null) {
            result.setHrefBase(urHelper.getCategoriesHrefBaseUrl(parameters.getHrefBase()));
        }
    }

}
