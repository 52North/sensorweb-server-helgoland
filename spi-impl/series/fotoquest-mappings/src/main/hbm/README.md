# Database Mapping Configuration


## Supported DBMS
* PostrgeSQL 9.x with PostGIS 2.x


## Database configuration
Database configuration is done via Spring. To configure your settings take a look at
```
 WEB-INF\classes\application.properties
```

## <tl;dr>
If your database is not used in parallel by the REST API DAOs and an SOS instance,
or if you do not have to read from an SOS profiled data model (like the
`e-reporting` profile) you don't have to bother which mapping files have to be
configured. The default mappings should do fine.


## Supported Schemas
All schemas under `hbm/sos/<version>/` subpackages are valid for all SOS versions
until a higher `<version>`. All mappings under `hbm/sos/v41/` are valid for all
SOS versions which are higher (or the same) than `4.1.x`. If the higher `v44`
subdirectory would appear, those mapping files would be valid only until (not
including!) that higher version (`4.1.x--4.3.x`).


## Different Concepts
Particular profiles may require some different mappings. Subpackages under the
version number like `hbm/sos/<version>/<profile>` indicate which mappings to
take for a profile.


## Example configurations
Schema mapping files are configured under `WEB-INF/spring/series-database-config.xml`.
Configure the `seriesSessionFactory` bean so that it takes those mappings needed
for your database model (see below).

### 52°North SOS v4.3.x with `series` profile
```
<property name="mappingLocations">
  <list>
    <value>classpath:hbm/sos/v42/*.hbm.xml</value>
    <value>classpath:hbm/sos/v42/series/*.hbm.xml</value>
  </list>
</property>
```

Note that the mappings under `v42` are also valid for 52°North SOS v4.3.x datamodel.


### 52°North SOS v4.3.x with `e-reporting` profile
```
<property name="mappingLocations">
  <list>
    <value>classpath:hbm/sos/v42/*.hbm.xml</value>
    <value>classpath:hbm/sos/v43/ereporting/*.hbm.xml</value>
  </list>
</property>
```

Note that the mappings under `v42` are also valid for 52°North SOS v4.3.x datamodel.