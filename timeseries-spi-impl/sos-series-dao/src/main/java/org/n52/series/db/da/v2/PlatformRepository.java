package org.n52.series.db.da.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.response.v2.MobilePlatformOutput;
import org.n52.io.response.v2.PlatformOutput;
import org.n52.io.response.v2.StationaryPlatformOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.sensorweb.spi.search.FeatureSearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.OutputAssembler;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.FeatureEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.series.db.da.dao.FeatureDao;
import org.n52.web.exception.ResourceNotFoundException;

import com.vividsolutions.jts.geom.Point;

public class PlatformRepository extends SessionAwareRepository implements OutputAssembler<PlatformOutput> {

	public PlatformRepository(ServiceInfo serviceInfo) {
		super(serviceInfo);
	}

	@Override
	public Collection<SearchResult> searchFor(String searchString, String locale) {
		Session session = getSession();
		try {
			FeatureDao featureDao = new FeatureDao(session);
			DbQuery parameters = createDefaultsWithLocale(locale);
			List<FeatureEntity> found = featureDao.find(searchString, parameters);
			return convertToSearchResults(found, locale);
		} finally {
			returnSession(session);
		}
	}

	@Override
	protected List<SearchResult> convertToSearchResults(List<? extends DescribableEntity<? extends I18nEntity>> found,
			String locale) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		for (DescribableEntity<? extends I18nEntity> searchResult : found) {
			String pkid = searchResult.getPkid().toString();
			String label = getLabelFrom(searchResult, locale);
			results.add(new FeatureSearchResult(pkid, label));
		}
		return results;
	}

	@Override
	public List<PlatformOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
		Session session = getSession();
		try {
			FeatureDao featureDao = new FeatureDao(session);
			List<PlatformOutput> results = new ArrayList<PlatformOutput>();
			for (FeatureEntity featureEntity : featureDao.getAllInstances(parameters)) {
				results.add(createCondensed(featureEntity, parameters));
			}
			return results;
		} finally {
			returnSession(session);
		}
	}

	@Override
	public List<PlatformOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
		Session session = getSession();
		try {
			FeatureDao featureDao = new FeatureDao(session);
			List<PlatformOutput> results = new ArrayList<PlatformOutput>();
			for (FeatureEntity featureEntity : featureDao.getAllInstances(parameters)) {
				results.add(createExpanded(featureEntity, parameters));
			}
			return results;
		} finally {
			returnSession(session);
		}
	}

	@Override
	public PlatformOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
		Session session = getSession();
		try {
			FeatureDao featureDao = new FeatureDao(session);
			FeatureEntity result = featureDao.getInstance(parseId(id), parameters);
			if (result == null) {
				throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
			}
			return createExpanded(result, parameters);
		} finally {
			returnSession(session);
		}
	}

	private PlatformOutput createExpanded(FeatureEntity entity, DbQuery parameters) throws DataAccessException {
		PlatformOutput result = createCondensed(entity, parameters);
		addFeatures(result);
		return result;
	}

	private PlatformOutput createCondensed(FeatureEntity entity, DbQuery parameters) {
		PlatformOutput result = getConcretePlatformOutput(entity, parameters);
		result.setId(Long.toString(entity.getPkid()));
		result.setLabel(getLabelFrom(entity, parameters.getLocale()));
		return result;
	}
	
	private PlatformOutput getConcretePlatformOutput(FeatureEntity entity, DbQuery parameters) {
		if (entity.isSetGeometry() && entity.getGeom() instanceof Point) {
			return new StationaryPlatformOutput();
		}
		return new MobilePlatformOutput();
	}

	private void addFeatures(PlatformOutput result) {
		if (result instanceof StationaryPlatformOutput) {
			// add Site
		} else if (result instanceof MobilePlatformOutput) {
			// Add Tracks
		}
	}
	
}
