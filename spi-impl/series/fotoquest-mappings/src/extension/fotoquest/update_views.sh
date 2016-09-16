#!/bin/sh

OWNER=postgres
DATABASE=fotoquest
OUTPUT_FILE=update_views_out.sql

echo "Generate SQL ..."

echo "DROP MATERIALIZED VIEW IF EXISTS validation.common CASCADE;
" > $OUTPUT_FILE

insertView () {
    VIEW_NAME=$1
    FILE=$2
    echo "Adding view select statement for $VIEW_NAME (file: $FILE)"
    echo "CREATE MATERIALIZED VIEW validation.$VIEW_NAME AS" >> $OUTPUT_FILE
    FILE_CONTENT=`cat $FILE | sed 's/^\xEF\xBB\xBF//'`
    echo "$FILE_CONTENT" >>  $OUTPUT_FILE
    echo "WITH DATA;
    ALTER TABLE validation.$VIEW_NAME 
    OWNER TO $OWNER;
    
    " >> $OUTPUT_FILE
}

insertView "common" "common.sql"

views=($(ls views/*.sql))
for CURRENT_FILE in ${views[@]}
do
    VIEW_NAME=`echo $CURRENT_FILE | sed 's/views\/\(.*\).sql$/\1/'`
    insertView $VIEW_NAME $CURRENT_FILE
done

echo "Update views in database $DATABASE"

psql -U $OWNER -d $DATABASE -f $OUTPUT_FILE

echo "Done."
