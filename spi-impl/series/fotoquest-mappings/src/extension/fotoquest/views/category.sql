select distinct
  c.category_id AS categoryid,
  c.category_identifier AS identifier,
  c.category_name AS name
from validation.common c