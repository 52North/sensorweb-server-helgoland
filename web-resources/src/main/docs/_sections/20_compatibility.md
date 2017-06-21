---
layout: section
title: Backwards Compatibility
permalink: /compatibility
---

The Web API strives to be backwards compatible to older implementation versions. This includes
features and datastructure once they become officially introduced. Client developers are safe 
to start development although API providers may upgrade to newer versions in the meantime.

However, to add new features and functions will require to add new content to existing data 
structures (which won't break backwards compability, though). Also new endpoints may be introduced
over time like we did in `v2.0.0`.

### Version 2.0.0

#### New Endpoints
* `/datasets`: More generic series metadata which can have different value types
* `/platforms`: More generic metadata from where observations are made
* `/geometries`: Separated endpoint to query different types of geometries


#### Platforms vs. Stations

Since `v2.0` the following sensor platforms are supported:

* `stationary` sensor platforms are fixed at a given `site`
* `mobile` sensor platforms do observe data along a `track`
* `insitu` sensor platforms do observe data at the location the platform is currently at
* `remote` sensor platforms do observe data from distance (like cameras or satellites)

There are four possbile types of a platform can have: `stationary_insitu`, `mobile_insitu`, 
`stationary_remote` and `mobile_remote`. `insitu` platforms are associated with a `platformLocation` 
which can be either `site` or `track`. Additionally, `remote` platforms can reference observations to 
`observedGeometries`: Either a fix geometry (`static` or an observed geometry (`dynamic`) on its own 
(which may or may not vary over time e.g. sea water level via satellite).

By having more types filtering gets more important for clients to get the right data out from the API.
So, the `platform`'s geometries can now grow and get very huge (e.g. moving platforms with long tracks). 
To make them better to handle from clients geometries can be filtered under `/geometries` endpoint. 
Therefore additional query parameters now exist:

* `platformTypes` can be used to filter on all resources which ensures to only get those resources
 related to the given filter.
* `platformLocations` to filter insitu geometries (`site` or `track`)
* `observedGeometries` to filter remote geometries (`static` or `dynamic`)
* `geometryTypes` cto filter on different geometries (like `POINT` or `POLYGON`)

The former `Station` resource relates to the combination of a `stationary`, `insitu` 
platform (located at a 0-dimensional location geometry) where `quantity` datasets
are made. Retrieving the former output under `/platforms` endpoint, one can now filter via
`/platforms?platformTypes=stationary,insitu` or still can get the collection via 
`/stations`.

#### Timeseries vs. Datasets

Shorthand: a `dataset` is more flexible. While `timeseries` only support `quantity` values
(which are implicit) a `dataset` contains the `valueType` member which indicates the actual
type of data (e.g. different scalar values or even aggregate types).

Check out in more detail under the [Value Types section]({{site.baseurl}}/valuetypes.html).

#### data vs. getData
Naming of `getData` was a bit confusing within a RESTful API. That's the reason why `GET`ting
data for a dataset has been renamed to `data` (`/datasets/<id>/data`). To stay backwards 
compatible the old `getData` still exist under the old endpoint (`/timeseries/<id>/getData`).
