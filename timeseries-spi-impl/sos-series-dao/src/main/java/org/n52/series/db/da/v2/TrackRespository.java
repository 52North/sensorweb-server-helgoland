package org.n52.series.db.da.v2;

import java.util.Collection;
import java.util.List;

import org.n52.io.response.v2.TrackOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.OutputAssembler;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.ServiceInfo;

public class TrackRespository extends SessionAwareRepository implements OutputAssembler<TrackOutput> {

	private static final String PREFIX = "track_";

	protected TrackRespository(ServiceInfo serviceInfo) {
		super(serviceInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<TrackOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TrackOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrackOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SearchResult> searchFor(String queryString, String locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<SearchResult> convertToSearchResults(List<? extends DescribableEntity<? extends I18nEntity>> found,
			String locale) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean checkId(String featureId) {
		return featureId.startsWith(PREFIX);
	}

}
