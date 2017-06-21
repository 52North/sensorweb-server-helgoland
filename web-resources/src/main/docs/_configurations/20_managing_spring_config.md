---
layout: section
title: Manage Spring Configuration
---

#### Managing Configuration

##### Separate Properties
It is helpful to separate all properties values from XML configuration for several
reasons. First, it may be tedious to find all single properties within verbose XML. 
However, more it also very important to keep sensitive information (like database
configuration) from the project itself.

If `local.configFile` parameter is present at startup-/build-time properties are read
from there, otherwise properties are read from default `WEB-INF/classes/application.properties`.

{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
A property replacement config example
</div>
```xml
<ctx:property-placeholder 
    <!-- ${user.home}/application.properties} when -Dlocal.configFile not defined -->
    location="${local.configFile:classpath:/application.properties}"
    ignore-resource-not-found="true" ignore-unresolvable="true" />
```
</div>

A placeholder can now be declared within Spring XML files via `${placeholder:default}`.
If present in the application properties file (your one or the default) it will be 
replaced, otherwise the given default will be used.


##### Separate Configuration Sections

To keep overview we can separate parts of the configuration files and include them
via file import, e.g. `<import resource="mvc.xml" />`.

