---
layout: section
title: License Extension
---

#### License Extension

This simple extension which reads a license file and makes the content available to clients
via `/<endpoint>/extras?fields=license` URL. This may be a full license text or a plain link.

##### Configuration Location

Place a text file calling `config-license.txt` under `WEB-INF/classes` and enable license by 
placing it under the `ParameterController`:

{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
Configuration Example
</div>
```xml
<bean class="org.n52.web.ctrl.ParameterController" id="parameterController" abstract="true">
    <property name="metadataExtensions">
        <list>
            <bean class="org.n52.io.response.extension.LicenseExtension" />
        </list>
    </property>
</bean>
```
</div>