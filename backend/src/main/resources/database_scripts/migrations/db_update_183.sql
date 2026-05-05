insert into internal_patch (script, date, context, name) values (183, now(3), 'Add index for search optimization', 'db_update_182.sql');

CREATE INDEX idx_field_value ON field(value(255));
CREATE INDEX idx_dataset_dataset_model_id ON dataset(dataset_model_id);
CREATE INDEX idx_field_field_model_id ON field(field_model_id);
