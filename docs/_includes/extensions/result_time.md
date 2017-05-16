
Next to the actual phenomenon time observation data may have a `resultTime` which 
indicates when the data became available. An example for such cases is forecast data
which may be (re-)calculated multiple times (optionally based on multiple models). 
Getting the right data values (belonging to a specific result time) a client can add 
the `resultTime=...` parameter when querying `datasets/<id>/data`.

In case of existing result times (which have to be different to the actual phenomenon 
time) are available to a client as `extra` data for a given dataset. 

Once activated no specific configuration is neccessary.