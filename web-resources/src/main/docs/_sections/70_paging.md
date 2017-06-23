---
layout: section
title: Paging
permalink: /paging
---

{:.n52-callout .n52-callout-info}
Paging is currently not supported for the Endpoint `/geometries`. All other Endpoints are supported.

The API offers basic Paging support using the Query Parameters `offset` and `limit`. Paging is automatically enabled if at least one of the parameters is present in the query. If only one parameter is provided, the missing parameters default value will be used.

### Parameter Semantics

`limit` describes the maximum amount of Elements in one Page. Except for the last Page, all Pages will always be filled to this maximum capacity.

`offset` describes the Page Number, starting from zero.

{:.n52-callout .n52-callout-info}
It is **strongly advised** to only specify the `limit` Parameter in the client, and get the `offset` from the Response Headers returned by the API (see examples below). 

### Parameter Values
The Parameter Values are subject to the following restrictions:

{:.table}
parameter     | MIN_VALUE  | MAX_VALUE  | default      |
--------------|------------|---------------------------|
`limit`       | 1          | 1000000    | 10000        |
`offset`      | 0          | 2147483647 | 0            |

{:.n52-callout .n52-callout-info}
Both `limit` and `offset` are integers and are programmatically limited to not exceed the value of 2147483647. If higher Numbers are provided the Request will fail and the API will throw an Error.

If an invalid `limit` (i.e. exceeding MAX_VALUE) is supplied, `limit` will default to the closest valid value (e.g. MIN_VALUE or MAX_VALUE). 
If no `limit` is supplied the default value will be used.

If no offset or an invalid `offset` is supplied, `offset` will default to its default value. 

### Response Headers
Paging Information is returned by the API in the Response Header. An Example of the returned Headers can be found below:


{::options parse_block_html="true" /}
{: .n52-example-block}
<div>
<div class="n52-example-caption">
Example Request
</div>
```
http://example.com/api/stations?offset=2&limit=10
```

<div class="n52-example-caption">
Response Header [partial]:
</div>
```
[...]
Link : <http://example.com/api/stations?offset=2&limit=10> rel="self"
Link : <http://example.com/api/stations?offset=3&limit=10> rel="next"
Link : <http://example.com/api/stations?offset=1&limit=10> rel="previous"
Link : <http://example.com/api/stations?offset=0&limit=10> rel="first"
Link : <http://example.com/api/stations?offset=7&limit=10> rel="last"
[...]
```
</div>

The Presence of the Links in the Response Header varies on the specific circumstances:

 - The Link to `self` is always present.
 - The Links to `first` and `last` are present if a valid offset was provided.
 - The Links to `next` and `previous` are present if the pages exist and a valid offset was provided.
