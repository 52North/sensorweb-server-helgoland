package org.n52.series.db.da.sos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.hibernate.Session;
import org.n52.series.db.da.HibernateSessionStore;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ds.hibernate.SessionFactoryProvider;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateSessionHolder implements HibernateSessionStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSessionHolder.class);

    private static final String DATASOURCE_PROPERTIES = "/datasource.properties";

    private HibernateSessionHolder sessionHolder;

    private static SessionFactoryProvider provider;

    public static HibernateSessionHolder createSessionHolder() {
        if (Configurator.getInstance() == null) {
            try (InputStream inputStream = SOSHibernateSessionHolder.class.getResourceAsStream(DATASOURCE_PROPERTIES)) {
                LOGGER.debug("SOS Configurator not present, trying to load DB config from '{}'", DATASOURCE_PROPERTIES);
                if (inputStream == null) {
                    LOGGER.error("No DB config found to configure SessionFactory!");
                    throw new RuntimeException("Could not establish database connection.");
                }
                Properties connectionProviderConfig = new Properties();
                connectionProviderConfig.load(inputStream);
                provider = new SessionFactoryProvider();
                provider.initialize(connectionProviderConfig);
                return new HibernateSessionHolder(provider);
            } catch (IOException e) {
                LOGGER.error("Could not establish database connection. Check '{}'", DATASOURCE_PROPERTIES, e);
                throw new RuntimeException("Could not establish database connection.");
            }
        } else {
            return new HibernateSessionHolder();
        }

    }

    @Override
    public void returnSession(Session session) {
        sessionHolder.returnSession(session);
    }

    @Override
    public Session getSession() {
        if (sessionHolder == null) {
            sessionHolder = createSessionHolder();
        }
        try {
            return sessionHolder.getSession();
        } catch (OwsExceptionReport e) {
            throw new IllegalStateException("Could not get hibernate session.", e);
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("shutdown '{}'", getClass().getSimpleName());
        provider.cleanup();
    }

}
