package org.n52.series.db.srv.v2;

import java.util.ArrayList;
import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.v2.PlatformOutput;
import org.n52.io.response.v2.PlatformOutputCollection;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.v2.PlatformRepository;
import org.n52.series.db.srv.ServiceInfoAccess;
import org.n52.web.exception.InternalServerException;

public class PlatformAccessService extends ServiceInfoAccess implements ParameterService<PlatformOutput> {

	@Override
	public PlatformOutputCollection getExpandedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			PlatformRepository repository = createPlatformRepository();
			List<PlatformOutput> results = repository.getAllExpanded(dbQuery);
			return new PlatformOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data.");
		}
	}

	@Override
	public PlatformOutputCollection getCondensedParameters(IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			PlatformRepository repository = createPlatformRepository();
			List<PlatformOutput> results = repository.getAllCondensed(dbQuery);
			return new PlatformOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data.");
		}
	}

	@Override
	public PlatformOutputCollection getParameters(String[] ids) {
		return getParameters(ids, IoParameters.createDefaults());
	}

	@Override
	public PlatformOutputCollection getParameters(String[] ids, IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			PlatformRepository repository = createPlatformRepository();
			List<PlatformOutput> results = new ArrayList<PlatformOutput>();
			for (String id : ids) {
				results.add(repository.getInstance(id, dbQuery));
			}
			return new PlatformOutputCollection(results);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data.");
		}
	}

	@Override
	public PlatformOutput getParameter(String platformId) {
		return getParameter(platformId, IoParameters.createDefaults());
	}

	@Override
	public PlatformOutput getParameter(String platformId, IoParameters query) {
		try {
			DbQuery dbQuery = DbQuery.createFrom(query);
			PlatformRepository repository = createPlatformRepository();
			return repository.getInstance(platformId, dbQuery);
		} catch (DataAccessException e) {
			throw new InternalServerException("Could not get platform data");
		}
	}

	private PlatformRepository createPlatformRepository() {
		return new PlatformRepository(getServiceInfo());
	}
}
