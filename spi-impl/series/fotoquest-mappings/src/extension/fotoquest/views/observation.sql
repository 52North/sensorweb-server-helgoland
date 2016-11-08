SELECT distinct
  c.fotoquest_item_id AS observationid,
  c.series_id AS seriesid,
  c.fotoquest_item_timestamp AS resulttime
  --st_astext(c.fotoquest_item_geom) AS geom
  --c.mediaitem_url AS value
  --c.mediaitem_direction AS direction
from validation.common c