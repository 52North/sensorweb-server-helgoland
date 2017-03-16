package org.n52.io.extension.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.series.db.da.SessionAwareRepository;

class MetadataRepository extends SessionAwareRepository {

    List<String> getFieldNames(String id) {
        Session session = getSession();
        try {
            DatabaseMetadataDao dao = new DatabaseMetadataDao(session);
            return dao.getMetadataNames(parseId(id));
        } finally {
            returnSession(session);
        }
    }

    Map<String, Object> getExtras(ParameterOutput output, IoParameters parameters) {
        Session session = getSession();
        try {
            DatabaseMetadataDao dao = new DatabaseMetadataDao(session);
            final Set<String> fields = parameters.getFields();
            return fields == null
                    ? convertToOutputs(dao.getAllFor(parseId(output.getId())))
                    : convertToOutputs(dao.getSelected(parseId(output.getId()), fields));
        } finally {
            returnSession(session);
        }
    }

    private Map<String, Object> convertToOutputs(List<MetadataEntity<?>> allInstances) {
        if (allInstances == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> outputs = new HashMap<>();
        for (MetadataEntity<?> entity : allInstances) {
            outputs.put(entity.getName(), entity.toOutput());
        }
        return outputs;
    }

}