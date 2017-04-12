#!/bin/sh

OWNER=sos_user
DATABASE=sos_uba_v44
OUTPUT_FILE=create_views_out.sql
SCHEMA=public

echo "Generate SQL ..."

echo "

-- update/creation script for UBA views within 52°North SOS v4.3.11-UBA-1.0.3

" > $OUTPUT_FILE

insertView () {
    VIEW_NAME=$1
    FILE=$2
    echo "Adding view select statement for $VIEW_NAME (file: $FILE)"
    echo "CREATE MATERIALIZED VIEW $SCHEMA.$VIEW_NAME AS" >> $OUTPUT_FILE

    # remove byte order mark
    FILE_CONTENT=`cat $FILE | sed 's/^\xEF\xBB\xBF//'`
    echo "$FILE_CONTENT" >>  $OUTPUT_FILE
    echo "WITH DATA;

    " >> $OUTPUT_FILE
}

views=$(ls queries/*.sql)
for CURRENT_FILE in $views
do
    VIEW_NAME=`echo $CURRENT_FILE | sed 's/queries\/\(.*\).sql$/\1/'`
    echo "DROP MATERIALIZED VIEW IF EXISTS $VIEW_NAME CASCADE;
    " >> $OUTPUT_FILE
    insertView $VIEW_NAME $CURRENT_FILE
done

for CURRENT_FILE in $views
do
    VIEW_NAME=`echo $CURRENT_FILE | sed 's/queries\/\(.*\).sql$/\1/'`
    echo "ALTER TABLE  $SCHEMA.$VIEW_NAME
    OWNER TO $OWNER;" >> $OUTPUT_FILE
done

echo "Update views in database $DATABASE"

psql -U $OWNER -d $DATABASE -f $OUTPUT_FILE

echo "Done."
