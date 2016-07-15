package org.n52.io;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.is;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DatasetFactoryTest {

    private DatasetFactory<Collection> factory;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
            
    
    @Before
    public void setUp() throws URISyntaxException {
        File config = getConfigFile("dataset-collection-factory.properties");
        factory = createCollectionFactory(config);
    }
    
    @Test
    public void when_created_then_hasMappings() throws DatasetFactoryException {
        Assert.assertTrue(factory.create("arraylist").getClass() == ArrayList.class);
    }

    @Test
    public void when_created_then_initHaveBeenCalled() throws DatasetFactoryException {
        Assert.assertThat(factory.create("arraylist").isEmpty(), Matchers.is(false));
    }
    
    @Test
    public void when_createdWithNullConfig_then_configureWithFallback() {
        DatasetFactory<Collection> f = createCollectionFactory(null);
        Assert.assertTrue(f.isKnown("hashmap"));
    }
    
    @Test
    public void when_havingInvalidEntry_then_throwException() throws URISyntaxException, DatasetFactoryException {
        thrown.expect(DatasetFactoryException.class);
        thrown.expectMessage(is("No datasets available for 'invalid'."));
        File configFile = getConfigFile("dataset-collection-factory-invalid-entry.properties");
        new DefaultIoHandlerFactory(configFile).create("invalid");
    }

    @Test
    public void when_creatingOfInvalidType_then_throwException() throws DatasetFactoryException {
        thrown.expect(DatasetFactoryException.class);
        thrown.expectMessage(is("No datasets available for 'invalid'."));
        factory.create("invalid");
    }

    private File getConfigFile(String name) throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/").toURI());
        return root.resolve(name).toFile();
    }

    private DatasetFactory<Collection> createCollectionFactory(File config) {
        return new DatasetFactory<Collection>(config) {
            @Override
            protected String getFallbackConfigResource() {
                return "/dataset-collection-factory-fallback.properties";
            }

            @Override
            protected Collection initInstance(Collection instance) {
                instance.add(new Object());
                return instance;
            }

            @Override
            protected Class<Collection> getTargetType() {
                return Collection.class;
            }
        };
    }
    
}
