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

import com.vividsolutions.jts.geom.Geometry;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanOrganization;
import eu.trentorise.opendata.jackan.model.CkanTag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.exception.ows.concrete.InvalidSridException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.AbstractFeature;
import org.n52.sos.ogc.om.AbstractPhenomenon;
import org.n52.sos.ogc.om.OmObservableProperty;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.om.OmObservationConstellation;
import org.n52.sos.ogc.om.features.SfConstants;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorML20Constants;
import org.n52.sos.ogc.sensorML.SmlContact;
import org.n52.sos.ogc.sensorML.SmlContactList;
import org.n52.sos.ogc.sensorML.SmlResponsibleParty;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.SmlCapabilities;
import org.n52.sos.ogc.sensorML.elements.SmlClassifier;
import org.n52.sos.ogc.sensorML.elements.SmlIdentifier;
import org.n52.sos.ogc.sensorML.elements.SmlIo;
import org.n52.sos.ogc.sos.SosOffering;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SweField;
import org.n52.sos.ogc.swe.SweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SweQuantity;
import org.n52.sos.ogc.swe.simpleType.SweText;
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
    
    void insertData(ResourceTable platformTable, SchemaDescriptor schemaDescription, CsvObservationsCollection csvObservationsCollection) {
        final List<Phenomenon> phenomena = parseObservableProperties(platformTable, schemaDescription);
        Map<ResourceMember, DataFile> observationCollections = csvObservationsCollection.getObservationDataCollections();
        for (Map.Entry<ResourceKey, Map<ResourceField, String>> rowEntry : platformTable.getTable().rowMap().entrySet()) {
//            try {
                final AbstractFeature feature = createFeatureRelation(rowEntry.getValue());
                for (Phenomenon phenomenon : phenomena) {
                    final InsertSensorRequest insertSensorRequest = new InsertSensorRequest();
//                    insertSensorRequest.setRelatedFeature(feature); // via insert observation
                    insertSensorRequest.setObservableProperty(phenomenaToIdList(phenomena));
                    final SosProcedureDescription sml = parseProcedureDescription(feature, phenomenon, schemaDescription);
                    insertSensorRequest.setProcedureDescription(sml);
                    //insertSensorDao.insertSensor(insertSensorRequest);
                    
                    for (Map.Entry<ResourceMember, DataFile> observationEntry : observationCollections.entrySet()) {
                        
                        ResourceTable observationTable = new ResourceTable(observationEntry.getKey(), observationEntry.getValue());
                        final ResourceMember leftMember = rowEntry.getKey().getMember();
                        observationTable.readIntoMemory(observationEntry.getKey().getJoinFields(leftMember));
                        InsertObservationRequest insertObservationRequest = new InsertObservationRequest();
                        
                        final DataTable joinedTable = observationTable.innerJoin(platformTable);
                        OmObservationConstellation constellation = new OmObservationConstellation();
                        constellation.setOfferings(offeringsToIdList(insertSensorRequest.getAssignedOfferings()));
                        constellation.setObservableProperty(createPhenomenon(phenomenon));
                        constellation.setFeatureOfInterest(feature);
                        constellation.setProcedure(sml);
                        insertObservationRequest.setObservation(createObservations(joinedTable, constellation, schemaDescription));
                    }
                }
//            } catch (OwsExceptionReport e) {
//                LOGGER.error("could not insert or update sensor", e);
//            } 
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
                                joinableField.getOther(CkanConstants.KnownFieldProperty.UOM)
                        ));
                    }
                }
            }
        }
        return new ArrayList<>(observableProperties);
    }


    private SosProcedureDescription parseProcedureDescription(AbstractFeature feature, Phenomenon phenomenon, SchemaDescriptor schemaDescription) {
        final System system = new org.n52.sos.ogc.sensorML.System();
        system.setDescription(schemaDescription.getDataset().getNotes());
        return system
                .setInputs(Collections.<SmlIo<?>>singletonList(createInput(phenomenon)))
                .setOutputs(Collections.<SmlIo<?>>singletonList(createOutput(phenomenon)))
                .setKeywords(createKeywordList(feature, phenomenon, schemaDescription))
                .setIdentifications(createIdentificationList(feature, phenomenon))
                .setClassifications(createClassificationList(feature, phenomenon))
//                .setValidTime(new ) // TODO
                .addCapabilities(createCapabilities(feature, phenomenon, schemaDescription))
//                .addContact(createContact(schemaDescription.getDataset())) // TODO
                // ... // TODO
                ;
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
                createProcedureLongName(feature, phenomenon.getLabel())));
        return idents;
    }

    private String createProcedureId(AbstractFeature feature, Phenomenon phenomenon) {
        return phenomenon + "-" + feature.getIdentifier();
    }

    private String createProcedureLongName(AbstractFeature feature, String phenomenon) {
        return phenomenon + "@" + feature.getFirstName().getValue();
    }

    private List<SmlClassifier> createClassificationList(AbstractFeature feature, Phenomenon phenomenon) {
        return Collections.singletonList(new SmlClassifier(
                "phenomenon", 
                "urn:ogc:def:classifier:OGC:1.0:phenomenon", 
                null, 
                phenomenon.getId()));
    }

    private List<SmlCapabilities> createCapabilities(AbstractFeature feature, Phenomenon phenomenon, SchemaDescriptor schemaDescription) {
        List<SmlCapabilities> capabilities = new ArrayList<>();
        capabilities.add(createFeatureCapabilities(feature, phenomenon, schemaDescription));
        capabilities.add(createOfferingCapabilities(feature, phenomenon, schemaDescription));
        capabilities.add(createBboxCapabilities(feature, phenomenon, schemaDescription));
        return capabilities;
    }
    
    private SmlCapabilities createFeatureCapabilities(AbstractFeature feature, Phenomenon phenomenon, SchemaDescriptor schemaDescription) {
        SmlCapabilities featuresCapabilities = new SmlCapabilities("featuresOfInterest");
        final SweSimpleDataRecord record = new SweSimpleDataRecord()
                .addField(createTextField(
                        SensorML20Constants.FEATURE_OF_INTEREST_FIELD_NAME, 
                        SensorML20Constants.FEATURE_OF_INTEREST_FIELD_DEFINITION, 
                        feature.getIdentifier()));
        return featuresCapabilities.addAbstractDataComponents(record);
    }

    private SmlCapabilities createOfferingCapabilities(AbstractFeature feature, Phenomenon phenomenon, SchemaDescriptor schemaDescription) {
        SmlCapabilities offeringCapabilities = new SmlCapabilities("offerings");
        String offeringId = "Offering_" + createProcedureId(feature, phenomenon);
        final SweSimpleDataRecord record = new SweSimpleDataRecord()
                .addField(createTextField(
                        "field_0",
                        SensorML20Constants.OFFERING_FIELD_DEFINITION, 
                        offeringId));
        return offeringCapabilities.addAbstractDataComponents(record);
    }

    private SweField createTextField(String name, String definition, String value) {
        return new SweField(name, new SweText().setValue(value).setDefinition(definition));
    }

    private SmlCapabilities createBboxCapabilities(AbstractFeature feature, Phenomenon phenomenon, SchemaDescriptor schemaDescription) {
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

    private List<OmObservation> createObservations(DataTable observationTable, OmObservationConstellation constellation, SchemaDescriptor schemaDescription) {
        List<OmObservation> observations = new ArrayList<>();
        Map<ResourceKey, Map<ResourceField, String>> rowMap = observationTable.getTable().rowMap();
        for (Map.Entry<ResourceKey, Map<ResourceField, String>> observationEntry : rowMap.entrySet()) {
            OmObservation omObservation = new OmObservation();
            for (Map.Entry<ResourceField, String> rowEntry : observationEntry.getValue().entrySet()) {
                ResourceKey key = observationEntry.getKey();
                ResourceField field = rowEntry.getKey();
                String resourceType = field.getQualifier().getResourceType();
                if (resourceType.equalsIgnoreCase(CkanConstants.ResourceType.OBSERVATIONS)) {
                    parseObservationValue(key, omObservation, field, constellation);
                } else if (resourceType.equalsIgnoreCase(CkanConstants.ResourceType.PLATFORMS)) {
                    parsePlatformValue(key, omObservation, field, schemaDescription);
                }
                
            }
        }
        return observations;
    }

    private void parseObservationValue(ResourceKey key, OmObservation omObservation, ResourceField field, OmObservationConstellation constellation) {
        if (field.hasProperty(CkanConstants.KnownFieldProperty.PHENOMENON)) {
            String phenomenonField = field.getFieldId();
            String phenomenonId = constellation.getObservableProperty().getIdentifier();
            if (phenomenonField.equalsIgnoreCase(phenomenonId)) {
                omObservation.setObservationID(key.getKeyId());
                omObservation.setDefaultElementEncoding(CkanConstants.DEFAULT_CHARSET.toString());
//                omObservation.setIdentifier(null)

            }
        }
    }

    private void parsePlatformValue(ResourceKey key, OmObservation omObservation, ResourceField field, SchemaDescriptor schemaDescription) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private AbstractPhenomenon createPhenomenon(Phenomenon phenomenon) {
        return new OmObservableProperty(phenomenon.getId());
    }

    
}
