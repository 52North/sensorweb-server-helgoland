SELECT distinct 
  c.procedure_id AS procedureid,
  c.procedure_identifier AS identifier,
  c.procedure_name AS name
from validation.common c
