-- list users who contributed observations on a sample multiple times
select t.* from (
	select
		count(c.fotoquest_item_id) as observations_made,
		count(c.foi_id) as samples_visited,
		c.procedure_id as user
	from validation.common c
		group by c.procedure_id
) as t
--where observations_made != samples_visited -- obvisously no one :)