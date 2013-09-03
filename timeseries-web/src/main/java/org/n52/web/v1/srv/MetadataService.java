package org.n52.web.v1.srv;

public interface MetadataService {
	
	int getServiceCount();

	int getStationsCount();

	int getTimeseriesCount();

	int getOfferingsCount();

	int getCategoriesCount();

	int getFeaturesCount();

	int getProceduresCount();

	int getPhenomenaCount();

}
