/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.series.ckan.sos;

import com.google.common.collect.Table;
import com.vividsolutions.jts.geom.Geometry;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanOrganization;
import eu.trentorise.opendata.jackan.model.CkanTag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceField;
import org.n52.series.ckan.beans.ResourceMember;
import org.n52.series.ckan.beans.SchemaDescriptor;
import org.n52.series.ckan.da.CkanConstants;
import org.n52.series.ckan.table.DataTable;
import org.n52.series.ckan.table.ResourceKey;
import org.n52.series.ckan.table.ResourceTable;
import org.n52.series.ckan.util.GeometryBuilder;
import org.n52.sos.ds.hibernate.InsertObservationDAO;
import org.n52.sos.ds.hibernate.InsertSensorDAO;
import org.n52.sos.exception.ows.concrete.InvalidSridException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.AbstractFeature;
import org.n52.sos.ogc.om.AbstractPhenomenon;
import org.n52.sos.ogc.om.OmConstants;
import org.n52.sos.ogc.om.OmObservableProperty;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.om.OmObservationConstellation;
import org.n52.sos.ogc.om.SingleObservationValue;
import org.n52.sos.ogc.om.features.SfConstants;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.sensorML.SensorML20Constants;
import org.n52.sos.ogc.sensorML.SmlContact;
import org.n52.sos.ogc.sensorML.SmlContactList;
import org.n52.sos.ogc.sensorML.SmlResponsibleParty;
import org.n52.sos.ogc.sensorML.elements.SmlCapabilities;
import org.n52.sos.ogc.sensorML.elements.SmlClassifier;
import org.n52.sos.ogc.sensorML.elements.SmlIdentifier;
import org.n52.sos.ogc.sensorML.elements.SmlIo;
import org.n52.sos.ogc.sos.SosOffering;
import org.n52.sos.ogc.swe.SweField;
import org.n52.sos.ogc.swe.SweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SweQuantity;
import org.n52.sos.ogc.swe.simpleType.SweText;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.SosInsertionMetadata;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultSosInsertionStrategy implements SosInsertionStrategy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSosInsertionStrategy.class);

    private final InsertSensorDAO insertSensorDao;
    
    private final InsertObservationDAO insertObservationDao;
    
    DefaultSosInsertionStrategy(InsertSensorDAO insertSensorDao, InsertObservationDAO insertObservationDao) {
        this.insertSensorDao = insertSensorDao;
        this.insertObservationDao = insertObservationDao;
    }

    @Override
    public void insertOrUpdate(CkanDataset dataset, CsvObservationsCollection csvObservationsCollection) {
        Map<ResourceMember, DataFile> platformDataCollections = csvObservationsCollection.getPlatformDataCollections();
        SchemaDescriptor schemaDescription = csvObservationsCollection.getSchemaDescriptor().getSchemaDescription();
        for (Map.Entry<ResourceMember, DataFile> platformEntry : platformDataCollections.entrySet()) {
            ResourceTable platformTable = new ResourceTable(platformEntry.getKey(), platformEntry.getValue());
            platformTable.readIntoMemory();
            insertData(platformTable, schemaDescription, csvObservationsCollection);
        }
    }
    
    private static class SensorInsertion {
        private final InsertSensorRequest request;
        private final AbstractFeature feature;
        private final List<OmObservation> observations;
        SensorInsertion(InsertSensorRequest request, AbstractFeature feature) {
            this.request = request;
            this.feature = feature;
            this.observations = new ArrayList<>();
        }
    }
    
    void insertData(ResourceTable platformTable, SchemaDescriptor schemaDescription, CsvObservationsCollection csvObservationsCollection) {
        final List<Phenomenon> phenomena = parseObservableProperties(platformTable, schemaDescription);
        Map<ResourceMember, DataFile> observationCollections = csvObservationsCollection.getObservationDataCollections();
        
        for (Map.Entry<ResourceMember, DataFile> observationEntry : observationCollections.entrySet()) {
            ResourceTable observationTable = new ResourceTable(observationEntry.getKey(), observationEntry.getValue());
            observationTable.readIntoMemory();
            
            DataTable joinedTable = platformTable.innerJoin(observationTable);
            Map<String, SensorInsertion> sensorInsertions = new HashMap<>();
            try {
                for (Map.Entry<ResourceKey, Map<ResourceField, String>> rowEntry : joinedTable.getTable().rowMap().entrySet()) {
                    AbstractFeature feature = createFeatureRelation(rowEntry.getValue());
                    for (Phenomenon phenomenon : phenomena) {
                        String procedureId = createProcedureId(feature, phenomenon);
                        if ( !sensorInsertions.containsKey(procedureId)) {
                            LOGGER.debug("inserting procedure '{}' with phenomenon '{}' (unit '{}')", 
                                procedureId, phenomenon.getLabel(), phenomenon.getUom());
                            InsertSensorRequest insertSensorRequest = prepareSmlInsertSensorRequest(feature, phenomenon, schemaDescription);
                            insertSensorRequest.setObservableProperty(phenomenaToIdList(phenomena));
                            insertSensorRequest.setProcedureDescriptionFormat("http://www.opengis.net/sensorML/1.0.1");
                            insertSensorRequest.setMetadata(createInsertSensorMetadata());
                            insertSensorDao.insertSensor(insertSensorRequest);
                            sensorInsertions.put(procedureId, new SensorInsertion(insertSensorRequest, feature));
                        } else {
                            SensorInsertion sensorInsertion = sensorInsertions.get(procedureId);
                            InsertSensorRequest insertSensorRequest = sensorInsertion.request;
                            List<String> offerings = offeringsToIdList(insertSensorRequest.getAssignedOfferings());
                            OmObservationConstellation constellation = new OmObservationConstellation();
                            constellation.setObservableProperty(createPhenomenon(phenomenon));
                            constellation.setFeatureOfInterest(sensorInsertion.feature);
                            constellation.setOfferings(offerings);
                            constellation.setObservationType(OmConstants.OBS_TYPE_MEASUREMENT);
                            constellation.setProcedure(insertSensorRequest.getProcedureDescription());

                            sensorInsertion.observations.add(createObservation(rowEntry, constellation, phenomenon));
                        }
                    }
                }
                
                for (Map.Entry<String, SensorInsertion> sensorEntries : sensorInsertions.entrySet()) {
                    if (sensorEntries.getValue().observations.size() > 0) {
                        LOGGER.debug("insert observations for sensor '{}'", sensorEntries.getKey());
                        InsertObservationRequest insertObservationRequest = new InsertObservationRequest();
                        insertObservationRequest.setObservation(sensorEntries.getValue().observations);
                        final List<SosOffering> assignedOfferings = sensorEntries.getValue().request.getAssignedOfferings();
                        insertObservationRequest.setOfferings(offeringsToIdList(assignedOfferings));
                        insertObservationDao.insertObservation(insertObservationRequest);
                        LOGGER.debug("Insertion of observations completed.");
                    } else {
                        LOGGER.debug("No observations to insert.");
                    }
                
                }
            } catch (Exception e) {
                LOGGER.error("Could not insert or update procedure/observation data.", e);
            } 
        }
    }
    
    private List<String> phenomenaToIdList(List<Phenomenon> phenomena) {
        List<String> ids = new ArrayList<>();
        for (Phenomenon phenomenon : phenomena) {
            ids.add(phenomenon.getId());
        }
        return ids;
    }
    
    private List<String> offeringsToIdList(List<SosOffering> offerings) {
        List<String> ids = new ArrayList<>();
        for (SosOffering offering : offerings) {
            ids.add(offering.getIdentifier());
        }
        return ids;
    }

    AbstractFeature createFeatureRelation(Map<ResourceField, String> platform) {
        final GeometryBuilder pointBuilder = GeometryBuilder.create();
        final SamplingFeature feature = new SamplingFeature(null);
        for (Map.Entry<ResourceField,String> fieldEntry : platform.entrySet()) {
            ResourceField field = fieldEntry.getKey();
            if (field.isField(CkanConstants.KnownFieldId.CRS)) {
                pointBuilder.withCrs(fieldEntry.getValue());
            }
            if (field.isField(CkanConstants.KnownFieldId.LATITUDE)) {
                pointBuilder.setLatitude(fieldEntry.getValue());
            }
            if (field.isField(CkanConstants.KnownFieldId.LONGITUDE)) {
                pointBuilder.setLongitude(fieldEntry.getValue());
            }
            if (field.isField(CkanConstants.KnownFieldId.ALTITUDE)) {
                pointBuilder.setAltitude(fieldEntry.getValue());
            }
            if (field.isField(CkanConstants.KnownFieldId.STATION_ID)) {
                feature.setIdentifier(fieldEntry.getValue());
            }
            if (field.isField(CkanConstants.KnownFieldId.STATION_NAME)) {
                feature.addName(fieldEntry.getValue());
            }   
        }
        setFeatureGeometry(feature, pointBuilder.getPoint());
        feature.setFeatureType(SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT);
//        return new SwesFeatureRelationship("samplingPoint", feature);
        return feature;
    }
    
    void setFeatureGeometry(SamplingFeature feature, Geometry point) {
        try {
            feature.setGeometry(point);
        } catch (InvalidSridException e) {
            LOGGER.error("could not set feature's geometry.", e);
        }
    }
    
    List<Phenomenon> parseObservableProperties(ResourceTable platformTable, SchemaDescriptor schemaDescription) {
        ResourceMember resourceMember = platformTable.getResourceMember();
        List<ResourceMember> members = schemaDescription.getMembers();
        Set<Phenomenon> observableProperties = new HashSet<>();
        for (ResourceMember member : members) {
            Set<ResourceField> joinableFields = resourceMember.getJoinableFields(member);
            if ( !joinableFields.isEmpty()) {
                for (ResourceField joinableField : joinableFields) {
                    if (joinableField.hasProperty(CkanConstants.KnownFieldProperty.PHENOMENON)) {
                        observableProperties.add(new Phenomenon(
                                joinableField.getFieldId(),
                                joinableField.getLongName(),
                                joinableField.getIndex(),
                                joinableField.getOther(CkanConstants.KnownFieldProperty.UOM)
                        ));
                    }
                }
            }
        }
        return new ArrayList<>(observableProperties);
    }


    private InsertSensorRequest prepareSmlInsertSensorRequest(AbstractFeature feature, Phenomenon phenomenon, SchemaDescriptor schemaDescription) {
        final InsertSensorRequest insertSensorRequest = new InsertSensorRequest();
        final org.n52.sos.ogc.sensorML.System system = new org.n52.sos.ogc.sensorML.System();
        system.setDescription(schemaDescription.getDataset().getNotes());
        
        final String procedureId = createProcedureId(feature, phenomenon);
        final SosOffering sosOffering = new SosOffering(procedureId);
        system
                .setInputs(Collections.<SmlIo<?>>singletonList(createInput(phenomenon)))
                .setOutputs(Collections.<SmlIo<?>>singletonList(createOutput(phenomenon)))
                .setKeywords(createKeywordList(feature, phenomenon, schemaDescription))
                .setIdentifications(createIdentificationList(feature, phenomenon))
                .setClassifications(createClassificationList(feature, phenomenon))
                .addCapabilities(createCapabilities(feature, phenomenon, sosOffering))
//                .addContact(createContact(schemaDescription.getDataset())) // TODO
                // ... // TODO
                .setValidTime(createValidTimePeriod())
                .setIdentifier(procedureId)
                ;
        system.setSensorDescriptionXmlString("");
        SensorML sml = new SensorML();
        sml.addMember(system);
        
        insertSensorRequest.setAssignedOfferings(Collections.singletonList(sosOffering));
        insertSensorRequest.setAssignedProcedureIdentifier(procedureId);
        insertSensorRequest.setProcedureDescription(sml);
        return insertSensorRequest;
    }
    
    private SmlIo<?> createInput(Phenomenon phenomeon) {
        return new SmlIo<>(new SweObservableProperty()
                .setDefinition(phenomeon.getId()))
                .setIoName(phenomeon.getId());
    }
    
    private SmlIo<?> createOutput(Phenomenon phenomeon) {
        return new SmlIo<>(new SweQuantity()
                .setUom(phenomeon.getUom())
                .setDefinition(phenomeon.getId()))
                .setIoName(phenomeon.getId());
    }

    private List<String> createKeywordList(AbstractFeature feature, Phenomenon phenomenon, SchemaDescriptor schemaDescription) {
        List<String> keywords = new ArrayList<>();
        keywords.add("CKAN data");
        keywords.add(feature.getFirstName().getValue());
        keywords.add(phenomenon.getLabel());
        keywords.add(phenomenon.getId());
        addDatasetTags(schemaDescription.getDataset(), keywords);
        return keywords;
    }

    private void addDatasetTags(CkanDataset dataset, List<String> keywords) {
        for (CkanTag tag : dataset.getTags()) {
            keywords.add(tag.getDisplayName());
        }
    }
    
    private List<SmlIdentifier> createIdentificationList(AbstractFeature feature, Phenomenon phenomenon) {
        List<SmlIdentifier> idents = new ArrayList<>();
        idents.add(new SmlIdentifier(
                OGCConstants.UNIQUE_ID, 
                OGCConstants.URN_UNIQUE_IDENTIFIER,
                // TODO check feautre id vs name
                createProcedureId(feature, phenomenon)));
        idents.add(new SmlIdentifier(
                "longName",
                "urn:ogc:def:identifier:OGC:1.0:longName",
                createProcedureLongName(feature, phenomenon)));
        return idents;
    }

    private String createProcedureId(AbstractFeature feature, Phenomenon phenomenon) {
        return phenomenon.getLabel() + "_" + feature.getFirstName().getValue() + "_" + feature.getIdentifier();
    }

    private String createProcedureLongName(AbstractFeature feature, Phenomenon phenomenon) {
        return phenomenon.getLabel() + "@" + feature.getFirstName().getValue();
    }

    private List<SmlClassifier> createClassificationList(AbstractFeature feature, Phenomenon phenomenon) {
        return Collections.singletonList(new SmlClassifier(
                "phenomenon", 
                "urn:ogc:def:classifier:OGC:1.0:phenomenon", 
                null, 
                phenomenon.getId()));
    }
   
    private TimePeriod createValidTimePeriod() {
        return new TimePeriod(new Date(), null);
    }

    private List<SmlCapabilities> createCapabilities(AbstractFeature feature, Phenomenon phenomenon, SosOffering offering) {
        List<SmlCapabilities> capabilities = new ArrayList<>();
        capabilities.add(createFeatureCapabilities(feature));
        capabilities.add(createOfferingCapabilities(feature, phenomenon, offering));
        capabilities.add(createBboxCapabilities(feature));
        return capabilities;
    }
    
    private SmlCapabilities createFeatureCapabilities(AbstractFeature feature) {
        SmlCapabilities featuresCapabilities = new SmlCapabilities("featuresOfInterest");
        final SweSimpleDataRecord record = new SweSimpleDataRecord()
                .addField(createTextField(
                        SensorML20Constants.FEATURE_OF_INTEREST_FIELD_NAME, 
                        SensorML20Constants.FEATURE_OF_INTEREST_FIELD_DEFINITION, 
                        feature.getIdentifier()));
        return featuresCapabilities.addAbstractDataComponents(record);
    }

    private SmlCapabilities createOfferingCapabilities(AbstractFeature feature, Phenomenon phenomenon, SosOffering offering) {
        SmlCapabilities offeringCapabilities = new SmlCapabilities("offerings");
        offering.setIdentifier("Offering_" + createProcedureId(feature, phenomenon));
        final SweSimpleDataRecord record = new SweSimpleDataRecord()
                .addField(createTextField(
                        "field_0",
                        SensorML20Constants.OFFERING_FIELD_DEFINITION, 
                        offering.getIdentifier()));
        return offeringCapabilities.addAbstractDataComponents(record);
    }

    private SweField createTextField(String name, String definition, String value) {
        return new SweField(name, new SweText().setValue(value).setDefinition(definition));
    }

    private SmlCapabilities createBboxCapabilities(AbstractFeature feature) {
        SmlCapabilities offeringCapabilities = new SmlCapabilities("observedBBOX");
        
        // TODO
        
        return offeringCapabilities;
    }

    private SmlContact createContact(CkanDataset dataset) {
        CkanOrganization organisation = dataset.getOrganization();
        SmlContactList contactList = new SmlContactList();
        final SmlResponsibleParty responsibleParty = new SmlResponsibleParty();
        responsibleParty.setOrganizationName(organisation.getTitle());
        
        // TODO 
        
        contactList.addMember(responsibleParty);
        return contactList;
    }
    
    private SosInsertionMetadata createInsertSensorMetadata() {
        SosInsertionMetadata metadata = new SosInsertionMetadata();
        metadata.setFeatureOfInterestTypes(Collections.singleton(SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_FEATURE));
        metadata.setObservationTypes(Collections.singleton(OmConstants.OBS_TYPE_MEASUREMENT));
        return metadata;
    }
    
    private OmObservation createObservation(Map.Entry<ResourceKey, Map<ResourceField, String>> observationEntry, OmObservationConstellation constellation, Phenomenon phenomenon) {
        SingleObservationValue<?> value = null;
        TimeInstant timeInstant = null;

        OmObservation omObservation = new OmObservation();
        omObservation.setObservationConstellation(constellation);
        omObservation.setDefaultElementEncoding(CkanConstants.DEFAULT_CHARSET.toString());
        for (Map.Entry<ResourceField, String> cells : observationEntry.getValue().entrySet()) {
            ResourceField field = cells.getKey();
            String resourceType = field.getQualifier().getResourceType();
            if (resourceType.equalsIgnoreCase(CkanConstants.ResourceType.OBSERVATIONS)) {
//                    if (field.hasProperty(CkanConstants.KnownFieldProperty.PHENOMENON)) {
                if (field.getIndex() == phenomenon.getFieldIdx()) {
                    // TODO check index vs fieldId comparison
                    String phenomenonField = field.getFieldId();
                    String phenomenonId = constellation.getObservableProperty().getIdentifier();
                    if (phenomenonField.equalsIgnoreCase(phenomenonId)) {
                        // TODO value null in case of NO_DATA
                        value = createQuantityObservationValue(field, cells.getValue());
                        omObservation.setIdentifier(observationEntry.getKey().getKeyId() + "_" + phenomenonId);
                    }
                } else if (field.isField(CkanConstants.KnownFieldId.RESULT_TIME)) {
                    timeInstant = parsePhenomenonTime(field, cells);
                }
            } 
        }
        // TODO remove value == null if this works out for NO_DATA
        if (value == null || timeInstant == null) {
            LOGGER.debug("ignore observation having no value/phenomenonTime.");
            return null;
        } else {
            value.setPhenomenonTime(timeInstant);
            omObservation.setValue(value);
            return omObservation;
        }
    }

    private List<OmObservation> createObservations(DataTable observationTable, OmObservationConstellation constellation, Phenomenon phenomenon) {
        List<OmObservation> observations = new ArrayList<>();
        Table<ResourceKey, ResourceField, String> table = observationTable.getTable();
        long start = System.currentTimeMillis();
        for (ResourceKey key : table.rowKeySet()) {
            SingleObservationValue<?> value = null;
            TimeInstant timeInstant = null;
            
            OmObservation omObservation = new OmObservation();
            omObservation.setObservationConstellation(constellation);
            omObservation.setDefaultElementEncoding(CkanConstants.DEFAULT_CHARSET.toString());
            for (Map.Entry<ResourceField, String> cells : table.row(key).entrySet()) {
                ResourceField field = cells.getKey();
                String resourceType = field.getQualifier().getResourceType();
                if (resourceType.equalsIgnoreCase(CkanConstants.ResourceType.OBSERVATIONS)) {
//                    if (field.hasProperty(CkanConstants.KnownFieldProperty.PHENOMENON)) {
                    if (field.getIndex() == phenomenon.getFieldIdx()) {
                        // TODO check index vs fieldId comparison
                        String phenomenonField = field.getFieldId();
                        String phenomenonId = constellation.getObservableProperty().getIdentifier();
                        if (phenomenonField.equalsIgnoreCase(phenomenonId)) {
                            // TODO value null in case of NO_DATA
                            value = createQuantityObservationValue(field, cells.getValue());
                            omObservation.setIdentifier(key.getKeyId() + "_" + phenomenonId);
                        }
                    } else if (field.isField(CkanConstants.KnownFieldId.RESULT_TIME)) {
                        timeInstant = parsePhenomenonTime(field, cells);
                    }
                } 
            }
            // TODO remove value == null if this works out for NO_DATA
            if (value == null || timeInstant == null) {
                LOGGER.debug("ignore observation having no value/phenomenonTime.");
            } else {
                value.setPhenomenonTime(timeInstant);
                omObservation.setValue(value);
                observations.add(omObservation);
            }
        }
        LOGGER.debug("assembled #{} observations in {}s for {}", observations.size(),
                (System.currentTimeMillis() - start)/1000d, constellation);
        return observations;
    }

    private TimeInstant parsePhenomenonTime(ResourceField field, Map.Entry<ResourceField, String> cells) {
        String dateFormat = parseDateFormat(field);
        return parseDateValue(cells, dateFormat);
    }

    private String parseDateFormat(ResourceField field) {
        String format = field.getOther(CkanConstants.KnownFieldProperty.DATE_FORMAT);
        return format.replace("DD", "dd").replace("hh", "HH"); // XXX hack to fix wrong format
    }

    private TimeInstant parseDateValue(Map.Entry<ResourceField, String> cells, String dateFormat) {
        final String dateValue = cells.getValue();
        final TimeInstant timeInstant = new TimeInstant();
        try {
            DateTime dateTime = DateTime.parse(dateValue, DateTimeFormat.forPattern(dateFormat));
            timeInstant.setValue(dateTime);
        } catch (Exception ex) {
            LOGGER.error("Cannot parse date string {} with format {}", dateValue, dateFormat, ex);
            return null;
        }
        return timeInstant;
    }

    private SingleObservationValue<Double> createQuantityObservationValue(ResourceField field, String value) {
        try {
            SingleObservationValue<Double> obsValue = new SingleObservationValue<>();
            if (field.isOfType(Integer.class)
                    || field.isOfType(Float.class)
                    || field.isOfType(Double.class)) {
                QuantityValue quantityValue = new QuantityValue(Double.parseDouble(value));
                quantityValue.setUnit(field.getOther(CkanConstants.KnownFieldProperty.UOM));
                obsValue.setValue(quantityValue);
                return obsValue;
            }
        } catch (Exception e) {
            LOGGER.error("could not parse value {}", value, e);
        }
        return null;
    }

    private AbstractPhenomenon createPhenomenon(Phenomenon phenomenon) {
        return new OmObservableProperty(phenomenon.getId());
    }

}
