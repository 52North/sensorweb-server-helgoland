select
  c.mediaitem_id AS mediaitemid,
  c.fotoquest_item_id AS observationid,
  c.fotoquest_item_geom AS geom,
  c.mediaitem_url AS value,
  c.series_id AS seriesid,
  c.mediaitem_direction AS direction
from validation.common c
  LEFT JOIN marker.mediaitem mi ON c.mediaitem_id = mi.id