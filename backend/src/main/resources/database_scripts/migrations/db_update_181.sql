insert into internal_patch (script, date, context, name) values (181, now(3), 'Delete profile id column on workflows', 'db_update_181.sql');

ALTER TABLE workflow_status DROP COLUMN profile_id;
ALTER TABLE workflow_status_audit DROP COLUMN profile_id;
