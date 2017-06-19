---
layout: page
title: Manage Spring Configuration
permalink: /configuration/manage_spring_config
---

## Managing Configuration

### Separate Properties
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


### Separate Configuration Sections

To keep overview we can separate parts of the configuration files and include them
via file import, e.g. `<import resource="mvc.xml" />`.

