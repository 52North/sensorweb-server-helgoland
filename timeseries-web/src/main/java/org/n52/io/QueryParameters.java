
package org.n52.io;

import java.util.Map;

import org.n52.io.crs.BoundingBox;
import org.n52.io.img.ChartDimension;
import org.n52.io.v1.data.StyleProperties;
import org.n52.web.BadRequestException;
import org.n52.web.WebException;
import org.springframework.util.MultiValueMap;

/**
 * Delegates IO parameters to an {@link IoParameters} instance by composing parameter access with Web
 * exception handling.
 */
public class QueryParameters extends IoParameters {

    private IoParameters composedParameterAccess;

    /**
     * Creates an simple view on given query. The {@link MultiValueMap} is flattened to a single value map.
     * 
     * @param query
     *        the incoming query parameters.
     * @return a query parameters instance handling Web exceptions.
     * @see WebException
     */
    public static IoParameters createFromQuery(MultiValueMap<String, String> query) {
        return createFromQuery(query.toSingleValueMap());
    }
    
    /**
     * Creates an simple view on given query.
     * 
     * @param query
     *        the incoming query parameters.
     * @return a query parameters instance handling Web exceptions.
     * @see WebException
     */
    public static IoParameters createFromQuery(Map<String, String> query) {
        return new QueryParameters(query);
    }

    private QueryParameters(Map<String, String> query) {
        super(query);
    }

    public int getOffset() {
        try {
            return composedParameterAccess.getOffset();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Bad '" + OFFSET + "' parameter.", e);
        }
    }

    public int getLimit() {
        try {
            return composedParameterAccess.getLimit();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Bad '" + LIMIT + "' parameter.", e);
        }
    }

    public ChartDimension getChartDimension() {
        try {
            return composedParameterAccess.getChartDimension();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Bad '" + WIDTH + "' or '" + HEIGHT + "' parameter(s).", e);
        }
    }

    public boolean isBase64() {
        try {
            return composedParameterAccess.isBase64();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Bad '" + BASE_64 + "' parameter.", e);
        }
    }

    public boolean isGrid() {
        try {
            return composedParameterAccess.isGrid();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Bad '" + GRID + "' parameter.", e);
        }
    }

    public boolean isGeneralize() {
        try {
            return composedParameterAccess.isGeneralize();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Bad '" + GENERALIZE + "' parameter.", e);
        }
    }

    public boolean isLegend() {
        try {
            return composedParameterAccess.isLegend();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Bad '" + LEGEND + "' parameter.", e);
        }
    }

    public String getLocale() {
        return composedParameterAccess.getLocale();
    }

    public StyleProperties getStyle() {
        try {
            return composedParameterAccess.getStyle();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Could not read '" + STYLE + "' property.", e);
        }
    }

    public String getFormat() {
        return composedParameterAccess.getFormat();
    }

    public String getTimespan() {
        try {
            return composedParameterAccess.getTimespan();
        }
        catch (IoParseException e) {
            BadRequestException badRequest = new BadRequestException("Invalid timespan.", e);
            badRequest.addHint("Valid timespans have to be in ISO8601 period format.");
            badRequest.addHint("Valid examples: 'PT6H/2013-08-13TZ' or '2013-07-13TZ/2013-08-13TZ'.");
            throw badRequest;
        }
    }

    public String getCategory() {
        return composedParameterAccess.getCategory();
    }

    public String getService() {
        return composedParameterAccess.getService();
    }

    public String getOffering() {
        return composedParameterAccess.getOffering();
    }

    public String getFeature() {
        return composedParameterAccess.getFeature();
    }

    public String getProcedure() {
        return composedParameterAccess.getProcedure();
    }

    public String getPhenomenon() {
        return composedParameterAccess.getPhenomenon();
    }

    public BoundingBox getSpatialFilter() {
        try {
            return composedParameterAccess.getSpatialFilter();
        }
        catch (IoParseException e) {
            BadRequestException ex = new BadRequestException("Spatial filter could not be determined.");
            ex.addHint("Refer to the API documentation and check the parameter against required syntax!");
            ex.addHint("Check http://epsg-registry.org for EPSG CRS definitions and codes.");
            throw ex;
        }
    }

    public String getCrs() {
        return composedParameterAccess.getCrs();
    }

    public boolean isForceXY() {
        try {
            return composedParameterAccess.isForceXY();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Bad '" + FORCE_XY + "' parameter.", e);
        }
    }

    public boolean isExpanded() {
        try {
            return composedParameterAccess.isExpanded();
        }
        catch (IoParseException e) {
            throw new BadRequestException("Bad '" + EXPANDED + "' parameter.", e);
        }
    }

    public boolean containsParameter(String parameter) {
        return composedParameterAccess.containsParameter(parameter);
    }

    public String getOther(String parameter) {
        return composedParameterAccess.getOther(parameter);
    }

}
