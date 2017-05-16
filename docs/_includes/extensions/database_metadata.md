
Next to the actual phenomenon time observation data may have a `resultTime` which 
indicates when the data became available. An example for such cases is forecast data
which may be (re-)calculated multiple times (optionally based on multiple models). 
Getting the right data values (belonging to a specific result time) a client can add 
the `resultTime=...` parameter when querying `datasets/<id>/data`.

In case of existing result times (which have to be different to the actual phenomenon 
time) are available to a client as `extra` data for a given dataset. 

### Requirements

The extension is part of the [database backend](https://github.com/52North/dao-series-api).
Therefore it requires a database (the DBMS shouldn't matter as connection is done via 
[Hibernate](http://hibernate.org/)) which has been extended by a metadata table. Also the
`metadata` mapping files have to be configured.

### Installation steps
1. create `metadata` table by executing `src/extension/metadata/create_metadata_table.sql`
1. add `hbm/sos/metadata/*.hbm.xml` to the `application.properties` in use, e.g.
```
series.database.mappings=\
  classpath*:/hbm/sos/v44/*.hbm.xml, \
  classpath*:/hbm/sos/metadata/*.hbm.xml
```
1. add metadata data for your dataset, e.g. with id `10`
```
﻿INSERT INTO series_metadata VALUES (1, 10, 'integrationPeriod', 'string', 'PT1M') ;
```

{:.n52-callout .n52-callout-todo}
`json` metadata is not serialized properly at the moment. 