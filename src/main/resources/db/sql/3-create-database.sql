/*
 * CREATE THE DATA_RETURNS DATABASE
 */
CREATE DATABASE ${db.rcr_api.name} OWNER ${db.rcr_api.username} ENCODING 'UTF8';
GRANT ALL PRIVILEGES ON DATABASE ${db.rcr_api.name} TO ${db.rcr_api.username};
