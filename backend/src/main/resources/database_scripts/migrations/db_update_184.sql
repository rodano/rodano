insert into internal_patch (script, date, context, name) values (184, now(3), 'Remove payment feature', 'db_update_184.sql');

/* drop tables child-first so their foreign keys are removed along with them */
DROP TABLE IF EXISTS payment_target;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS payment_batch;
