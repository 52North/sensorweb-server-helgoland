SELECT 
  c.series_id AS seriesid,
  min(c.mediaitem_transaction) AS firsttimestamp,
  max(c.mediaitem_transaction) AS lasttimestamp
FROM validation.common c
  GROUP BY c.series_id