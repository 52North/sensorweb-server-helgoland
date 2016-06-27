package org.n52.sensorweb.spi.v1.ext;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.v1.ext.GeometryInfo;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.sensorweb.spi.v1.TransformationService;

import com.vividsolutions.jts.geom.Geometry;

public class TransformingGeometryOutputService extends ParameterService<GeometryInfo> {

    private final ParameterService<GeometryInfo> composedService;

    private final TransformationService transformationService;

    public TransformingGeometryOutputService(ParameterService<GeometryInfo> toCompose) {
        this.composedService = toCompose;
        this.transformationService = new TransformationService();
    }

    @Override
    public OutputCollection<GeometryInfo> getExpandedParameters(IoParameters query) {
        return transform(query, composedService.getExpandedParameters(query));
    }

    @Override
    public OutputCollection<GeometryInfo> getCondensedParameters(IoParameters query) {
        return transform(query, composedService.getCondensedParameters(query));
    }

    @Override
    public OutputCollection<GeometryInfo> getParameters(String[] items, IoParameters query) {
        return transform(query, composedService.getParameters(items, query));
    }

    @Override
    public GeometryInfo getParameter(String item, IoParameters query) {
        return transform(query, composedService.getParameter(item, query));
    }

    @Override
    public boolean exists(String id) {
        return composedService.exists(id);
    }

    private OutputCollection<GeometryInfo> transform(IoParameters query, OutputCollection<GeometryInfo> infos) {
        if (infos != null) {
            for (GeometryInfo info : infos) {
                transformInline(query, info);
            }
        }
        return infos;
    }

    private GeometryInfo transform(IoParameters query, GeometryInfo info) {
        transformInline(query, info);
        return info;
    }

    private void transformInline(IoParameters query, GeometryInfo info) {
        Geometry geometry = info.getGeometry();
        info.setGeometry(transformationService.transform(geometry, query));
    }

}
