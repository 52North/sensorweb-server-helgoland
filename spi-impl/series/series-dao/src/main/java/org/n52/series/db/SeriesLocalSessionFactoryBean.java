package org.n52.series.db;

import java.util.Properties;
import java.util.TimeZone;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

public class SeriesLocalSessionFactoryBean extends LocalSessionFactoryBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeriesLocalSessionFactoryBean.class);

    private static final String JDBC_TIME_ZONE = "jdbc.time.zone";

    @Override
    protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
        Properties properties = sfb.getProperties();
        sfb.registerTypeOverride(createZonalTimestampType(properties));
        return super.buildSessionFactory(sfb);
    }

    private ZonalTimestampType createZonalTimestampType(Properties properties) {
        return new ZonalTimestampType(createTimeZone(properties));
    }

    private TimeZone createTimeZone(Properties properties) {
        String zone = properties.containsKey(JDBC_TIME_ZONE)
            ? properties.getProperty(JDBC_TIME_ZONE)
            : "UTC";
        try {
            LOGGER.info("Configure timezone for JDBC layer: " + zone);
            return TimeZone.getTimeZone(zone);
        } catch (Throwable e) {
            LOGGER.warn("Could not configure timezone for JDBC layer: " + zone);
            return TimeZone.getTimeZone("UTC");
        }
    }
}
