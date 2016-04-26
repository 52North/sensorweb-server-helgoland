package org.n52.series.db.da;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class SeriesHibernateSessionHolder implements HibernateSessionStore {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SeriesHibernateSessionHolder.class);
        
    @Autowired
    private SessionFactory seriesSessionFactory;
    
    @Override
    public Session getSession() {
        return seriesSessionFactory.openSession();
    }

    @Override
    public void returnSession(Session session) {
        if (session != null && session.isOpen()) {
            session.clear();
            session.close();
        }
    }
    
//    public EntityManager createEntityManager(String persistenceUnitName) {
//        return createEntityManager(persistenceUnitName, Collections.<String, Object>emptyMap());
//    }
//    
//    public EntityManager createEntityManager(String persistenceUnitName, Map<String, Object> overrides) {
//        return Persistence.createEntityManagerFactory(persistenceUnitName, overrides)
//                .createEntityManager();
//    }

    @Override
    public void shutdown() {
        LOGGER.info("Closing '{}'", getClass().getSimpleName());
        seriesSessionFactory.close();
    }
    
}
