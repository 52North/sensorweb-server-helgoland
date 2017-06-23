---
layout: section
title: Extensions
permalink: /extensions
---

{:.n52-callout .n52-callout-todo}
section under revision/update

{:.n52-callout .n52-callout-info}
Extensions are configured for a controller instance via `metadataExtensions` property list. For
details have a look at [Configuration section]({{site.baseurl}}/configuration.html) section.

<div>
{% for extension in site.extensions %}
    {{extension.content | markdownify}}
{% endfor %}
</div>