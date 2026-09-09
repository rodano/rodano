/*
 * Baseline version of the database structure.
 * These rows record which migrations are already reflected in the structure scripts.
 * They are applied when the structure is created and re-applied after the database is truncated.
 */
insert into internal_patch (script, date, context, name) values (179, now(3), 'Remove country from user', 'db_update_179.sql');
insert into internal_patch (script, date, context, name) values (180, now(3), 'Denormalize scope fk property on datasets and forms', 'db_update_180.sql');
insert into internal_patch (script, date, context, name) values (181, now(3), 'Delete profile id column on workflow statuses', 'db_update_181.sql');
insert into internal_patch (script, date, context, name) values (182, now(3), 'Rename deleted column to removed', 'db_update_182.sql');
insert into internal_patch (script, date, context, name) values (183, now(3), 'Add index for search optimization', 'db_update_183.sql');
insert into internal_patch (script, date, context, name) values (184, now(3), 'Remove payment feature', 'db_update_184.sql');
