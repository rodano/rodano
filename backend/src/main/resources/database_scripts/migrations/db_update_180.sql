insert into internal_patch (script, date, context, name) values (180, now(3), 'Denormalize scope fk property on datasets and forms', 'db_update_180.sql');

UPDATE dataset INNER JOIN event ON dataset.event_fk = event.pk SET dataset.scope_fk = event.scope_fk WHERE dataset.scope_fk IS NULL;
UPDATE dataset_audit INNER JOIN event ON dataset_audit.event_fk = event.pk SET dataset_audit.scope_fk = event.scope_fk WHERE dataset_audit.scope_fk IS NULL;

UPDATE form INNER JOIN event ON form.event_fk = event.pk SET form.scope_fk = event.scope_fk WHERE form.scope_fk IS NULL;
UPDATE form_audit INNER JOIN event ON form_audit.event_fk = event.pk SET form_audit.scope_fk = event.scope_fk WHERE form_audit.scope_fk IS NULL;

ALTER TABLE dataset CHANGE COLUMN scope_fk scope_fk BIGINT(20) NOT NULL AFTER `deleted`;
ALTER TABLE dataset_audit CHANGE COLUMN scope_fk scope_fk BIGINT(20) NOT NULL AFTER `deleted`;

ALTER TABLE form CHANGE COLUMN scope_fk scope_fk BIGINT(20) NOT NULL AFTER `deleted`;
ALTER TABLE form_audit CHANGE COLUMN scope_fk scope_fk BIGINT(20) NOT NULL AFTER `deleted`;
