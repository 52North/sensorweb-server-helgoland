---
layout: page
title: Common Configuration
permalink: /configuration/common_config
---

## Common Utils

```xml
<bean class="org.n52.series.db.dao.DefaultDbQueryFactory">
  <property name="databaseSrid" value="${database.srid}" />
</bean>

<bean class="org.n52.series.db.da.EntityCounter" />
<bean id="metadataService" class="org.n52.series.srv.CountingMetadataAccessService" />
<bean id="searchService" class="org.n52.series.srv.Search" />

<bean class="org.n52.series.db.da.DefaultDataRepositoryFactory" />
<bean class="org.n52.io.DefaultIoFactory" />
```

### Static Service Entity
In case of a unique data backend a static service entity can be defined via Spring bean. Here's an example

```xml
<bean class="org.n52.series.db.beans.ServiceEntity">
  <property name="pkid" value="1" />
  <property name="version" value="2.0" />
  <property name="name" value="My Dataset Service" />
  <property name="noDataValues" value="-9999.0,99999,NO_DATA" />
</bean>
```