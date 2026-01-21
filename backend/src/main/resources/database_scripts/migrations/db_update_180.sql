insert into internal_patch (script, date, context, name) values (180, now(3), 'Add index for search optimization', 'db_update_180.sql');

CREATE INDEX idx_scope_relation_parent_start ON scope_relation(parent_fk, start_date);
CREATE INDEX idx_scope_relation_parent_end ON scope_relation(parent_fk, end_date);

CREATE INDEX idx_workflow_status_scope_workflow ON workflow_status(scope_fk, workflow_id);
CREATE INDEX idx_workflow_status_filter ON workflow_status(workflow_id, deleted, state_id, scope_fk);

CREATE INDEX idx_dataset_scope_model ON dataset(scope_fk, dataset_model_id);

CREATE INDEX idx_field_dataset_model ON field(dataset_fk, field_model_id);
CREATE INDEX idx_field_value ON field(value);