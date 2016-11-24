select distinct
  c.series_id AS seriesid,
  c.foi_id AS featureofinterestid,
  c.phenomenon_id AS observablepropertyid,
  c.category_id AS categoryid,
  c.procedure_id AS procedureid,
  'F' AS deleted,
  'T' AS published,
  min(c.fotoquest_item_timestamp) AS firsttimestamp,
  max(c.fotoquest_item_timestamp) AS lasttimestamp
from validation.common c
  group by c.series_id, c.foi_id, c.phenomenon_id, c.category_id, c.procedure_id