insert into internal_patch (script, date, context, name) values (179, now(3), 'Remove country from user', 'db_update_179.sql');

ALTER TABLE user DROP COLUMN country_id;
ALTER TABLE user_audit DROP COLUMN country_id;
