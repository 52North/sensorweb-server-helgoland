package org.n52.io.request;

import java.util.Set;

public class FilterResolver {

	private final IoParameters parameters;

	public FilterResolver(IoParameters parameters) {
		this.parameters = parameters;
	}

    public boolean shallIncludeMobilePlatformTypes() {
        return shallIncludeAllPlatformTypes() || isSetMobileFilter() || !isSetStationaryFilter();
    }

    public boolean shallIncludeStationaryPlatformTypes() {
        return shallIncludeAllPlatformTypes() || isSetStationaryFilter() || !isSetMobileFilter();
    }

    public boolean shallIncludeInsituPlatformTypes() {
        return shallIncludeAllPlatformTypes() || isSetInsituFilter() || !isSetRemoteFilter();
    }

    public boolean shallIncludeRemotePlatformTypes() {
        return shallIncludeAllPlatformTypes() || isSetRemoteFilter() || !isSetInsituFilter();
    }

    public boolean isSetStationaryFilter() {
        return getPlatformTypes().contains("stationary") || getPlatformTypes().contains("all");
    }

	public boolean isSetMobileFilter() {
        return getPlatformTypes().contains("mobile") || getPlatformTypes().contains("all");
    }

    public boolean isSetInsituFilter() {
        return getPlatformTypes().contains("insitu") || getPlatformTypes().contains("all");
    }

    public boolean isSetRemoteFilter() {
        return getPlatformTypes().contains("remote") || getPlatformTypes().contains("all");
    }

    public boolean shallIncludeAllPlatformTypes() {
        return !isSetPlatformTypeFilter() || getPlatformTypes().contains("all");
    }

	private boolean isSetPlatformTypeFilter() {
		return !getPlatformTypes().isEmpty();
	}

    private boolean shallIncludeAllPlatformGeometries() {
        return !isSetPlatformGeometriesFilter() || getPlatformGeometryTypes().contains("all");
    }

	private boolean isSetPlatformGeometriesFilter() {
		return !getPlatformGeometryTypes().isEmpty();
	}

    private boolean shallIncludeAllObservedGeometries() {
        return !isSetObservedGeometriesFilter() || getObservedGeometryTypes().contains("all");
    }

	private boolean isSetObservedGeometriesFilter() {
		return !getObservedGeometryTypes().isEmpty();
	}

    public boolean shallIncludePlatformGeometriesSite() {
        return shallIncludeAllPlatformGeometries()
        		&& shallIncludeAllPlatformTypes()
        		&& !isSetObservedGeometriesFilter()
        		&& !isSetMobileFilter()
        		&& !isSetRemoteFilter()
        		|| isSetStationaryFilter()
        		|| isSetInsituFilter()
        		|| isSetSiteFilter();
    }

	private boolean isSetSiteFilter() {
		return getPlatformGeometryTypes().contains("site");
	}

	public boolean shallIncludePlatformGeometriesTrack() {
        return shallIncludeAllPlatformGeometries()
        		&& shallIncludeAllPlatformTypes()
        		&& !isSetObservedGeometriesFilter()
        		&& !isSetStationaryFilter()
        		&& !isSetRemoteFilter()
        		|| isSetMobileFilter()
        		|| isSetInsituFilter()
        		|| isSetTrackFilter();
    }

	private boolean isSetTrackFilter() {
		return getPlatformGeometryTypes().contains("track");
	}

    public boolean shallIncludeObservedGeometriesStatic() {
        return shallIncludeAllObservedGeometries()
        		&& shallIncludeAllPlatformTypes()
        		|| isSetStaticFilter()
        		|| !isSetDynamicFilter()
        		&& !isSetInsituFilter();
    }

	private boolean isSetStaticFilter() {
		return getObservedGeometryTypes().contains("static") || getObservedGeometryTypes().contains("all");
	}

    public boolean shallIncludeObservedGeometriesDynamic() {
        return shallIncludeAllObservedGeometries()
        		&& shallIncludeAllPlatformTypes()
        		|| isSetDynamicFilter()
        		|| !isSetStaticFilter()
        		&& !isSetInsituFilter();
    }

	private boolean isSetDynamicFilter() {
		return getObservedGeometryTypes().contains("dynamic") || getObservedGeometryTypes().contains("all");
	}

	private Set<String> getPlatformTypes() {
		return parameters.getPlatformTypes();
	}

    private Set<String> getObservedGeometryTypes() {
		return parameters.getObservedGeometryTypes();
	}

    private Set<String> getPlatformGeometryTypes() {
    	return parameters.getPlatformGeometryTypes();
    }

}
