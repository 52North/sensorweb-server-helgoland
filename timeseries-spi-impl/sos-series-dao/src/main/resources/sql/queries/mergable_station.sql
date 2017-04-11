 SELECT foi2.geom
FROM ( 
  SELECT 
  count(foi1.stationid) AS num,
  foi1.geom
  FROM (
    SELECT DISTINCT 
      st.stationid,
      foi.geom
    FROM series s,
    featureofinterest foi,
    samplingpoint sp,
    station st
    WHERE s.featureofinterestid = foi.featureofinterestid 
    AND s.samplingpointid = sp.samplingpointid 
    AND sp.station = st.stationid
  ) foi1
  GROUP BY foi1.geom
) foi2
WHERE foi2.num <> 1

