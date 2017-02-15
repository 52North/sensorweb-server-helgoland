package org.n52.proxy.decode;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.sampling.x20.SFSamplingFeatureDocument;
import net.opengis.sampling.x20.SFSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.shetland.ogc.OGCConstants;
import org.n52.shetland.ogc.gml.AbstractFeature;
import org.n52.shetland.ogc.gml.CodeType;
import org.n52.shetland.ogc.gml.CodeWithAuthority;
import org.n52.shetland.ogc.om.features.SfConstants;
import org.n52.shetland.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.svalbard.decode.AbstractGmlDecoderv321;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.util.CodingHelper;
import org.n52.svalbard.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jan Schulte
 */
public class SamplingFeatureDecoder extends AbstractGmlDecoderv321<XmlObject, AbstractFeature> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingFeatureDecoder.class);

    private static final Set<DecoderKey> DECODER_KEYS = CodingHelper.decoderKeysForElements(
            SfConstants.NS_SF, SFSamplingFeatureDocument.class,
            SFSamplingFeatureType.class);

    @Override
    public AbstractFeature decode(XmlObject element) throws DecodingException {
        return parseSamplingFeature(((SFSamplingFeatureDocument) element).getSFSamplingFeature());
    }

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.unmodifiableSet(DECODER_KEYS);
    }

    private AbstractFeature parseSamplingFeature(SFSamplingFeatureType sfSamplingFeature) throws DecodingException {
        final SamplingFeature sosFeat = new SamplingFeature(null, sfSamplingFeature.getId());
        parseAbstractFeatureType(sfSamplingFeature, sosFeat);
        sosFeat.setFeatureType(getFeatureType(sfSamplingFeature.getType()));
        sosFeat.setSampledFeatures(getSampledFeatures(sfSamplingFeature.getSampledFeatureArray()));
        sosFeat.setXml(getXmlDescription(sfSamplingFeature));
        sosFeat.setGeometry(getGeometry(sfSamplingFeature));
        checkTypeAndGeometry(sosFeat);
        sosFeat.setGmlId(sfSamplingFeature.getId());
        return sosFeat;
    }

    private String getFeatureType(final ReferenceType type) {
        if (type != null && type.getHref() != null && !type.getHref().isEmpty()) {
            return type.getHref();
        }
        return null;
    }

    private List<AbstractFeature> getSampledFeatures(FeaturePropertyType[] sampledFeatureArray)
            throws DecodingException {
        final List<AbstractFeature> sampledFeatures = Lists.newArrayList();
        for (FeaturePropertyType featurePropertyType : sampledFeatureArray) {
            sampledFeatures.addAll(getSampledFeatures(featurePropertyType));
        }
        return sampledFeatures;
    }

    private List<AbstractFeature> getSampledFeatures(final FeaturePropertyType sampledFeature)
            throws DecodingException {
        final List<AbstractFeature> sampledFeatures = new ArrayList<>(1);
        if (sampledFeature != null && !sampledFeature.isNil()) {
            // if xlink:href is set
            if (sampledFeature.getHref() != null && !sampledFeature.getHref().isEmpty()) {
                if (sampledFeature.getHref().startsWith("#")) {
                    sampledFeatures.add(new SamplingFeature(null, sampledFeature.getHref().replace("#", "")));
                } else {
                    final SamplingFeature sampFeat
                            = new SamplingFeature(new CodeWithAuthority(sampledFeature.getHref()));
                    if (sampledFeature.getTitle() != null && !sampledFeature.getTitle().isEmpty()) {
                        sampFeat.addName(new CodeType(sampledFeature.getTitle()));
                    }
                    sampledFeatures.add(sampFeat);
                }
            } else {
                XmlObject abstractFeature = null;
                if (sampledFeature.getAbstractFeature() != null) {
                    abstractFeature = sampledFeature.getAbstractFeature();
                } else if (sampledFeature.getDomNode().hasChildNodes()) {
                    try {
                        abstractFeature = XmlObject.Factory
                                .parse(XmlHelper.getNodeFromNodeList(sampledFeature.getDomNode().getChildNodes()));
                    } catch (XmlException xmle) {
                        throw new DecodingException("Error while parsing feature request!", xmle);
                    }
                }
                if (abstractFeature != null) {
                    final Object decodedObject = decodeXmlObject(abstractFeature);
                    if (decodedObject instanceof AbstractFeature) {
                        sampledFeatures.add((AbstractFeature) decodedObject);
                    }
                }
                throw new DecodingException(Sos2Constants.InsertObservationParams.observation,
                        "The requested sampledFeature type is not supported by this service!");
            }
        }
        return sampledFeatures;
    }

    private String getXmlDescription(SFSamplingFeatureType sfSamplingFeature) {
        final SFSamplingFeatureDocument featureDoc
                = SFSamplingFeatureDocument.Factory.newInstance(getXmlOptions());
        featureDoc.setSFSamplingFeature(sfSamplingFeature);
        return featureDoc.xmlText(getXmlOptions());
    }

    private void checkTypeAndGeometry(final SamplingFeature sosFeat) throws DecodingException {
        final String featTypeForGeometry = getFeatTypeForGeometry(sosFeat.getGeometry());
        if (sosFeat.getFeatureType() == null) {
            sosFeat.setFeatureType(featTypeForGeometry);
        } else {
            if (!featTypeForGeometry.equals(sosFeat.getFeatureType())) {
                throw new DecodingException("The requested observation is invalid! The featureOfInterest type "
                        + "does not comply with the defined type (%s)!", sosFeat.getFeatureType());
            }
        }
    }

    private String getFeatTypeForGeometry(final Geometry geometry) {
        if (geometry instanceof Point) {
            return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT;
        } else if (geometry instanceof LineString) {
            return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE;
        } else if (geometry instanceof Polygon) {
            return SfConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE;
        }
        return OGCConstants.UNKNOWN;
    }

    private Geometry getGeometry(SFSamplingFeatureType sfSamplingFeature) throws DecodingException {
        XmlObject[] shapes = sfSamplingFeature.selectChildren(SfConstants.NS_SAMS, "shape");
        if (shapes.length == 1) {
            try {
                ShapeDocument shapeDoc = (ShapeDocument) XmlObject.Factory.parse(shapes[0].getDomNode());
                Object decodedObject = decodeXmlElement(shapeDoc.getShape().getAbstractGeometry());
                if (decodedObject instanceof Geometry) {
                    return (Geometry) decodedObject;
                }
            } catch (XmlException ex) {
                throw new DecodingException(Sos2Constants.InsertObservationParams.observation,
                        "The requested geometry type of featureOfInterest is not supported by this service!");
            }
        }
        throw new DecodingException(Sos2Constants.InsertObservationParams.observation,
                "The requested geometry type of featureOfInterest is not supported by this service!");
    }

}
