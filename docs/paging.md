---
layout: page
title: Paging
permalink: /paging
---



## Overview

{:.n52-callout .n52-callout-info}
Paging is currently not supported for the Endpoint `/geometries`. All other Endpoints are supported.

The API offers basic Paging support using the Query Parameters `offset` and `limit`. Paging is automatically enabled if at least one of the parameters is present in the query. If only one parameter is provided, the missing parameters default value will be used.

### Parameter Values
The Parameter Values are subject to the following restrictions:

{:.table}
parameter     | MIN_VALUE  | MAX_VALUE  | default      |
--------------|------------|---------------------------|
`limit`       | 1          | 1000       | 100          | 
`offset`      | 0          | 2147483647 | 0            | 

{:.n52-callout .n52-callout-info}
Both `limit` and `offset` are integers and are programmatically limited to not exceed the value of 2147483647. If higher Numbers are provided the Request will fail and the API will throw an Error.

If an invalid `limit` (i.e. exceeding MAX_VALUE) is supplied, `limit` will default to the closest valid value (e.g. MIN_VALUE or MAX_VALUE). 
If no `limit` is supplied the default value will be used.

If no offset or an invalid `offset` is supplied, `offset` will default to its default value. 

### Response Headers
Paging Information is returned by the API in the Response Header. An Example of the returned Headers can be found below:

**Example Request URL**
```
http://example.com/api/stations?offset=20&limit=10
```

**Example Response Header [partial]:**
```
[...]
Link : <http://example.com/api/stations?offset=20&limit=10> rel="self"
Link : <http://example.com/api/stations?offset=30&limit=10> rel="next"
Link : <http://example.com/api/stations?offset=10&limit=10> rel="previous"
Link : <http://example.com/api/stations?offset=0&limit=10> rel="first"
Link : <http://example.com/api/stations?offset=70&limit=10> rel="last"
[...]
```
The Presence of the Links in the Response Header varies on the specific circumstances:

 - The Link to `self` is always present.
 - The Links to `first` and `last` are present if a valid offset was provided.
 - The Links to `next` and `previous` are present if the pages exist and a valid offset was provided.
