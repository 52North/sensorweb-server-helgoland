# 52n Series REST API

## Description

**Thin Web binding API to access timeseries data.**

*The Series REST API provides an access layer to sensor data via RESTful Web binding with different output formats like json, pdf or png. It provides a well defined Sevice Provider Interface (SPI) which can be implemented by arbitrary backend services to serve series data via the *

The Series REST API provides a thin access layer to sensor and observation data via RESTful Web binding. In addition, it offers several IO functionalities e.g. 
  * prerendering of series data, 
  * generalization, 
  * overlaying of data from multiple series
  * conversion of raw data to other formats like pdf and png

Output formats for *stationary*, *mobile*, *insitu* and *remote* sensors are available, each filterable by metadata parameters. This enables clients to access the data via different approaches, e.g. to filter all series by phenomena first or by a special procedure.

Next to the Web API, a Service Provider Interface (SPI) defines the underlying interface for data providing backends. With this, the API is flexible enough to be put ontop of arbitrary data stores. Its modular design enables a seamless integration into existing Web applications.

Existing SPI implementations:

- SOS Proxy: Allows to aggregate multiple OGC SOS instances under one API
- Direct database access: Allows to define Hibernate mappings to serve data directly from a database

The following main frameworks are used to provide this API:

- [Spring MVC](https://spring.org/) 
- [JFreeChart](http://www.jfree.org/jfreechart/) 

## License

The client is published under the [GNU General Public License v2 (GPLv2)](http://www.gnu.org/licenses/gpl-2.0.html).

## Demo

* The [SOS proxy demo](http://sensorweb.demo.52north.org/sensorwebclient-webapp-stable/api/v1/) provides an SPI implementation which accesses data from multiple Sensor Observation Services [(OGC SOS)](http://opengeospatial.org/standards/sos).
* The [Web application integration demo](sensorweb.demo.52north.org/52n-sos-webapp/api/v1/) gives an integration demo which directly accesses the data from a database.

## Contributing
We try to follow [the GitFlow model](http://nvie.com/posts/a-successful-git-branching-model/), 
although we do not see it that strict. 

However, make sure to do pull requests for features, hotfixes, etc. by
making use of GitFlow. Altlassian provides [a good overview]
(https://www.atlassian.com/de/git/workflows#!workflow-gitflow). of the 
most common workflows.

# Getting started and configuration

## Client development
The [Web API documentation](http://52north.github.io/series-rest-api) gives a detailed overview on how to access the data provided by the API. Available I/O functions are described there, too, like generelization, chart rendering/overlay, etc.

~~The API documentation is in the 52°North wiki:
https://wiki.52north.org/bin/view/SensorWeb/SensorWebClientRESTInterface~~

## API Configuration
How to provide a custom SPI implementation is beyond this section. See (...TBD...) to get detailed 
information on this.

### Logging

### Generalizer
In file `WEB-INF/spring/config-general.json` add 

```
"generalizer": {
    "defaultGeneralizer": "lttb",
    "noDataGapThreshold": 5
}
```

The parameters are described on the official [Web API documentation](http://52north.github.io/series-rest-api).

### Prerendering
Prerendering is supported for measurement data.

Prerendering configuration is a task which can be run regularly by a scheduler. Configuration is done as a 
`PreRenderingJob` bean. Checkout `WEB-INF/spring/spi-impl-dao_tasks.xml` how to set up a prerendering
job. The actual rendering configuration for each series/phenomenon can be referenced within the job bean.

Rendering configuration tells how to render the actual series information. It comprises a `phenomenonStyles`
section (valid for a set of series for a given phenomenon) and a `seriesStyles` section (which actually 
overrides a phenomenon style of a specific series) Each section can override parameters made in the `generalConfig`.

Only those series are prerendered having a match either in `phenomenonStyles` or `seriesStyles`. 

### Date formatting
In file `WEB-INF/spring/config-general.json` you can set `timeformat` Parameter. Please checkout the 
[Java SimpleDateFormat rules](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) how the 
format has to look like.

### Extra information

#### Rendering Hints
TBD

#### Status Intervals
TBD

#### Metadata from a Database

