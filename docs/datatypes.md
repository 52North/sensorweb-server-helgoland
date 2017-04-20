---
layout: page
title: Data Types
permalink: /datatypes
---

A dataset has a particular type which indicates the type of data
which is being, or has been observed by a specific platform. This 
can be simple scalar types (`count`, `boolean`,`double`, `text`)
or more complex types like `profile` or other `record` data being
observed over time or along a track. The default dataset type is 
`measurement` which actually is a numeric observation over time.

A resource can be filtered by dataset type(s).

## General
A simplest data value has a `timestamp` and a corresponding `value`.
However, when data values provide an observation interval (`timestart`
and `timeend`) `timestamp` is omitted.
A `null` value means `no-data`. 

While `timestamp` and `value` are mandatory (`timestamp` is mutually 
exclusive to `timestart` and `timeend` though), other information can
be encoded as well (if present).

{:.table}
Member        | Optional      | Description
--------------|---------------|------------
`timestamp`   | no            | when value has been observed
`timestart`   | no            | when observation started (mutual exclusive to `timestamp`)
`timeend`     | no            | when observation ended (mutual exclusive to `timestamp`)
`value`       | no            | the actual observation value
`geometry`    | yes           | relevant for `mobile` platforms
`parameters`  | yes           | additional parameters
`validTime`   | yes           | when the observation is being considered valid

## Scalar data types

### Measurement
Depending on what data type members are available (see above table which are 
optional) the following output would be valid. Note, that each data value might
have different optionals set.

**Measurement Example (`stationary` platform)**
```
{
  "measurement_1": {
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

**Text example (observation interval, `stationary` platform)**
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

**Measurement example (`mobile` platform)**
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
  

## Complex data types
tbd

### Record
tbd

### Profile
tbd
