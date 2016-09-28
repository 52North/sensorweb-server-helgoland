alter table series add identifier character varying(255) ;
update series set identifier='http://to.be/done/' || seriesid;
alter table series alter column identifier SET NOT NULL;
alter table series add constraint seriesidentifieruk UNIQUE(identifier);