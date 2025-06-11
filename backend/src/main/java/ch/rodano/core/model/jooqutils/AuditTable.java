package ch.rodano.core.model.jooqutils;

import java.time.ZonedDateTime;

import org.jooq.Field;
import org.jooq.impl.DSL;

public interface AuditTable {

	default Field<Long> PK() {
		return DSL.field("pk", Long.class);
	}

	default Field<Long> AUDIT_OBJECT_FK() {
		return DSL.field("audit_object_fk", Long.class);
	}

	default Field<Long> AUDIT_ACTION_FK() {
		return DSL.field("audit_action_fk", Long.class);
	}

	default Field<ZonedDateTime> AUDIT_DATETIME() {
		return DSL.field("audit_datetime", ZonedDateTime.class);
	}

	default Field<String> AUDIT_ACTOR() {
		return DSL.field("audit_actor", String.class);
	}

	default Field<Long> AUDIT_USER_FK() {
		return DSL.field("audit_user_fk", Long.class);
	}

	default Field<Long> AUDIT_ROBOT_FK() {
		return DSL.field("audit_robot_fk", Long.class);
	}

	default Field<String> AUDIT_CONTEXT() {
		return DSL.field("audit_context", String.class);
	}
}
