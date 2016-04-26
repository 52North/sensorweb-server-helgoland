package org.n52.series.db.da;

import org.hibernate.Session;

public interface HibernateSessionStore {
    
    public Session getSession();
    
    public void returnSession(Session session);
    
    public void shutdown();
    
}
