SELECT DISTINCT st1.stationid,
  m.masterref,
  CASE
    WHEN st1.stationid = m.masterref THEN 'master'::text
    ELSE 'slave'::text
  END AS merge_role,
  st1.identifier,
  st1.codespace,
  st1.name,
  st1.codespacename,
  st1.description,
  st1.geom
FROM foi_station st1
JOIN (
  SELECT max(st2.stationid) AS masterref, st2.geom
  FROM foi_station st2
  GROUP BY st2.geom
) m ON st1.geom = m.geom
