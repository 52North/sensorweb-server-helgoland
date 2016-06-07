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

### Date formatting

### Extra information
