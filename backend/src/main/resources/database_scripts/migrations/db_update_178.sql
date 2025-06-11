insert into internal_patch (script, date, context, name) values (178, now(3), 'Remove unused user properties', 'db_update_178.sql');

ALTER TABLE user
	DROP COLUMN authkey,
	DROP COLUMN email_check_message,
	DROP COLUMN file_notification,
	DROP COLUMN scope_enrollment_notification,
	DROP COLUMN can_upload_data;
ALTER TABLE user_audit
	DROP COLUMN authkey,
	DROP COLUMN email_check_message,
	DROP COLUMN file_notification,
	DROP COLUMN scope_enrollment_notification,
	DROP COLUMN can_upload_data;
