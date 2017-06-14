---
layout: page
title: Configuration
permalink: /configuration
---

{:.n52-callout .n52-callout-todo}
section under revision/update

The Web interface is intended and prepared for straight integration with Spring MVC. 
However, using Spring as Web framework is not required but leaves open the last 
meters of integrating the endpoint controllers.

{:.n52-callout .n52-callout-todo}
make links to Spring MVC and to endpoint controllers.

All endpoints are annotated with Spring's annotations `Connector`, `RequestMethod`, 
`RequestMapping`, etc. What is needed is a configured `DispatcherServlet` 
in the `web.xml` and proper beans injections.

{:.n52-callout .n52-callout-todo}
Check if/how we can provide a pre-configured `Configuration` lib or class which can
be changed/extended by Web application implementors.

## Application Configuration Locations
* `WEB-INF/classes/config-general.properties`
* `WEB-INF/spring/`
* `WEB-INF/classes/application.properties`
* `WEB-INF/web.xml` (cors etc.)

## General Configuration

### Enabling CORS
{:.n52-callout .n52-callout-todo}
link to CORS testing page

CORS can be enabled via a third party filter (to stay independend from a concrete
Servlet container which may not ship such filter) within a Web application's `web.xml`
file. A simple example (which allows all requests) may look like this:

```
<filter>
    <filter-name>CORS</filter-name>
    <filter-class>com.thetransactioncompany.cors.CORSFilter</filter-class>
    <init-param>
        <param-name>cors.allowOrigin</param-name>
        <param-value>*</param-value>
    </init-param>
    <init-param>
        <param-name>cors.allowGenericHttpRequests</param-name>
        <param-value>true</param-value>
    </init-param>
    <init-param>
        <param-name>cors.supportedMethods</param-name>
        <param-value>GET, POST, HEAD, PUT, DELETE, OPTIONS</param-value>
    </init-param>
    <init-param>
        <param-name>cors.supportedHeaders</param-name>
        <param-value>Content-Type, Content-Encoding, Accept</param-value>
    </init-param>
    <init-param>
        <param-name>cors.exposedHeaders</param-name>
        <param-value>Content-Type, Content-Encoding</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>CORS</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```


## Spring

This describes an example configuration via Spring. There are lots of variants and 
alternatives which may end in the same result. This example splits Spring configuration 
files into two main files:

* `/WEB-INF/spring/dispatcher-servlet.xml` contains Web controllers and views
* `/WEB-INF/spring/application-context.xml` contains SPI implementations

However, everything starts by adding Spring's `DispatcherServlet` within the `web.xml`, 
put both configuration files, and relate it to some context path like so:

```xml
<servlet>
    <servlet-name>api-dispatcher</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/spring/dispatcher-servlet.xml,/WEB-INF/spring/application-context.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>

<servlet-mapping>
    <servlet-name>api-dispatcher</servlet-name>
    <url-pattern>/api/*</url-pattern>
</servlet-mapping>
```

### Managing Configuration

#### Separate Properties
It is helpful to separate all properties values from XML configuration for several
reasons. First, it may be tedious to find all single properties within verbose XML. 
However, more it also very important to keep sensitive information (like database
configuration) from the project itself.

Create a `WEB-INF/classes/application.properties` file which will keep all the default 
values. Another may be located under your home directory. Configure the following which
will first take parameters 

```xml
<!-- these properties do override default settings -->
<ctx:property-placeholder 
    <!-- ${user.home}/application.properties} when -Dlocal.configFile not defined -->
    location="${local.configFile:${user.home}/application.properties}"
    ignore-resource-not-found="true" ignore-unresolvable="true" order="0" />

<!-- default settings -->
<ctx:property-placeholder 
    location="classpath:/application.properties"
    ignore-resource-not-found="false" ignore-unresolvable="false" order="1" />
```

A placeholder can now be declared within Spring XML files via `${placeholder:default}`.
If present in the application properties file (your one or the default) it will be 
replaced, otherwise the given default will be used.

#### Separate Configuration Sections

To keep overview we can separate parts of the configuration files and include them
via file import, e.g. `<import resource="mvc.xml" />`.




### Dispatcher Configuration
Dispatcher configuration includes content negotiation and default serialization config
and Web controller injections.

#### Content Negotiation
To support proper content negotiation and JSON serialization the following should be 
added to the `/WEB-INF/spring/dispatcher-servlet.xml`:

```xml
<mvc:annotation-driven />
<ctx:annotation-config />

<bean id="objectMapper" class="com.fasterxml.jackson.databind.ObjectMapper">
    <property name="serializationInclusion" value="NON_NULL" />
</bean>

<bean id="jsonViewResolver" class="org.springframework.web.servlet.view.json.MappingJackson2JsonView">
    <property name="extractValueFromSingleKeyModel" value="true" />
    <property name="objectMapper" ref="objectMapper" />
</bean>

<mvc:annotation-driven content-negotiation-manager="contentNegotiationManager" />

<bean id="contentNegotiationManager" class="org.springframework.web.accept.ContentNegotiationManagerFactoryBean">
    <property name="defaultContentType" value="application/json" />
</bean>

<bean class="org.springframework.web.servlet.view.ContentNegotiatingViewResolver">
    <property name="defaultViews">
        <util:list>
            <ref bean="jsonViewResolver" />
        </util:list>
    </property>
</bean>
```

