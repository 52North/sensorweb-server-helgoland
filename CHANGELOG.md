# Changelog

## 2.x

### 2.0.0

#### Web API Extension
The platform resource is more generic and the following platform types are available now: 
*stationary*, *mobile*, *insitu* and *remote*. It includes the old *station* resource as a 
*stationary/insitu* typed platform.

Also, version 2.0.0 is more flexible in supporting observation types. These are accessible
from the new *series* endpoint. Before, only measurements were supported (as *timeseries*).

To not break old clients with the new concepts, the API offers new endpoints:

- `/ext/services/`
- `/ext/procedures/`
- `/ext/features/`
- `/ext/phenomena/`
- `/ext/offerings/`
- `/ext/categories/`
- `/series/`
- `/platforms/`

The following resources remain for backwards compatibility reasons. They provide 
*stationary/insitu* platforms only:

- `/services/`
- `/procedures/`
- `/features/`
- `/phenomena/`
- `/offerings/`
- `/categories/`
- `/timeseries/`
- `/stations/`

## 1.x

TBD