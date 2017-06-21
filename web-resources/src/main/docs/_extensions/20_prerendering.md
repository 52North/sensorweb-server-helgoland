---
layout: section
title: Prerendering Extension
---

#### Prerendering

A data provider can configure datasets being pre-/rerendered regularly for given time intervals
(`lastMonth`, `lastWeek`, or `lastDay`). A client experience may be instant access rather than
to wait for that kind of fixed time intervals. Also, prerendered charts are easy to integrate via 
html tags without making requests to render charts dynamically.

On prerendering mode only single series charts are rendered (no chart overlay).

##### Configuration Location

{:.n52-callout .n52-callout-todo}
reference to JobScheduler configuration

Prerendering (styles, intervals, legend, etc.) can be configured for datasets having a particular 
phenomenon or each individually (overriding a possibly matching phenomenona config). A general 
configuration section applies to all further config unless overridden (within a `config` entry). 


{::options parse_block_html="true" /}
{: .n52-example-code}
<div>
<div class="n52-example-caption">
Example of a prerendering configuration
</div>
```json
{
  "generalConfig" : {
    "width": "800",
    "height": "500",
    "locale" : "de",
    "grid" : true,
    "generalize" : false
  },
  "phenomenonStyles": [
    {
     "id" : "3",
      "title" : "Station: %1$s, Phenomenon: %2$s [{ %8$s}] (w/ legend)",
      "chartQualifier" : "with_legend",
      "interval": [
        "lastWeek",
        "lastMonth"
      ],
      "config" : {
        "legend" : true
      },
      "style": {
        "chartType": "line",
        "properties": {
          "color": "#00ffff",
          "lineType": "solid",
          "width": 2
        }
      }
    }
  ],
  "datasetStyles": [
    {
      "id" : "16",
      "interval": [
        "lastWeek",
        "lastMonth"
      ],
      "style": {
        "chartType": "bar",
        "properties": {
          "interval": "byDay",
          "width": 0.8,
          "color": "#0000ff"
        }
      }
    }
  ]
}
```
</div>

{:.n52-callout .n52-callout-info}
`timeseriesStyles` and `seriesStyles` mean the same as `datasetStyles` and is kept 
for backwards compatibility reasons.

##### Placeholders

To add fine grained title configuration on a phenomenona group a 
[`String.format` template](https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax) 
can be used.

Choose one of the following

* `%1$s`: Platform label
* `%2$s`: Phenomenon label
* `%3$s`: Procedure label
* `%4$s`: Category label
* `%5$s`: Offering label
* `%6$s`: Feature label
* `%7$s`: Service label
* `%8$s`: Unit of Measure

Use the template for the title config within config file config-task-prerendering, e.g. `Platform: %1$s, Phenomenon: %2$s [{ %8$s}]`.

##### Accessing prerendered charts

{:.n52-callout .n52-callout-todo}
`datasets/<id>/images/<img_id>`
