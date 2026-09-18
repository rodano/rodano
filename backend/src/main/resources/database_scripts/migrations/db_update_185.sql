insert into internal_patch (script, date, context, name) values (185, now(3), 'Add latest message to workflow statuses', 'db_update_185.sql');

ALTER TABLE workflow_status ADD COLUMN last_message varchar(1000) default null AFTER trigger_message;
UPDATE workflow_status AS ws
SET last_message = (
	SELECT wsa.audit_context
	FROM workflow_status_audit AS wsa
	WHERE wsa.audit_object_fk = ws.pk
	ORDER BY wsa.audit_datetime DESC, wsa.pk DESC
	LIMIT 1
);
