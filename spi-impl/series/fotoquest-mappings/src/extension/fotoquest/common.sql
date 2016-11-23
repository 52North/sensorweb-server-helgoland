SELECT
  fqi.id AS fotoquest_item_id,
  mi.id AS mediaitem_id,
  mi.ttransaction AS mediaitem_transaction,
  fqi."timestamp" AS fotoquest_item_timestamp,
  'http://www.geo-wiki.org/'::text || mi.url::text AS mediaitem_url,
  case 
	when v.direction = '' then 'unknown'
	when v.direction IS NULL then 'unknown'
	else v.direction
  end AS mediaitem_direction, -- treat as observation value attribute
  mloc.the_geom AS fotoquest_item_geom, -- treat as observation value attribute

  (s.id::text || fqi.userid::text)::bigint AS series_id, -- combination of sampleid and userid
  
  s.id AS foi_id, -- the sample --> stationary platform
  'http://www.connectingeo.net/fotoquest/features/'::text || s.id::text AS foi_identifier,
  ((('Sample '::text || s.id) || ' ('::text) || g.metadata::text) || ')'::text AS foi_name,
  sloc.the_geom AS foi_geom,

  fqi.userid AS procedure_id, -- the user
  'http://www.connectingeo.net/fotoquest/procedure/'::text || fqi.userid AS procedure_identifier,
  fqi.userid AS procedure_name,

  1 AS phenomenon_id, -- fixed
  'http://www.connectingeo.net/fotoquest/phenomena/1'::text AS phenomenon_identifier,
  'imagery'::text AS phenomenon_name,

  g.id AS category_id, -- observation context
  'http://www.connectingeo.net/fotoquest/category/'::text || g.id::text AS category_identifier,
  g.metadata::text AS category_name,
  
  fqi.score
  
FROM validation.fotoquest v
     LEFT JOIN marker.mediaitem mi ON v.mediaitemid = mi.id
     LEFT JOIN marker.location mloc ON mloc.id = mi.locationid
     LEFT JOIN marker."group" g ON g.id = mi.groupid
     
     LEFT JOIN marker.fotoquest_item fqi ON v.fotoquest_item_id = fqi.id
     LEFT JOIN sample.fotoquest s ON s.id = fqi.fotoquest_sample_id
     LEFT JOIN sample.location sloc ON sloc.id = s.locationid
WHERE (mi.groupid = ANY (ARRAY[9, 12, 13])) 
  AND s.active 
  AND fqi.visible
  AND mi.public
  AND NOT s.is_training_point
  AND (fqi.skip_reason = 'noskip' OR fqi.skip_reason ='' OR fqi.skip_reason IS NULL)
  AND mi.deleted IS NULL AND NOT st_equals(mloc.the_geom, st_geomfromtext('POINT (0 0)'::text, 4326))
