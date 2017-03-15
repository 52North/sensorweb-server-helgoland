package org.n52.io.extension.parents;

import java.util.Map;

import org.n52.io.request.IoParameters;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.extension.MetadataExtension;

public class HierarchicalParameterExtension extends MetadataExtension<PlatformOutput> {

    private static final String EXTENSION_NAME = "parents";


    private HierarchicalParameterService service;

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Map<String, Object> getExtras(PlatformOutput output, IoParameters parameters) {
        return wrapSingleIntoMap(service.getExtras(output.getId(), parameters));
    }

    @Override
    public void addExtraMetadataFieldNames(PlatformOutput output) {
        output.addExtra(EXTENSION_NAME);
    }

    public HierarchicalParameterService getService() {
        return service;
    }

    public void setService(HierarchicalParameterService service) {
        this.service = service;
    }

}
