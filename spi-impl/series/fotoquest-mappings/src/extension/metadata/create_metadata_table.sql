CREATE TABLE series_metadata 
(
	metadata_id bigint NOT NULL,
	series_id bigint NOT NULL,
	field_name character varying(255) NOT NULL,
	field_type character varying(10) DEFAULT 'string',
	field_value text,
	last_update timestamp,
	CONSTRAINT seriespk PRIMARY KEY (metadata_id),
	CONSTRAINT seriesfk FOREIGN KEY (series_id)
		REFERENCES series (seriesid) MATCH SIMPLE
		ON UPDATE NO ACTION ON DELETE NO ACTION,
	CONSTRAINT metadataidentity UNIQUE (series_id, field_name),
	CONSTRAINT chk_type CHECK (field_type IN ('string','boolean','integer','double','text','json'))
)