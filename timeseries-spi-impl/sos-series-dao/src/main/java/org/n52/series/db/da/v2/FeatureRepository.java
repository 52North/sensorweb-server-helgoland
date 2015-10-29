package org.n52.series.db.da.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.n52.io.response.v2.FeatureOutput;
import org.n52.io.response.v2.SiteOutput;
import org.n52.io.response.v2.TrackOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.sensorweb.spi.search.FeatureSearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.OutputAssembler;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.web.exception.ResourceNotFoundException;

public class FeatureRepository extends SessionAwareRepository implements OutputAssembler<FeatureOutput>  {

	public FeatureRepository(ServiceInfo serviceInfo) {
		super(serviceInfo);
	}

	@Override
	public Collection<SearchResult> searchFor(String queryString, String locale) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		Collection<SearchResult> sites = createSiteRepository().searchFor(queryString, locale);
		if (sites != null) {
			results.addAll(sites);
		}
		Collection<SearchResult> tracks = createTrackRespository().searchFor(queryString, locale);
		if (tracks != null) {
			results.addAll(tracks);
		}
		return results;
	}

	@Override
	protected List<SearchResult> convertToSearchResults(List<? extends DescribableEntity<? extends I18nEntity>> found,
			String locale) {
		List<SearchResult> results = new ArrayList<SearchResult>();
        for (DescribableEntity< ? extends I18nEntity> searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = getLabelFrom(searchResult, locale);
            results.add(new FeatureSearchResult(pkid, label));
        }
        return results;
	}

	@Override
	public List<FeatureOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
		List<FeatureOutput> results = new ArrayList<FeatureOutput>();
		List<SiteOutput> sites = createSiteRepository().getAllCondensed(parameters);
		if (sites != null) {
			results.addAll(sites);
		}
		List<TrackOutput> tracks = createTrackRespository().getAllCondensed(parameters);
		if (tracks != null) {
			results.addAll(tracks);
		}
		return results;
	}

	@Override
	public List<FeatureOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
		List<FeatureOutput> results = new ArrayList<FeatureOutput>();
		List<SiteOutput> sites = createSiteRepository().getAllExpanded(parameters);
		if (sites != null) {
			results.addAll(sites);
		}
		List<TrackOutput> tracks = createTrackRespository().getAllExpanded(parameters);
		if (tracks != null) {
			results.addAll(tracks);
		}
		return results;
	}

	@Override
	public FeatureOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
		FeatureOutput result = null;
		if (createSiteRepository().checkId(id)) {
			result = createSiteRepository().getInstance(id, parameters);
		} else if (createTrackRespository().checkId(id)) {
			result = createTrackRespository().getInstance(id, parameters);
		}
		if (result == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
		return result;
	}
	
	private SiteRepository createSiteRepository() {
		return new SiteRepository(getServiceInfo());
	}
	
	private TrackRespository createTrackRespository() {
		return new TrackRespository(getServiceInfo());
	}

}
