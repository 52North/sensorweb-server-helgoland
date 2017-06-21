---
layout: section
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

### Configuration Locations
* `WEB-INF/classes/config-general.properties`
* `WEB-INF/spring/`
* `WEB-INF/classes/application.properties`
* `WEB-INF/web.xml` (cors etc.)


<div>
{% for config in site.configurations %}
    {{config.content | markdownify}}
{% endfor %}
</div>
