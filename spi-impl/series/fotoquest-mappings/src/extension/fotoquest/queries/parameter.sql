SELECT
  mi.id AS observationid,
  mi.ttransaction AS timestamp,
  'http://www.geo-wiki.org/'::text || mi.url::text AS value,
  mloc.the_geom AS geom
FROM validation.fotoquest v
     LEFT JOIN marker.mediaitem mi ON v.mediaitemid = mi.id
     LEFT JOIN marker.location mloc ON mloc.id = mi.locationid
     LEFT JOIN marker.group g ON g.id = mi.groupid
     
     LEFT JOIN marker.fotoquest_item fqi ON v.fotoquest_item_id = fqi.id
     LEFT JOIN sample.fotoquest s ON s.id = fqi.fotoquest_sample_id
     LEFT JOIN sample.location sloc ON sloc.id = s.locationid
  WHERE (mi.groupid = ANY (ARRAY[9, 12, 13])) 
  AND s.active
  AND mi.deleted IS NULL
  AND NOT st_equals(mloc.the_geom, st_geomfromtext('POINT (0 0)'::text, 4326))