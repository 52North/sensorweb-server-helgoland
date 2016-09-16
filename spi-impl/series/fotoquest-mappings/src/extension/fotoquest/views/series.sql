select distinct
  c.series_id AS seriesid,
  c.foi_id AS featureofinterestid,
  c.phenomenon_id AS observablepropertyid,
  c.category_id AS categoryid,
  c.procedure_id AS procedureid,
  'F' AS deleted,
  'T' AS published,
  min(c.mediaitem_transaction) AS firsttimestamp,
  max(c.mediaitem_transaction) AS lasttimestamp
from validation.common c
  group by c.series_id, c.foi_id, c.phenomenon_id, c.category_id, c.procedure_id
