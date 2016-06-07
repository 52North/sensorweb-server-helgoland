# Description

The Series REST API provides a thin access layer to sensor and observation data via RESTful
Web binding. In addition, it offers several IO functionalities e.g. 
  * prerendering of series data, 
  * generalization, 
  * overlaying of data from multiple series
  * conversion of raw data to other formats like pdf and png

Output formats for *stationary*, *mobile*, *insitu* and *remote* sensors are available, each 
filterable by metadata parameters. This enables clients to access the data via different
approaches, e.g. to filter all series by phenomena first or by a special procedure.

Next to the Web API, a Service Provider Interface (SPI) defines the underlying interface for
data providing backends. With this, the API is flexible enough to be put ontop of arbitrary
data stores. Its modular design enables a seamless integration into existing Web applications.

Existing SPI implementations:

- SOS Proxy: Allows to aggregate multiple OGC SOS instances under one API
- Direct database access: Allows to define Hibernate mappings to serve data directly from a database

The following main frameworks are used to provide this API:

- [Spring MVC](https://angularjs.org/) 
- [JFreeChart](http://leafletjs.com/) 