#### Web Controller injections
A Web controller behaves like described in the [Web API]({{site.baseurl}}/api.html) and performs 
[I/O operations]({{site.baseurl}}/io.html) if needed. 


{:.n52-callout .n52-callout-info}
A Web controller delegates data request to the actual SPI implementation so it has to be 
referenced here. SPI implementors have to use these references to make sure the right
backend service is called.

```xml
<mvc:annotation-driven />
<ctx:annotation-config />

<!--
    This bean description file injects the Web binding layer. SPI implementation 
    beans have to match the ref-ids associated below.
-->

<bean class="org.n52.web.ctrl.ResourcesController">
    <property name="metadataService" ref="metadataService" />
</bean>

<bean class="org.n52.web.ctrl.SearchController">
    <property name="searchService" ref="searchService"/>
</bean>

<bean class="org.n52.web.ctrl.ParameterController" id="parameterController" abstract="true">
    <property name="externalUrl" value="${external.url}" />
    <property name="metadataExtensions">
        <list>
            <bean class="org.n52.io.response.extension.LicenseExtension" />
        </list>
    </property>
</bean>

<bean class="org.n52.web.ctrl.OfferingsParameterController" parent="parameterController">
    <property name="parameterService">
        <bean class="org.n52.web.ctrl.ParameterBackwardsCompatibilityAdapter">
            <constructor-arg index="0" ref="offeringParameterService" />
        </bean>
    </property>
</bean>

<bean class="org.n52.web.ctrl.ServicesParameterController" parent="parameterController">
    <property name="parameterService">
        <bean class="org.n52.web.ctrl.ParameterBackwardsCompatibilityAdapter">
            <constructor-arg index="0" ref="serviceParameterService" />
        </bean>
    </property>
</bean>

<bean class="org.n52.web.ctrl.CategoriesParameterController" parent="parameterController">
    <property name="parameterService">
        <bean class="org.n52.web.ctrl.ParameterBackwardsCompatibilityAdapter">
            <constructor-arg index="0" ref="categoryParameterService" />
        </bean>
    </property>
</bean>

<bean class="org.n52.web.ctrl.FeaturesParameterController" parent="parameterController">
    <property name="parameterService">
        <bean class="org.n52.web.ctrl.ParameterBackwardsCompatibilityAdapter">
            <constructor-arg index="0" ref="featureParameterService" />
        </bean>
    </property>
</bean>

<bean class="org.n52.web.ctrl.ProceduresParameterController" parent="parameterController">
    <property name="parameterService">
        <bean class="org.n52.web.ctrl.ParameterBackwardsCompatibilityAdapter">
            <constructor-arg index="0" ref="procedureParameterService" />
        </bean>
    </property>
</bean>

<bean class="org.n52.web.ctrl.PhenomenaParameterController" parent="parameterController">
    <property name="parameterService">
        <bean class="org.n52.web.ctrl.ParameterBackwardsCompatibilityAdapter">
            <constructor-arg index="0" ref="phenomenonParameterService" />
        </bean>
    </property>
</bean>

<bean class="org.n52.web.ctrl.PlatformsParameterController" parent="parameterController">
    <property name="parameterService" ref="platformParameterService" />
    <property name="metadataExtensions">
        <list merge="true">
            <bean class="org.n52.io.extension.parents.HierarchicalParameterExtension">
                <property name="service" ref="hierarchicalParameterService" />
            </bean>
        </list>
    </property>
</bean>

<bean class="org.n52.web.ctrl.GeometriesController" parent="parameterController">
    <property name="parameterService" ref="geometriesService" />
</bean>

<bean class="org.n52.web.ctrl.DatasetController" parent="parameterController">
    <property name="parameterService" ref="datasetService" />
    <property name="metadataExtensions">
        <list merge="true">
            <bean class="org.n52.io.extension.RenderingHintsExtension" />
            <bean class="org.n52.io.extension.StatusIntervalsExtension" />
            <bean class="org.n52.io.extension.resulttime.ResultTimeExtension">
                <property name="service" ref="resultTimeService" />
            </bean>
            <!-- Using DatabaseMetadataExtension requires some preparation work. -->
            <!-- Have a look at the README.md at TBD -->
            <!--<bean class="org.n52.io.extension.metadata.DatabaseMetadataExtension" />-->
        </list>
    </property>
</bean>

<bean class="org.n52.web.ctrl.DataController">
    <property name="dataService" ref="datasetService" />
    <property name="datasetService" ref="datasetService" />
    <property name="preRenderingTask" ref="preRenderingJob" />
    <property name="requestIntervalRestriction" value="${request.interval.restriction}" />
</bean>
```


### Application Properties



### Extensions

### Static Service Entity
