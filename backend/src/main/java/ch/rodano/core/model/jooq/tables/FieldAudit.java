/*
 * This file is generated by jOOQ.
 */
package ch.rodano.core.model.jooq.tables;


import ch.rodano.core.helpers.configuration.DateConverter;
import ch.rodano.core.model.jooq.DefaultSchema;
import ch.rodano.core.model.jooq.Keys;
import ch.rodano.core.model.jooq.tables.AuditAction.AuditActionPath;
import ch.rodano.core.model.jooq.tables.Field.FieldPath;
import ch.rodano.core.model.jooq.tables.Robot.RobotPath;
import ch.rodano.core.model.jooq.tables.User.UserPath;
import ch.rodano.core.model.jooq.tables.records.FieldAuditRecord;
import ch.rodano.core.model.jooqutils.AuditTable;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class FieldAudit extends TableImpl<FieldAuditRecord> implements AuditTable {

	private static final long serialVersionUID = 1L;

	/**
	 * The reference instance of <code>field_audit</code>
	 */
	public static final FieldAudit FIELD_AUDIT = new FieldAudit();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<FieldAuditRecord> getRecordType() {
		return FieldAuditRecord.class;
	}

	/**
	 * The column <code>field_audit.pk</code>.
	 */
	public final TableField<FieldAuditRecord, Long> PK = createField(DSL.name("pk"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

	/**
	 * The column <code>field_audit.audit_action_fk</code>.
	 */
	public final TableField<FieldAuditRecord, Long> AUDIT_ACTION_FK = createField(DSL.name("audit_action_fk"), SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>field_audit.audit_datetime</code>.
	 */
	public final TableField<FieldAuditRecord, ZonedDateTime> AUDIT_DATETIME = createField(DSL.name("audit_datetime"), SQLDataType.LOCALDATETIME(3).nullable(false), this, "", new DateConverter());

	/**
	 * The column <code>field_audit.audit_actor</code>.
	 */
	public final TableField<FieldAuditRecord, String> AUDIT_ACTOR = createField(DSL.name("audit_actor"), SQLDataType.VARCHAR(200).nullable(false), this, "");

	/**
	 * The column <code>field_audit.audit_user_fk</code>.
	 */
	public final TableField<FieldAuditRecord, Long> AUDIT_USER_FK = createField(DSL.name("audit_user_fk"), SQLDataType.BIGINT.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINT)), this, "");

	/**
	 * The column <code>field_audit.audit_robot_fk</code>.
	 */
	public final TableField<FieldAuditRecord, Long> AUDIT_ROBOT_FK = createField(DSL.name("audit_robot_fk"), SQLDataType.BIGINT.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINT)), this, "");

	/**
	 * The column <code>field_audit.audit_context</code>.
	 */
	public final TableField<FieldAuditRecord, String> AUDIT_CONTEXT = createField(DSL.name("audit_context"), SQLDataType.CLOB.nullable(false), this, "");

	/**
	 * The column <code>field_audit.audit_object_fk</code>.
	 */
	public final TableField<FieldAuditRecord, Long> AUDIT_OBJECT_FK = createField(DSL.name("audit_object_fk"), SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>field_audit.dataset_fk</code>.
	 */
	public final TableField<FieldAuditRecord, Long> DATASET_FK = createField(DSL.name("dataset_fk"), SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>field_audit.dataset_model_id</code>.
	 */
	public final TableField<FieldAuditRecord, String> DATASET_MODEL_ID = createField(DSL.name("dataset_model_id"), SQLDataType.VARCHAR(100).nullable(false), this, "");

	/**
	 * The column <code>field_audit.field_model_id</code>.
	 */
	public final TableField<FieldAuditRecord, String> FIELD_MODEL_ID = createField(DSL.name("field_model_id"), SQLDataType.VARCHAR(100).nullable(false), this, "");

	/**
	 * The column <code>field_audit.value</code>.
	 */
	public final TableField<FieldAuditRecord, String> VALUE = createField(DSL.name("value"), SQLDataType.CLOB.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.CLOB)), this, "");

	private FieldAudit(Name alias, Table<FieldAuditRecord> aliased) {
		this(alias, aliased, (Field<?>[]) null, null);
	}

	private FieldAudit(Name alias, Table<FieldAuditRecord> aliased, Field<?>[] parameters, Condition where) {
		super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
	}

	/**
	 * Create an aliased <code>field_audit</code> table reference
	 */
	public FieldAudit(String alias) {
		this(DSL.name(alias), FIELD_AUDIT);
	}

	/**
	 * Create an aliased <code>field_audit</code> table reference
	 */
	public FieldAudit(Name alias) {
		this(alias, FIELD_AUDIT);
	}

	/**
	 * Create a <code>field_audit</code> table reference
	 */
	public FieldAudit() {
		this(DSL.name("field_audit"), null);
	}

	public <O extends Record> FieldAudit(Table<O> path, ForeignKey<O, FieldAuditRecord> childPath, InverseForeignKey<O, FieldAuditRecord> parentPath) {
		super(path, childPath, parentPath, FIELD_AUDIT);
	}

	/**
	 * A subtype implementing {@link Path} for simplified path-based joins.
	 */
	public static class FieldAuditPath extends FieldAudit implements Path<FieldAuditRecord> {

		private static final long serialVersionUID = 1L;
		public <O extends Record> FieldAuditPath(Table<O> path, ForeignKey<O, FieldAuditRecord> childPath, InverseForeignKey<O, FieldAuditRecord> parentPath) {
			super(path, childPath, parentPath);
		}
		private FieldAuditPath(Name alias, Table<FieldAuditRecord> aliased) {
			super(alias, aliased);
		}

		@Override
		public FieldAuditPath as(String alias) {
			return new FieldAuditPath(DSL.name(alias), this);
		}

		@Override
		public FieldAuditPath as(Name alias) {
			return new FieldAuditPath(alias, this);
		}

		@Override
		public FieldAuditPath as(Table<?> alias) {
			return new FieldAuditPath(alias.getQualifiedName(), this);
		}
	}

	@Override
	public Schema getSchema() {
		return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
	}

	@Override
	public Identity<FieldAuditRecord, Long> getIdentity() {
		return (Identity<FieldAuditRecord, Long>) super.getIdentity();
	}

	@Override
	public UniqueKey<FieldAuditRecord> getPrimaryKey() {
		return Keys.KEY_FIELD_AUDIT_PRIMARY;
	}

	@Override
	public List<ForeignKey<FieldAuditRecord, ?>> getReferences() {
		return Arrays.asList(Keys.FK_FIELD_AUDIT_OBJECT_FK, Keys.FK_FIELD_AUDIT_ROBOT_FK, Keys.FK_FIELD_AUDIT_USER_FK, Keys.FK_FIELD_TRAIL_AUDIT_ACTION_FK);
	}

	private transient FieldPath _field;

	/**
	 * Get the implicit join path to the <code>field</code> table.
	 */
	public FieldPath field() {
		if (_field == null)
			_field = new FieldPath(this, Keys.FK_FIELD_AUDIT_OBJECT_FK, null);

		return _field;
	}

	private transient RobotPath _robot;

	/**
	 * Get the implicit join path to the <code>robot</code> table.
	 */
	public RobotPath robot() {
		if (_robot == null)
			_robot = new RobotPath(this, Keys.FK_FIELD_AUDIT_ROBOT_FK, null);

		return _robot;
	}

	private transient UserPath _user;

	/**
	 * Get the implicit join path to the <code>user</code> table.
	 */
	public UserPath user() {
		if (_user == null)
			_user = new UserPath(this, Keys.FK_FIELD_AUDIT_USER_FK, null);

		return _user;
	}

	private transient AuditActionPath _auditAction;

	/**
	 * Get the implicit join path to the <code>audit_action</code> table.
	 */
	public AuditActionPath auditAction() {
		if (_auditAction == null)
			_auditAction = new AuditActionPath(this, Keys.FK_FIELD_TRAIL_AUDIT_ACTION_FK, null);

		return _auditAction;
	}

	@Override
	public FieldAudit as(String alias) {
		return new FieldAudit(DSL.name(alias), this);
	}

	@Override
	public FieldAudit as(Name alias) {
		return new FieldAudit(alias, this);
	}

	@Override
	public FieldAudit as(Table<?> alias) {
		return new FieldAudit(alias.getQualifiedName(), this);
	}

	/**
	 * Rename this table
	 */
	@Override
	public FieldAudit rename(String name) {
		return new FieldAudit(DSL.name(name), null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public FieldAudit rename(Name name) {
		return new FieldAudit(name, null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public FieldAudit rename(Table<?> name) {
		return new FieldAudit(name.getQualifiedName(), null);
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public FieldAudit where(Condition condition) {
		return new FieldAudit(getQualifiedName(), aliased() ? this : null, null, condition);
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public FieldAudit where(Collection<? extends Condition> conditions) {
		return where(DSL.and(conditions));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public FieldAudit where(Condition... conditions) {
		return where(DSL.and(conditions));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public FieldAudit where(Field<Boolean> condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public FieldAudit where(SQL condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public FieldAudit where(@Stringly.SQL String condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public FieldAudit where(@Stringly.SQL String condition, Object... binds) {
		return where(DSL.condition(condition, binds));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public FieldAudit where(@Stringly.SQL String condition, QueryPart... parts) {
		return where(DSL.condition(condition, parts));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public FieldAudit whereExists(Select<?> select) {
		return where(DSL.exists(select));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public FieldAudit whereNotExists(Select<?> select) {
		return where(DSL.notExists(select));
	}
}
