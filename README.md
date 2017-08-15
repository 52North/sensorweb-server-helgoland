# 52n Series REST API

<img style="width: 60%; height: 60%" alt="series-rest-api architecture overview" src="https://52north.github.io/series-rest-api/img/big-picture.png">

## Description

**Thin Web binding API to access timeseries data.**

_The Series REST API provides an access layer to sensor data via RESTful Web binding with different output formats like json, pdf or png. It provides a well defined Sevice Provider Interface (SPI) which can be implemented by arbitrary backend services to make series data available via the API_

The Series REST API provides a thin access layer to sensor and observation data via RESTful Web binding. In addition, it offers several IO functionalities e.g. 
  * prerendering of series data, 
  * generalization, 
  * overlaying of data from multiple series
  * conversion of raw data to other formats like pdf and png

Output formats for *stationary*, *mobile*, *insitu* and *remote* sensors are available, each filterable by metadata parameters. This enables clients to access the data via different approaches, e.g. to filter all series by phenomena first or by a special procedure.

Next to the Web API, a Service Provider Interface (SPI) defines the underlying interface for data providing backends. With this, the API is flexible enough to be put ontop of arbitrary data stores. Its modular design enables a seamless integration into existing Web applications.

The following main frameworks are used to provide this API:

- [Spring MVC](https://spring.org/) 
- [JFreeChart](http://www.jfree.org/jfreechart/) 


### Existing SPI implementations:

- [SOS Proxy](https://github.com/52North/series-sos-proxy): Allows to aggregate multiple OGC SOS instances under one API
- [Direct database access](https://github.com/52North/dao-series-api): Allows to define Hibernate mappings to serve data directly from a database
- [Fotoquest database access](https://github.com/52North/fotoquest-series-api): tbd
- [DWD Weather Alerts](https://github.com/52North/dwd-series-api): tbd

## References
* The [SOS proxy demo](http://sensorweb.demo.52north.org/sensorwebclient-webapp-stable/api/v1/) provides an SPI implementation which accesses data from multiple Sensor Observation Services [(OGC SOS)](http://opengeospatial.org/standards/sos).
* The [Web application integration demo](sensorweb.demo.52north.org/52n-sos-webapp/api/v1/) gives an integration demo which directly accesses the data from a database.
* The [52°North Helgoland Web Client](https://githum.com/52North/helgoland) consumes one or multiple instances of the REST API. A demo can be found under the http://sensorweb.demo.52north.org/client/#/

## License

The client is published under the [GNU General Public License v2 (GPLv2)](http://www.gnu.org/licenses/gpl-2.0.html).

## Changelog
- https://github.com/52North/series-rest-api/blob/develop/CHANGELOG.md
- for detailed infos check https://github.com/52North/series-rest-api/pulls?q=is%3Apr+is%3Aclosed

## Contributing
We try to follow [the GitFlow model](http://nvie.com/posts/a-successful-git-branching-model/), 
although we do not see it that strict. 

However, make sure to do pull requests for features, hotfixes, etc. by
making use of GitFlow. Altlassian provides [a good overview]
(https://www.atlassian.com/de/git/workflows#!workflow-gitflow). of the 
most common workflows.

## Contact
Henning Bredel (h.bredel@52north.org)

## Quick Start

### Client development
The [Web API documentation](http://52north.github.io/series-rest-api) gives a detailed overview on how to access the data provided by the API. Available I/O functions are described there, too, like generelization, chart rendering/overlay, etc.

~~The API documentation is in the 52°North wiki:
https://wiki.52north.org/bin/view/SensorWeb/SensorWebClientRESTInterface~~

### API Configuration
How to provide a custom SPI implementation is beyond this section. See (...TBD...) to get detailed 
information on this.

#### Logging

#### Generalizer
In file `WEB-INF/classes/config-general.json` add 

```
"generalizer": {
    "defaultGeneralizer": "lttb",
    "noDataGapThreshold": 5
}
```

The parameters are described on the official [Web API documentation](http://52north.github.io/series-rest-api).

#### Prerendering
Prerendering is supported for measurement data.

Prerendering configuration is a task which can be run regularly by a scheduler. Configuration is done as a 
`PreRenderingJob` bean. Checkout `WEB-INF/spring/spi-impl-dao_tasks.xml` how to set up a prerendering
job. The actual rendering configuration for each series/phenomenon can be referenced within the job bean.

Rendering configuration tells how to render the actual series information. It comprises a `phenomenonStyles`
section (valid for a set of series for a given phenomenon) and a `seriesStyles` section (which actually 
overrides a phenomenon style of a specific series) Each section can override parameters made in the `generalConfig`.

Only those series are prerendered having a match either in `phenomenonStyles` or `seriesStyles`. 

#### Date formatting
In file `WEB-INF/spring/config-general.json` you can set `timeformat` Parameter. Please checkout the 
[Java SimpleDateFormat rules](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) how the 
format has to look like.

#### Rendering Hints
TBD

#### Status Intervals
TBD

#### Metadata from a Database
TBD

## Credits

The development of the 52°North REST-API implementation was supported by several organizations and projects. Among other we would like to thank the following organisations and project

| Project/Logo | Description |
| :-------------: | :------------- |
| <a target="_blank" href="http://www.nexosproject.eu/"><img alt="NeXOS - Next generation, Cost-effective, Compact, Multifunctional Web Enabled Ocean Sensor Systems Empowering Marine, Maritime and Fisheries Management" align="middle" width="172" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/logo_nexos.png" /></a> | The development of this version of the 52&deg;North REST-API was supported by the <a target="_blank" href="http://cordis.europa.eu/fp7/home_en.html">European FP7</a> research project <a target="_blank" href="http://www.nexosproject.eu/">NeXOS</a> (co-funded by the European Commission under the grant agreement n&deg;614102) |
| <a target="_blank" href="http://www.fixo3.eu/"><img alt="FixO3 - Fixed-Point Open Ocean Observatories" align="middle" width="172" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/logo_fixo3.png" /></a> | The development of this version of the 52&deg;North REST-API was supported by the <a target="_blank" href="http://cordis.europa.eu/fp7/home_en.html">European FP7</a> research project <a target="_blank" href="http://www.fixo3.eu/">FixO3</a> (co-funded by the European Commission under the grant agreement n&deg;312463) |
| <a target="_blank" href="http://www.odip.org"><img alt="ODIP II - Ocean Data Interoperability Platform" align="middle" width="100" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/odip-logo.png"/></a> | The development of this version of the 52&deg;North REST-API was supported by the <a target="_blank" href="https://ec.europa.eu/programmes/horizon2020/">Horizon 2020</a> research project <a target="_blank" href="http://www.odip.org/">ODIP II</a> (co-funded by the European Commission under the grant agreement n&deg;654310) |
| <a target="_blank" href="https://www.seadatanet.org/About-us/SeaDataCloud/"><img alt="SeaDataCloud" align="middle" width="156" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/logo_seadatanet.png"/></a> | The development of this version of the 52&deg;North REST-API was supported by the <a target="_blank" href="https://ec.europa.eu/programmes/horizon2020/">Horizon 2020</a> research project <a target="_blank" href="https://www.seadatanet.org/About-us/SeaDataCloud/">SeaDataCloud</a> (co-funded by the European Commission under the grant agreement n&deg;730960) |
| <a target="_blank" href="http://www.wupperverband.de"><img alt="Wupperverband" align="middle" width="196" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/logo_wv.jpg"/></a> | The <a target="_blank" href="http://www.wupperverband.de/">Wupperverband</a> for water, humans and the environment (Germany) |
| <a target="_blank" href="http://www.irceline.be/en"><img alt="Belgian Interregional Environment Agency (IRCEL - CELINE)" align="middle" width="130" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/logo_irceline_no_text.png"/></a> | The <a href="http://www.irceline.be/en" target="_blank" title="Belgian Interregional Environment Agency (IRCEL - CELINE)">Belgian Interregional Environment Agency (IRCEL - CELINE)</a> is active in the domain of air quality (modelling, forecasts, informing the public on the state of their air quality, e-reporting to the EU under the air quality directives, participating in scientific research on air quality, etc.). IRCEL &mdash; CELINE is a permanent cooperation between three regional environment agencies: <a href="http://www.awac.be/" title="Agence wallonne de l&#39Air et du Climat (AWAC)">Agence wallonne de l'Air et du Climat (AWAC)</a>, <a href="http://www.ibgebim.be/" title="Bruxelles Environnement - Leefmilieu Brussel">Bruxelles Environnement - Leefmilieu Brussel</a> and <a href="http://www.vmm.be/" title="Vlaamse Milieumaatschappij (VMM)">Vlaamse Milieumaatschappij (VMM)</a>. |
| <a target="_blank" href="http://www.geowow.eu/"><img alt="GEOWOW - GEOSS interoperability for Weather, Ocean and Water" align="middle" width="172" src="https://raw.githubusercontent.com/52North/sos/develop/spring/views/src/main/webapp/static/images/funding/logo_geowow.png"/></a> | The development of this version of the 52&deg;North SOS was supported by the <a target="_blank" href="http://cordis.europa.eu/fp7/home_en.html">European FP7</a> research project <a href="http://www.geowow.eu/" title="GEOWOW">GEOWOW</a> (co-funded by the European Commission under the grant agreement n&deg;282915) |
