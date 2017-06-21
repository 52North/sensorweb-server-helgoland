---
layout: section
title: Value Types
permalink: /valuetypes
---

A dataset has a particular type which indicates the type of data values
which are being, or have been observed by a specific platform. This 
can be simple scalar types or more complex types like `profile` or
other `record` data being observed over time or along a track. 

The default dataset type is `quantity` which actually is a 
numeric observation over time. A resource can be filtered by 
dataset type(s).

### General
A simplest data value has a `timestamp` and a corresponding `value`.
Showing time intervals (`timestart` and `timeend`) can be activated
via query parameter `showTimeIntervals=true`.

`NULL` values means `no-data`.

While `timestamp` and `value` are mandatory (`timestamp` is mutually 
exclusive to `timestart` and `timeend`), other information can
be encoded as well (if present).

{:.table}
Member        | Optional      | Description
--------------|---------------|------------
`timestamp`   | no            | when value has been observed
`timestart`   | no            | when observation started (mutual exclusive to `timestamp`)
`timeend`     | no            | when observation ended (mutual exclusive to `timestamp`)
`value`       | no            | the actual (typed) data value
`geometry`    | yes           | relevant for `mobile` platforms
`parameters`  | yes           | additional parameters
`validTime`   | yes           | when the observation is being considered valid

Each data value might have different optionals set.

### Scalar data types
Scalar data types are single values (`double`, `count`, `boolean`, `double`, 
`text`). Observations of type `double` are identified by `quantity`. Other 
scalar types are denoted as is.

Depending on what members are available (see above table) the following output 
would be valid.


{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
Quantity Example (`stationary` platform)
</div>
```
{
  "quantity_1": {
    "values": [
      {
        "timestamp": 1353326400000,
        "value": 1,
        "validTime": {
          "start": 1381528800000,
          "end": 1386802800000
        },
        "bar": "alice",
        "foo": 3.4
      },
      {
        "timestamp": 1353326460000,
        "value": 1.1,
        "bar": "bob",
        "foo": 5
      }
    ]
  }
}
```
</div>


{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
Text example (observation interval, `stationary` platform)
</div>
```
{
  "text_1": {
    "values": [
      {
        "timestart": 1353326400000,
        "timesend": 135332605000,
        "value": "foo",
      },
      {
        "timestart": 1353326460000,
        "timeend": 1353326465000
        "value": "bar",
      }
    ]
  }
}
```
</div>


{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
Quantity example (`mobile` platform)
</div>
```
{
  "values": [
    {
      "timestamp": 1362610500000,
      "value": 261.494,
      "geometry": {
        "type": "Point",
        "coordinates": [
          3.020595,
          52.40741
        ]
      }
    },
    {
      "timestamp": 1362610520000,
      "value": 261.738,
      "geometry": {
        "type": "Point",
        "coordinates": [
          3.018087,
          52.408466
        ]
      }
    },
  ]
}
```
</div>

### Complex data types
Complex data types are compound values. Each compound value shall be seen as abstract
value type which has to be made concrete by data providers. In case of providing 
record data, the dataset should provide a concrete value type like `record+quantity` in 
case of `quantity` values. Other, completely customized, value types (like `my-type`) 
are also possible, but lead to less compatibility towards client (it has to know how to 
deal with that specific type). 

Please note that combining complex types with a complex type (e.g. `record+record) is not
supported by the API, just one subtype level.

Depending on what members are available (see above table) the following output 
would be valid.

#### Record
A record's `value` member is a map of key-valued objects. However, a `record` is abstract
and each implementation have to provide its own `valueType` within the `dataset` metadata.


{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
Typed Record Example: `text` (`stationary` platform)
</div>
```
{
  "record+text_309510107": {
    "values": [
      {
        "timestamp": 1437160475022,
        "value": {
          "east": "http://example.org/upload/201507/55a9541b20c50876106726.jpg",
          "south": "http://example.org/upload/201507/55a9541b26583054767586.jpg",
          "north": "http://example.org/upload/201507/55a9541b19209320230740.jpg",
          "spot": "http://example.org/upload/201507/55a9541b3782c557670845.jpg",
          "west": "http://example.org/upload/55a9541b30c2f165697520.jpg"
        }
      }
    ]
  }
}
```
</div>


{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
Customized Example: `fotoquest` (`stationary` platform)
</div>
[Fotoquest](https://github.com/52North/fotoquest-series-api) value types describe some 
kind of (referenced) media taken at a given location. It could also be described more 
generally as `record+geojson`. Background infos about the project can be found at [the
Fotoquest Homepage](http://fotoquest-go.org/).

```
{
  "fotoquest_309510107": {
    "values": [
      {
        "timestamp": 1437160475022,
        "value": {
          "east": {
            "type": "Feature",
            "id": "120644",
            "properties": {
              "id": "120644",
              "href": "http://example.org/upload/201507/55a9541b20c50876106726.jpg"
            },
            "geometry": {
              "type": "Point",
              "coordinates": [
                14.27377,
                48.30273
              ]
            }
          },
          "south": {
            "type": "Feature",
            "id": "120645",
            "properties": {
              "id": "120645",
              "href": "http://example.org/upload/201507/55a9541b26583054767586.jpg"
            },
            "geometry": {
              "type": "Point",
              "coordinates": [
                14.27375,
                48.30275
              ]
            }
          },
          "north": {
            "type": "Feature",
            "id": "120643",
            "properties": {
              "id": "120643",
              "href": "http://example.org/upload/201507/55a9541b19209320230740.jpg"
            },
            "geometry": {
              "type": "Point",
              "coordinates": [
                14.27378,
                48.30273
              ]
            }
          },
          "spot": {
            "type": "Feature",
            "id": "120647",
            "properties": {
              "id": "120647",
              "href": "http://example.org/upload/201507/55a9541b3782c557670845.jpg"
            },
            "geometry": {
              "type": "Point",
              "coordinates": [
                14.27375,
                48.30275
              ]
            }
          },
          "west": {
            "type": "Feature",
            "id": "120646",
            "properties": {
              "id": "120646",
              "href": "http://example.org/upload/55a9541b30c2f165697520.jpg"
            },
            "geometry": {
              "type": "Point",
              "coordinates": [
                14.27374,
                48.30275
              ]
            }
          }
        }
      }
    ]
  }
}
```
</div>

#### Profile
A value type which contain observations along a vertical axis (e.g. observations made on
different depth levels). The vertical unit is overridable within each vertical item.


{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
Vertical Example (`stationary` platform)
</div>
```
{
  "values": [
    {
      "timestamp": 1353330000000,
      "value": [
        {
          "vertical": 10,
          "value": 100
        },
        {
          "vertical": 20,
          "value": 200
        }
      ],
      "verticalUnit": "test_unit_4"
    },
    {
      "timestamp": 1353330540000,
      "value": [
        {
          "vertical": 12,
          "value": 400
        },
        {
          "vertical": 15,
          "value": 300
        }
      ],
      "verticalUnit": "test_unit_4"
    }
  ]
}
```
</div>