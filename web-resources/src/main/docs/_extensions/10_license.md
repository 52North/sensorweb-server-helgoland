---
layout: section
title: License Extension
---

### License

This simple extension which reads a license file and makes the content available to clients
via `/<endpoint>/extras?fields=license` URL. This may be a full license text or a plain link.

{::options parse_block_html="true" /}
{: .n52-example-block}
<div>
<div class="btn n52-example-caption n52-example-toggler active" type="button" data-toggle="button">
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


#### Configuration File

Place a text file calling `config-license.txt` under `WEB-INF/classes` and enable license by 
placing it under the `ParameterController`:
