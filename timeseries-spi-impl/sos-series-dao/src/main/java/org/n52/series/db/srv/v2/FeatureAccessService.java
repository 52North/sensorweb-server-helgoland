package org.n52.series.db.srv.v2;

import java.util.ArrayList;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.v2.FeatureOutput;
import org.n52.io.response.v2.FeatureOutputCollection;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.v2.FeatureRepository;
import org.n52.series.db.srv.ServiceInfoAccess;
import org.n52.web.exception.InternalServerException;

public class FeatureAccessService extends ServiceInfoAccess implements ParameterService<FeatureOutput> {

	@Override
	public FeatureOutputCollection getExpandedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			FeatureRepository repository = createFeatureRepository();
			List<FeatureOutput> results = repository.getAllExpanded(dbQuery);
			return new FeatureOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get feature data.");
		}
	}

	@Override
	public FeatureOutputCollection getCondensedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			FeatureRepository repository = createFeatureRepository();
			List<FeatureOutput> results = repository.getAllCondensed(dbQuery);
			return new FeatureOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get feature data.");
		}
	}

	@Override
	public FeatureOutputCollection getParameters(String[] featureIds) {
		return getParameters(featureIds, IoParameters.createDefaults());
	}

	@Override
	public FeatureOutputCollection getParameters(String[] featureIds, IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			FeatureRepository repository = createFeatureRepository();
			List<FeatureOutput> results = new ArrayList<FeatureOutput>();
			for (String featureId : featureIds) {
				results.add(repository.getInstance(featureId, dbQuery));
			}
			return new FeatureOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get feature data.");
		}
	}

	@Override
	public FeatureOutput getParameter(String featureId) {
		return getParameter(featureId, IoParameters.createDefaults());
	}

	@Override
	public FeatureOutput getParameter(String featureId, IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			FeatureRepository repository = createFeatureRepository();
			return repository.getInstance(featureId, dbQuery);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get feature data");
		}
	}

	private FeatureRepository createFeatureRepository() {
		return new FeatureRepository(getServiceInfo());
	}

}
