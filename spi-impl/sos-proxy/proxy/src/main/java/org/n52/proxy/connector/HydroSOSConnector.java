package org.n52.proxy.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.utils.ConnectorHelper;
import org.n52.proxy.connector.utils.DatasetConstellation;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.om.features.FeatureCollection;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosCapabilities;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.shetland.ogc.sos.SosObservationOffering;
import org.n52.shetland.ogc.sos.request.GetFeatureOfInterestRequest;
import org.n52.shetland.ogc.sos.response.GetFeatureOfInterestResponse;
import org.slf4j.LoggerFactory;

/**
 * @author Jan Schulte
 */
public class HydroSOSConnector extends AbstractSosConnector {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HydroSOSConnector.class);

    @Override
    protected boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        return false;
    }

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        ServiceConstellation serviceConstellation = new ServiceConstellation();
        try {
            config.setVersion(Sos2Constants.SERVICEVERSION);
            config.setConnector(getConnectorName());
            ConnectorHelper.addService(config, serviceConstellation);
            SosCapabilities sosCaps = (SosCapabilities) capabilities.getCapabilities();
            addDatasets(serviceConstellation, sosCaps, config.getUrl());
        } catch (UnsupportedOperationException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return serviceConstellation;
    }

    @Override
    public List<MeasurementDataEntity> getObservations(MeasurementDatasetEntity seriesEntity, DbQuery query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void addDatasets(ServiceConstellation serviceConstellation, SosCapabilities sosCaps, String url) {
        if (sosCaps != null) {
            sosCaps.getContents().get().forEach((obsOff) -> {
                addByOffering(obsOff, serviceConstellation, url);
            });
//            addByOffering(sosCaps.getContents().get().first(), serviceConstellation, url);
        }
    }

    private void addByOffering(SosObservationOffering obsOff, ServiceConstellation serviceConstellation, String url) {
        String offeringId = ConnectorHelper.addOffering(obsOff, serviceConstellation);

        obsOff.getProcedures().forEach((procedureId) -> {
            ConnectorHelper.addProcedure(procedureId, true, false, serviceConstellation);
            obsOff.getObservableProperties().forEach(phenomenonId -> {
                ConnectorHelper.addPhenomenon(phenomenonId, serviceConstellation);
                String categoryId = ConnectorHelper.addCategory(phenomenonId, serviceConstellation);

                GetFeatureOfInterestResponse foiResponse = getFeatureOfInterestResponse(procedureId, url);
                AbstractFeature abstractFeature = foiResponse.getAbstractFeature();
                if (abstractFeature instanceof FeatureCollection) {
                    FeatureCollection featureCollection = (FeatureCollection) abstractFeature;
                    featureCollection.getMembers().forEach((featureKey, feature) -> {
                        String featureId = ConnectorHelper.addFeature((SamplingFeature) feature, serviceConstellation);
                        serviceConstellation.add(new DatasetConstellation(procedureId, offeringId, categoryId, phenomenonId, featureId));
                    });
                }
            });
        });
    }

    private GetFeatureOfInterestResponse getFeatureOfInterestResponse(String procedureId, String url) {
        GetFeatureOfInterestRequest request = new GetFeatureOfInterestRequest(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        request.setProcedures(new ArrayList<>(Arrays.asList(procedureId)));
        return (GetFeatureOfInterestResponse) getSosRepsonseFor(request, Sos2Constants.NS_SOS_20, url);
    }

}
