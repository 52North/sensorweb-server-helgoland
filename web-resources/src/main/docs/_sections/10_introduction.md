---
layout: section
title: Introduction
permalink: /
---

The Series REST API provides an access layer to sensor data via RESTful Web binding 
with different output formats like json, pdf or png. It provides a well defined Sevice 
Provider Interface (SPI) which can be implemented by arbitrary backend services to make 
series data available via the API

The Series REST API provides a thin access layer to sensor and observation data via 
RESTful Web binding. In addition, it offers several IO functionalities e.g. 
  * prerendering of series data, 
  * generalization, 
  * overlaying of data from multiple series
  * conversion of raw data to other formats like pdf and png

Output formats for *stationary*, *mobile*, *insitu* and *remote* sensors are available, 
each filterable by metadata parameters. This enables clients to access the data via different 
approaches, e.g. to filter all series by phenomena first or by a special procedure.

{% comment %}

### Backwards Compatibility
The [Web API]({{site.baseurl}}/api.html) is backwards compatible to older implementation versions. Client 
developers are safe to start development while API providers upgrade to newer versions of the API.

However, [SPI implementors]({{site.baseurl}}/development.html) should take care of changes, though changes 
to the SPI interface will be kept to a minimum and communicated properly.


### Development
If you want to develop clients to consume series data refer to the [Web API reference]({{site.baseurl}}/api.html).
In case of being interested in how the API works and/or want to contribute check the 
[development section]({{site.baseurl}}/development.html).

TODO 
## Document status

What | When
-----|-----
Made several changes:
* Added endpoints /platforms, /datasets and /geometries
* Add side notes how previous endpoints relate to new features
* Add plural filter parameters to general query section
* Removed paging section as no implementation exists and led to confusion so far
* Reviewed document to rename Timeseries API to Series API | 2016-07-27

Added extras metadata. | 2015-12-14
Added generalization section. | 2015-07-06
Added station to general query parameters. | 2014-02-06
Create legacy section and add low-level SOS concepts. | 2013-12-16
{% endcomment %}