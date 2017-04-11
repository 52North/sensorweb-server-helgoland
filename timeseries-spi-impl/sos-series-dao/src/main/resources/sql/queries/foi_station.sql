SELECT DISTINCT st.stationid,
  st.identifier,
  st.codespace,
  st.name,
  st.codespacename,
  st.description,
  s.seriesid,
  sp.samplingpointid,
  foi.featureofinterestid,
  foi.geom
FROM series s,
  featureofinterest foi,
  samplingpoint sp,
  station st
WHERE s.featureofinterestid = foi.featureofinterestid
AND s.samplingpointid = sp.samplingpointid
AND sp.station = st.stationid
