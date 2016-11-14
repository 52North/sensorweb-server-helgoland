/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.series.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.n52.io.crs.CRSUtils;
import org.n52.io.request.IoParameters;
import org.n52.io.response.CategoryOutput;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.OfferingOutput;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.PhenomenonOutput;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.ServiceOutput;
import org.n52.io.response.dataset.SeriesParameters;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.dao.ProxyDbQuery;
import org.n52.web.ctrl.UrlHelper;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jansch
 */
public class ProxySessionAwareRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxySessionAwareRepository.class);

    private final CRSUtils crsUtils = CRSUtils.createEpsgStrictAxisOrder();

    private String databaseSrid;

    @Autowired
    private HibernateSessionStore sessionStore;

    @Autowired
    private ServiceEntity serviceInfo;

    protected UrlHelper urHelper = new UrlHelper();

    protected ServiceOutput createCondensedService(ServiceEntity entity) {
        ServiceOutput result = new ServiceOutput();
        result.setId(Long.toString(entity.getPkid()));
//        result.setLabel(entity.getServiceId());
        return result;
    }

    protected ProxyDbQuery getDbQuery(IoParameters parameters) {
        return ProxyDbQuery.createFrom(parameters);
    }

    public HibernateSessionStore getSessionStore() {
        return sessionStore;
    }

    public void setSessionStore(HibernateSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public void setServiceInfo(ServiceEntity serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public ServiceEntity getServiceInfo() {
        return serviceInfo;
    }

    protected CRSUtils getCrsUtils() {
        return crsUtils;
    }

    protected String getDatabaseSrid() {
        return databaseSrid;
    }

    protected Long parseId(String id) throws BadRequestException {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            LOGGER.debug("Unable to parse {} to Long.", e);
            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
        }
    }

    public void returnSession(Session session) {
        sessionStore.returnSession(session);
    }

    public Session getSession() {
        try {
            return sessionStore.getSession();
        } catch (Throwable e) {
            throw new IllegalStateException("Could not get hibernate session.", e);
        }
    }

    public void setDatabaseSrid(String databaseSrid) {
        this.databaseSrid = databaseSrid;
    }

    protected Map<String, SeriesParameters> createTimeseriesList(List<MeasurementDatasetEntity> series, ProxyDbQuery parameters) throws DataAccessException {
        Map<String, SeriesParameters> timeseriesOutputs = new HashMap<>();
        for (MeasurementDatasetEntity timeseries : series) {
            if (!timeseries.getProcedure().isReference()) {
                String timeseriesId = timeseries.getPkid().toString();
                timeseriesOutputs.put(timeseriesId, createTimeseriesOutput(timeseries, parameters));
            }
        }
        return timeseriesOutputs;
    }

    protected SeriesParameters createTimeseriesOutput(MeasurementDatasetEntity timeseries, ProxyDbQuery parameters) throws DataAccessException {
        SeriesParameters timeseriesOutput = new SeriesParameters();
        timeseriesOutput.setService(createCondensedService(timeseries.getService()));
        timeseriesOutput.setOffering(getCondensedOffering(timeseries.getProcedure(), parameters));
        timeseriesOutput.setProcedure(getCondensedProcedure(timeseries.getProcedure(), parameters));
        timeseriesOutput.setPhenomenon(getCondensedPhenomenon(timeseries.getPhenomenon(), parameters));
        timeseriesOutput.setFeature(getCondensedFeature(timeseries.getFeature(), parameters));
        timeseriesOutput.setCategory(getCondensedCategory(timeseries.getCategory(), parameters));
        return timeseriesOutput;
    }

    protected SeriesParameters createSeriesParameters(DatasetEntity series, ProxyDbQuery parameters) throws DataAccessException {
        SeriesParameters seriesParameter = new SeriesParameters();
        seriesParameter.setService(createCondensedExtendedService(series.getService(), parameters));
        seriesParameter.setOffering(getCondensedExtendedOffering(series.getProcedure(), parameters));
        seriesParameter.setProcedure(getCondensedExtendedProcedure(series.getProcedure(), parameters));
        seriesParameter.setPhenomenon(getCondensedExtendedPhenomenon(series.getPhenomenon(), parameters));
        seriesParameter.setFeature(getCondensedExtendedFeature(series.getFeature(), parameters));
        seriesParameter.setCategory(getCondensedExtendedCategory(series.getCategory(), parameters));
        seriesParameter.setPlatform(getCondensedPlatform(series.getPlatform(), parameters));
        return seriesParameter;
    }

    private ServiceOutput createCondensedExtendedService(ServiceEntity entity, ProxyDbQuery parameters) throws DataAccessException {
        ServiceOutput serviceOutput = createCondensedService(entity);
        serviceOutput.setHref(urHelper.getServicesHrefBaseUrl(parameters.getHrefBase()) + "/" + serviceOutput.getId());
        return serviceOutput;
    }

    protected ParameterOutput getCondensedPhenomenon(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new PhenomenonOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedExtendedPhenomenon(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new PhenomenonOutput(), entity, parameters, urHelper.getPhenomenaHrefBaseUrl(parameters.getHrefBase()));
    }

    protected ParameterOutput getCondensedOffering(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new OfferingOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedExtendedOffering(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new OfferingOutput(), entity, parameters, urHelper.getOfferingsHrefBaseUrl(parameters.getHrefBase()));
    }

    private ParameterOutput createCondensed(ParameterOutput outputvalue, DescribableEntity entity, ProxyDbQuery parameters) {
        outputvalue.setLabel(entity.getLabelFrom(parameters.getLocale()));
        outputvalue.setId(Long.toString(entity.getPkid()));
        return outputvalue;
    }

    private ParameterOutput createCondensed(ParameterOutput outputvalue, DescribableEntity entity, ProxyDbQuery parameters, String hrefBase) {
        createCondensed(outputvalue, entity, parameters);
        outputvalue.setHref(hrefBase + "/" + outputvalue.getId());
        return outputvalue;
    }

    protected ParameterOutput getCondensedProcedure(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new ProcedureOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedExtendedProcedure(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new ProcedureOutput(), entity, parameters, urHelper.getProceduresHrefBaseUrl(parameters.getHrefBase()));
    }

    protected ParameterOutput getCondensedFeature(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new FeatureOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedExtendedFeature(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new FeatureOutput(), entity, parameters, urHelper.getFeaturesHrefBaseUrl(parameters.getHrefBase()));
    }

    protected ParameterOutput getCondensedCategory(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new CategoryOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedExtendedCategory(DescribableEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new CategoryOutput(), entity, parameters, urHelper.getCategoriesHrefBaseUrl(parameters.getHrefBase()));
    }

    protected ParameterOutput getCondensedPlatform(PlatformEntity entity, ProxyDbQuery parameters) {
        return createCondensed(new PlatformOutput(entity.getPlatformType()), entity, parameters, urHelper.getPlatformsHrefBaseUrl(parameters.getHrefBase()));
    }

}
