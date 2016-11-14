/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.series.db.da;

import java.util.Collection;
import java.util.List;
import org.n52.io.request.IoParameters;
import org.n52.io.response.PlatformOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.ProxySessionAwareRepository;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.dao.ProxyDbQuery;
import org.n52.series.spi.search.SearchResult;

/**
 *
 * @author jansch
 */
public class ProxyPlatformRepository extends ProxySessionAwareRepository implements ProxyOutputAssembler<PlatformOutput> {

    private final PlatformRepository repository;

    public ProxyPlatformRepository(PlatformRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PlatformOutput> getAllCondensed(ProxyDbQuery parameters) throws DataAccessException {
        return repository.getAllCondensed(parameters);
    }

    @Override
    public List<PlatformOutput> getAllExpanded(ProxyDbQuery parameters) throws DataAccessException {
        return repository.getAllExpanded(parameters);
    }

    @Override
    public PlatformOutput getInstance(String id, ProxyDbQuery parameters) throws DataAccessException {
        return repository.getInstance(id, parameters);
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
    public boolean exists(String id, ProxyDbQuery query) throws DataAccessException {
        return repository.exists(id, query);
    }

}
