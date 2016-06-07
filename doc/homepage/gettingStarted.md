# Getting started and configuration

## Client development
The [Web API documentation](http://52north.github.io/series-rest-api) gives a detailed overview
on how to access the data provided by the API. Available I/O functions are described there, too, 
like generelization, chart rendering/overlay, etc.

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