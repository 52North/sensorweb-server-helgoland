package org.n52.io.extension.parents;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetType;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HierarchicalEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.da.PlatformRepository;
import org.n52.series.db.da.ProcedureRepository;
import org.n52.series.db.dao.DatasetDao;
import org.n52.series.db.dao.DbQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

class HierarchicalParameterRepository extends PlatformRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(HierarchicalParameterRepository.class);

    @Autowired
    private ProcedureRepository procedureRepository;

    Map<String, Collection<String>> getExtras(String platformId, IoParameters parameters) {
        Session session = getSession();
        try {
            DbQuery dbQuery = getDbQuery(parameters);
            Map<String, Collection<String>> extras = new HashMap<>();

            PlatformOutput platform = getInstance(platformId, dbQuery);
            Collection<DatasetOutput> datasets = platform.getDatasets();
            DatasetDao<DatasetEntity<?>> dao = new DatasetDao<>(session);
            for (DatasetOutput dataset : datasets) {
                String datasetId = DatasetType.extractId(dataset.getId());
                DatasetEntity<?> instance = dao.getInstance(Long.parseLong(datasetId), dbQuery);
                ProcedureEntity procedure = instance.getProcedure();
                Hibernate.initialize(procedure);
                
                Collection<String> parents = getParents(procedure, dbQuery);
                if (!parents.isEmpty()) {
                    if ( !extras.containsKey("procedures")) {
                        extras.put("procedures", new HashSet<>());
                    }
                    extras.get("procedures").addAll(parents);
                }
            }

            return extras;
        } catch (NumberFormatException e) {
            LOGGER.debug("Could not convert id '{}' to long.", platformId, e);
        } catch (DataAccessException e) {
            LOGGER.error("Could not query hierarchical parameters for dataset with id '{}'", platformId, e);
        } finally {
            returnSession(session);
        }
        return Collections.emptyMap();
    }

    private Collection<String> getParents(ProcedureEntity entity, DbQuery query) {
        if (entity.hasParents()) {
            return entity.getParents().stream()
                    .map(e -> createCondensed(new ProcedureOutput(), e, query).getId())
                    .collect(Collectors.toList());
        }
        return Collections.singleton(createCondensed(new ProcedureOutput(), entity, query).getId());
    }
}
