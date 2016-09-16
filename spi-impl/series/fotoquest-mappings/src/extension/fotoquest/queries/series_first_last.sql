SELECT 
  s.id AS seriesid,
  min(mi.ttransaction) AS firsttimestamp,
  max(mi.ttransaction) AS lasttimestamp
FROM validation.fotoquest v
     LEFT JOIN marker.mediaitem mi ON v.mediaitemid = mi.id
     LEFT JOIN marker.location mloc ON mloc.id = mi.locationid
     
     LEFT JOIN marker.fotoquest_item fqi ON v.fotoquest_item_id = fqi.id
     LEFT JOIN sample.fotoquest s ON s.id = fqi.fotoquest_sample_id
  WHERE (mi.groupid = ANY (ARRAY[9, 12, 13])) 
  AND s.active 
  AND mi.deleted IS NULL 
  AND mi.public 
  AND NOT s.is_training_point 
  AND (fqi.skip_reason = 'noskip' OR fqi.skip_reason ='' OR fqi.skip_reason IS NULL)
  AND NOT st_equals(mloc.the_geom, st_geomfromtext('POINT (0 0)'::text, 4326))
  GROUP BY s.id