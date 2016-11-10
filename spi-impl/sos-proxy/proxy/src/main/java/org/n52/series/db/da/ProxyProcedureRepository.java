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
import org.n52.io.response.ProcedureOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.ProxySessionAwareRepository;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.dao.ProxyDbQuery;
import org.n52.series.spi.search.SearchResult;

/**
 *
 * @author jansch
 */
public class ProxyProcedureRepository extends ProxySessionAwareRepository implements ProxyOutputAssembler<ProcedureOutput> {

    private final ProcedureRepository repository;

    public ProxyProcedureRepository(ProcedureRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean exists(String id, ProxyDbQuery query) throws DataAccessException {
        return repository.exists(id, query);
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
    public List<ProcedureOutput> getAllCondensed(ProxyDbQuery parameters) throws DataAccessException {
        return repository.getAllCondensed(parameters);
    }

    @Override
    public List<ProcedureOutput> getAllExpanded(ProxyDbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            List<ProcedureOutput> results = new ArrayList<>();
            for (ProcedureEntity procedureEntity : repository.getAllInstances(parameters, session)) {
                results.add(createExpanded(procedureEntity, parameters));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public ProcedureOutput getInstance(String id, ProxyDbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            ProcedureEntity result = repository.getInstance(parseId(id), parameters, session);
            return createExpanded(result, parameters);
        } finally {
            returnSession(session);
        }
    }

    private ProcedureOutput createExpanded(ProcedureEntity entity, ProxyDbQuery parameters) throws DataAccessException {
        ProcedureOutput result = repository.createCondensed(entity, parameters);
        result.setService(createCondensedService(entity.getService()));
        return result;
    }

}
