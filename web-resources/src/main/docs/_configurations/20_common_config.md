---
layout: section
title: Common Configuration
---

#### Common Utils


{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
Configuration of common utility helpers
</div>
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
</div>

##### Static Service Entity
In case of a unique data backend a static service entity can be defined via Spring bean. Here's an example

{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
A static service configuration
</div>
```xml
<bean class="org.n52.series.db.beans.ServiceEntity">
  <property name="pkid" value="1" />
  <property name="version" value="2.0" />
  <property name="name" value="My Dataset Service" />
  <property name="noDataValues" value="-9999.0,99999,NO_DATA" />
</bean>
```
</div>