SELECT DISTINCT 
  c.foi_id AS featureofinterestid,
  c.foi_identifier AS identifier,
  c.foi_name AS name,
  c.foi_geom AS geom
from 
  validation.common c