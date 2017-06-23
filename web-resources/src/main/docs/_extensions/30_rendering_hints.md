---
layout: section
title: Rendering Hints Extension
---

### Rendering Hints

The Series API provides I/O mechanisms to render charts so that clients may directly request
data via `accept=image/png` HTTP header. However, styles are chosen either randomly or by 
passing the `style=...` parameter. As the data provider should know best what style fit best
to a particular dataset, it can be configured as rendering hints. 


The rendering hints are available to a client as `extra` data for a given dataset. 

#### Configuration Location

Status intervals can be configured for datasets having a particular phenomenon or each individually 
(overriding a possibly matching phenomenona config). Each entry provides a title, upper and lower
limit and some color hint a client may use to render properly.


{::options parse_block_html="true" /}
{: .n52-example-block}
<div>
<div class="btn n52-example-caption n52-example-toggler" type="button" data-toggle="button">
Example of a rendering hints configuration
</div>
```json
{
  "phenomenonStyles": {
    "3": {
      "style": {
        "chartType": "bar",
        "properties": {
          "interval": "byHour",
          "width": 0.8,
          "color": "#0000ff"
        }
      }
    }
  },
  "datasetStyles": {
    "2": {
      "style": {
        "chartType": "line",
        "properties": {
          "lineType": "solid",
          "width": 1,
          "color": "#1E90FF"
        },
        "referenceValueStyleProperties": {
          "278" : {
            "chartType": "line",
            "properties": {
              "lineType": "solid",
              "width": 1,
              "color": "#1E90FF"
            }
          }
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