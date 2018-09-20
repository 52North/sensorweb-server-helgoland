package org.n52.io.generalize.quantity;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.type.quantity.generalize.DouglasPeuckerGeneralizer;
import org.n52.io.type.quantity.generalize.Generalizer;
import org.n52.io.type.quantity.generalize.GeneralizerFactory;

public class GeneralizerFactoryTest {

    @Test
    public void when_nonDefaultAlgorithmConfig_then_factoryCreatesAppropriateGeneralizer() throws URISyntaxException {
        IoParameters nonDefaultConfig = IoParameters.createDefaults(getAlternativeConfigFile());
        Generalizer<Data<QuantityValue>> generalizer = GeneralizerFactory.createGeneralizer(nonDefaultConfig);
        assertThat(generalizer, IsInstanceOf.instanceOf(DouglasPeuckerGeneralizer.class));
    }

    private File getAlternativeConfigFile() throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/")
                                        .toURI());
        return root.resolve("test-config.json")
                   .toFile();
    }

}
