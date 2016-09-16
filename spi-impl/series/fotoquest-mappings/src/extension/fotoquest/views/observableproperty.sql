SELECT distinct
  c.phenomenon_id AS observablepropertyid,
  c.phenomenon_identifier AS identifier,
  c.phenomenon_name AS name
FROM
  validation.common c