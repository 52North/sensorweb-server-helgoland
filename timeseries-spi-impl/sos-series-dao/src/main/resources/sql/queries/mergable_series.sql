select counts.samplingpointid from (
  select samplingpointid,count(seriesid) num from series group by samplingpointid
) counts
where counts.num <> 1
