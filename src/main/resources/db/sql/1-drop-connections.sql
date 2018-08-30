/*
 * DROP ALL ACTIVE CONNECTIONS TO THE DATABASE
 *
 * This script forcibly drops connections to the database so that the database itself can be dropped
 */
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = '${db.rcr_api.name}'
		AND pid <> pg_backend_pid();
