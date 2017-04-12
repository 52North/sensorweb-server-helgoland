select 
case
  when s.seriesid in (
    select min(seriesid) from series group by samplingpointid
  ) THEN 'master'::text
  ELSE 'slave'::text
  END as merge_role,
s.*,
sp.station stationid
from series s, samplingpoint sp
where s.samplingpointid = sp.samplingpointid
