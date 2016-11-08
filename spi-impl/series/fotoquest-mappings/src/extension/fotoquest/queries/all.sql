SELECT
  DISTINCT s.id AS seriesid,
  s.id AS featureofinterestid,
  1 AS observablepropertyid, -- fixed to 'imagery'
  1 AS procedureid, -- fixed to 'ground truth or similar'
  g.id AS categoryid,
  'F' AS deleted,
  'T' AS published,
  flv.firsttimestamp,
  flv.lasttimestamp
FROM validation.fotoquest v
     LEFT JOIN marker.mediaitem mi ON v.mediaitemid = mi.id
     LEFT JOIN marker.location mloc ON mloc.id = mi.locationid
     LEFT JOIN marker.group g ON g.id = mi.groupid
     
     LEFT JOIN marker.fotoquest_item fqi ON v.fotoquest_item_id = fqi.id
     LEFT JOIN sample.fotoquest s ON s.id = fqi.fotoquest_sample_id
     LEFT JOIN sample.location sloc ON sloc.id = s.locationid
     LEFT JOIN validation.first_last_values flv ON flv.seriesid = s.id
  WHERE (mi.groupid = ANY (ARRAY[9, 12, 13])) 
  AND s.active 
  AND mi.deleted IS NULL 
  AND mi.public 
  AND NOT s.is_training_point 
  AND (fqi.skip_reason = 'noskip' OR fqi.skip_reason ='' OR fqi.skip_reason IS NULL)
  AND NOT st_equals(mloc.the_geom, st_geomfromtext('POINT (0 0)'::text, 4326))