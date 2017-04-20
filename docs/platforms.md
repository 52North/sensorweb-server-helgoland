---
layout: page
title: Platforms
permalink: /platforms
---

Since `v2.0` the following sensor platforms are supported:

* `stationary` sensor platforms
* `mobile` sensor platforms
* `insitu` sensor platforms
* `remote` sensor platforms

The former Stations resource collection relates to the combination 
of `stationary` and `insitu` platforms (located at a 0-dimensional
location geometry) which do observe `measurement` datasets. 

So retrieving getting the former output under platforms endpoint one 
can filter via`/platforms?platformTypes=stationary,insitu` or get the 
collection via `/stations`. The query parameter `platformTypes` can be 
used to filter on all resources which ensures to only get those resources 
related to the given filter.