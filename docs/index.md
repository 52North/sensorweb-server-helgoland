---
layout: page
title: Series REST API Documentation
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

Next to the Web API, a Service Provider Interface (SPI) defines the underlying interface for 
data providing backends. With this, the API is flexible enough to be put ontop of arbitrary 
data stores. Its modular design enables a seamless integration into existing Web applications.

## Overview

![Big Picture]({{base_url}}images/big-picture.png)

{% comment %}
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