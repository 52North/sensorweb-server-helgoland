package org.n52.proxy.connector.utils;

import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.shetland.ogc.gml.CodeType;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.gda.GetDataAvailabilityResponse.DataAvailability;

/**
 * @author Jan Schulte
 */
public class ConnectorHelper {

    public static void addService(DataSourceConfiguration config, ServiceConstellation serviceConstellation) {
        serviceConstellation.setService(EntityBuilder.createService(config.getItemName(), "here goes description", config.getConnector(), config.getUrl(), config.getVersion()));
    }

    public static String addOffering(SosObservationOffering offering, ServiceConstellation serviceConstellation) {
        String offeringId = offering.getIdentifier();
        CodeType name = offering.getFirstName();
        if (name != null) {
            serviceConstellation.putOffering(offeringId, name.getValue());
        } else {
            serviceConstellation.putOffering(offeringId, offeringId);
        }
        return offeringId;
    }

    public static String addProcedure(String procedureId, boolean insitu, boolean mobile, ServiceConstellation serviceConstellation) {
        serviceConstellation.putProcedure(procedureId, procedureId, insitu, mobile);
        return procedureId;
    }

    public static String addProcedure(DataAvailability dataAval, boolean insitu, boolean mobile, ServiceConstellation serviceConstellation) {
        String procedureId = dataAval.getProcedure().getHref();
        String procedureName = dataAval.getProcedure().getTitle();
        serviceConstellation.putProcedure(procedureId, procedureName, insitu, mobile);
        return procedureId;
    }

    public static String addPhenomenon(String phenomenonId, ServiceConstellation serviceConstellation) {
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonId);
        return phenomenonId;
    }

    public static String addPhenomenon(DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String phenomenonId = dataAval.getObservedProperty().getHref();
        String phenomenonName = dataAval.getObservedProperty().getTitle();
        serviceConstellation.putPhenomenon(phenomenonId, phenomenonName);
        return phenomenonId;
    }

    public static String addCategory(String categoryId, ServiceConstellation serviceConstellation) {
        serviceConstellation.putCategory(categoryId, categoryId);
        return categoryId;
    }

    public static String addCategory(DataAvailability dataAval, ServiceConstellation serviceConstellation) {
        String categoryId = dataAval.getObservedProperty().getHref();
        String categoryName = dataAval.getObservedProperty().getTitle();
        serviceConstellation.putCategory(categoryId, categoryName);
        return categoryId;
    }

    public static String addFeature(SamplingFeature abstractFeature, ServiceConstellation serviceConstellation) {
        String featureId = abstractFeature.getIdentifier();
        String featureName;
        if (abstractFeature.getName().size() == 1 && abstractFeature.getName().get(0).getValue() != null) {
            featureName = abstractFeature.getName().get(0).getValue();
        } else {
            featureName = featureId;
        }
        double lat = abstractFeature.getGeometry().getCoordinate().x;
        double lng = abstractFeature.getGeometry().getCoordinate().y;
        int srid = abstractFeature.getGeometry().getSRID();
        serviceConstellation.putFeature(featureId, featureName, lat, lng, srid);
        return featureId;
    }

}
