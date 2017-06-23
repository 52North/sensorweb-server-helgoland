---
layout: section
title: License Extension
---

### Status Intervals

The data provider knows best how to interpret available data and can add domain specific information 
which may help to interpret data values of a particular dataset.Clients are then able to render data 
values (e.g. when rendering `lastValue`s on a map) by comparing against those intervals. This gives a 
user hints what data values actually mean and helps to avoid misunderstandings.

The status intervals are available to a client as `extra` data for a given dataset. 

#### Configuration Location

Status intervals can be configured for datasets having a particular phenomenon or each individually 
(overriding a possibly matching phenomenona config). Each entry provides a title, upper and lower
limit and some color hint a client may use to render properly.

{::options parse_block_html="true" /}
{: .n52-example-block}
<div>
<div class="btn n52-example-caption n52-example-toggler" type="button" data-toggle="button">
Example of a status intervals configuration
</div>
```json
{
  "phenomenonIntervals": {
    "1": {
      "statusIntervals": {
        "90-100 Percent": {
          "upper" : 100.0,
          "lower" : 90.0,
          "color" : "#0000FF"
        },
        "80-90 Percent": {
          "upper" : 90.0,
          "lower" : 80.0,
          "color" : "#1C00E2"
        },
        "70-80 Percent": {
          "upper" : 80.0,
          "lower" : 70.0,
          "color" : "#3800C6"
        },
        "60-70 Percent": {
          "upper" : 70.0,
          "lower" : 60.0,
          "color" : "#5500AA"
        },
        "50-60 Percent": {
          "upper" : 60.0,
          "lower" : 50.0,
          "color" : "#71008D"
        },
        "40-50 Percent": {
          "upper" : 50.0,
          "lower" : 40.0,
          "color" : "#8D0071"
        },
        "30-40 Percent": {
          "upper" : 40.0,
          "lower" : 30.0,
          "color" : "#AA0055"
        },
        "20-30 Percent": {
          "upper" : 30.0,
          "lower" : 20.0,
          "color" : "#C60038"
        },
        "10-20 Percent": {
          "upper" : 20.0,
          "lower" : 10.0,
          "color" : "#E2001C"
        },
        "0-10 Percent": {
          "upper" : 10.0,
          "lower" : 0.0,
          "color" : "#FF0000"
        }
      }
    }
  },
  "datasetIntervals": {
    "10": {
      "statusIntervals": {
        ">20": {
          "lower" : 20.0,
          "color" : "#FF0000"
        },
        "15-20": {
          "upper" : 20.0,
          "lower" : 15.0,
          "color" : "#BF003F"
        },
        "10-15": {
          "upper" : 15.0,
          "lower" : 10.0,
          "color" : "#7F007F"
        },
        "5-10": {
          "upper" : 10.0,
          "lower" : 5.0,
          "color" : "#3F00BF"
        },
        "0-5": {
          "upper" : 5.0,
          "lower" : 0.0,
          "color" : "#0000FF"
        }
      }
    }
  }
}
```
</div>

{:.n52-callout .n52-callout-info}
`timeseriesStyles` and `seriesStyles` mean the same as `datasetStyles` and is kept 
for backwards compatibility reasons.