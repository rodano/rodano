/*
 * This file is generated by jOOQ.
 */
package ch.rodano.core.model.jooq.tables;


import ch.rodano.core.helpers.configuration.DateConverter;
import ch.rodano.core.model.jooq.DefaultSchema;
import ch.rodano.core.model.jooq.Indexes;
import ch.rodano.core.model.jooq.Keys;
import ch.rodano.core.model.jooq.tables.DatasetAudit.DatasetAuditPath;
import ch.rodano.core.model.jooq.tables.Event.EventPath;
import ch.rodano.core.model.jooq.tables.Field.FieldPath;
import ch.rodano.core.model.jooq.tables.File.FilePath;
import ch.rodano.core.model.jooq.tables.Scope.ScopePath;
import ch.rodano.core.model.jooq.tables.records.DatasetRecord;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
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
public class Dataset extends TableImpl<DatasetRecord> {

	private static final long serialVersionUID = 1L;

	/**
	 * The reference instance of <code>dataset</code>
	 */
	public static final Dataset DATASET = new Dataset();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<DatasetRecord> getRecordType() {
		return DatasetRecord.class;
	}

	/**
	 * The column <code>dataset.pk</code>.
	 */
	public final TableField<DatasetRecord, Long> PK = createField(DSL.name("pk"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

	/**
	 * The column <code>dataset.id</code>.
	 */
	public final TableField<DatasetRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(200).nullable(false), this, "");

	/**
	 * The column <code>dataset.creation_time</code>.
	 */
	public final TableField<DatasetRecord, ZonedDateTime> CREATION_TIME = createField(DSL.name("creation_time"), SQLDataType.LOCALDATETIME(3).nullable(false).defaultValue(DSL.field(DSL.raw("current_timestamp(3)"), SQLDataType.LOCALDATETIME)), this, "", new DateConverter());

	/**
	 * The column <code>dataset.last_update_time</code>.
	 */
	public final TableField<DatasetRecord, ZonedDateTime> LAST_UPDATE_TIME = createField(DSL.name("last_update_time"), SQLDataType.LOCALDATETIME(3).nullable(false).defaultValue(DSL.field(DSL.raw("current_timestamp(3)"), SQLDataType.LOCALDATETIME)), this, "", new DateConverter());

	/**
	 * The column <code>dataset.deleted</code>.
	 */
	public final TableField<DatasetRecord, Boolean> DELETED = createField(DSL.name("deleted"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.BOOLEAN)), this, "");

	/**
	 * The column <code>dataset.scope_fk</code>.
	 */
	public final TableField<DatasetRecord, Long> SCOPE_FK = createField(DSL.name("scope_fk"), SQLDataType.BIGINT.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINT)), this, "");

	/**
	 * The column <code>dataset.event_fk</code>.
	 */
	public final TableField<DatasetRecord, Long> EVENT_FK = createField(DSL.name("event_fk"), SQLDataType.BIGINT.defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.BIGINT)), this, "");

	/**
	 * The column <code>dataset.dataset_model_id</code>.
	 */
	public final TableField<DatasetRecord, String> DATASET_MODEL_ID = createField(DSL.name("dataset_model_id"), SQLDataType.VARCHAR(100).nullable(false), this, "");

	private Dataset(Name alias, Table<DatasetRecord> aliased) {
		this(alias, aliased, (Field<?>[]) null, null);
	}

	private Dataset(Name alias, Table<DatasetRecord> aliased, Field<?>[] parameters, Condition where) {
		super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
	}

	/**
	 * Create an aliased <code>dataset</code> table reference
	 */
	public Dataset(String alias) {
		this(DSL.name(alias), DATASET);
	}

	/**
	 * Create an aliased <code>dataset</code> table reference
	 */
	public Dataset(Name alias) {
		this(alias, DATASET);
	}

	/**
	 * Create a <code>dataset</code> table reference
	 */
	public Dataset() {
		this(DSL.name("dataset"), null);
	}

	public <O extends Record> Dataset(Table<O> path, ForeignKey<O, DatasetRecord> childPath, InverseForeignKey<O, DatasetRecord> parentPath) {
		super(path, childPath, parentPath, DATASET);
	}

	/**
	 * A subtype implementing {@link Path} for simplified path-based joins.
	 */
	public static class DatasetPath extends Dataset implements Path<DatasetRecord> {

		private static final long serialVersionUID = 1L;
		public <O extends Record> DatasetPath(Table<O> path, ForeignKey<O, DatasetRecord> childPath, InverseForeignKey<O, DatasetRecord> parentPath) {
			super(path, childPath, parentPath);
		}
		private DatasetPath(Name alias, Table<DatasetRecord> aliased) {
			super(alias, aliased);
		}

		@Override
		public DatasetPath as(String alias) {
			return new DatasetPath(DSL.name(alias), this);
		}

		@Override
		public DatasetPath as(Name alias) {
			return new DatasetPath(alias, this);
		}

		@Override
		public DatasetPath as(Table<?> alias) {
			return new DatasetPath(alias.getQualifiedName(), this);
		}
	}

	@Override
	public Schema getSchema() {
		return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
	}

	@Override
	public List<Index> getIndexes() {
		return Arrays.asList(Indexes.DATASET_IDX_DATASET_DELETED);
	}

	@Override
	public Identity<DatasetRecord, Long> getIdentity() {
		return (Identity<DatasetRecord, Long>) super.getIdentity();
	}

	@Override
	public UniqueKey<DatasetRecord> getPrimaryKey() {
		return Keys.KEY_DATASET_PRIMARY;
	}

	@Override
	public List<UniqueKey<DatasetRecord>> getUniqueKeys() {
		return Arrays.asList(Keys.KEY_DATASET_U_DATASET_ID);
	}

	@Override
	public List<ForeignKey<DatasetRecord, ?>> getReferences() {
		return Arrays.asList(Keys.FK_DATASET_EVENT_FK, Keys.FK_DATASET_SCOPE_FK);
	}

	private transient EventPath _event;

	/**
	 * Get the implicit join path to the <code>event</code> table.
	 */
	public EventPath event() {
		if (_event == null)
			_event = new EventPath(this, Keys.FK_DATASET_EVENT_FK, null);

		return _event;
	}

	private transient ScopePath _scope;

	/**
	 * Get the implicit join path to the <code>scope</code> table.
	 */
	public ScopePath scope() {
		if (_scope == null)
			_scope = new ScopePath(this, Keys.FK_DATASET_SCOPE_FK, null);

		return _scope;
	}

	private transient DatasetAuditPath _datasetAudit;

	/**
	 * Get the implicit to-many join path to the <code>dataset_audit</code> table
	 */
	public DatasetAuditPath datasetAudit() {
		if (_datasetAudit == null)
			_datasetAudit = new DatasetAuditPath(this, null, Keys.FK_DATASET_AUDIT_OBJECT_FK.getInverseKey());

		return _datasetAudit;
	}

	private transient FieldPath _field;

	/**
	 * Get the implicit to-many join path to the <code>field</code> table
	 */
	public FieldPath field() {
		if (_field == null)
			_field = new FieldPath(this, null, Keys.FK_FIELD_DATASET_FK.getInverseKey());

		return _field;
	}

	private transient FilePath _file;

	/**
	 * Get the implicit to-many join path to the <code>file</code> table
	 */
	public FilePath file() {
		if (_file == null)
			_file = new FilePath(this, null, Keys.FK_FILE_DATASET_FK.getInverseKey());

		return _file;
	}

	@Override
	public Dataset as(String alias) {
		return new Dataset(DSL.name(alias), this);
	}

	@Override
	public Dataset as(Name alias) {
		return new Dataset(alias, this);
	}

	@Override
	public Dataset as(Table<?> alias) {
		return new Dataset(alias.getQualifiedName(), this);
	}

	/**
	 * Rename this table
	 */
	@Override
	public Dataset rename(String name) {
		return new Dataset(DSL.name(name), null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public Dataset rename(Name name) {
		return new Dataset(name, null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public Dataset rename(Table<?> name) {
		return new Dataset(name.getQualifiedName(), null);
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public Dataset where(Condition condition) {
		return new Dataset(getQualifiedName(), aliased() ? this : null, null, condition);
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public Dataset where(Collection<? extends Condition> conditions) {
		return where(DSL.and(conditions));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public Dataset where(Condition... conditions) {
		return where(DSL.and(conditions));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public Dataset where(Field<Boolean> condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public Dataset where(SQL condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public Dataset where(@Stringly.SQL String condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public Dataset where(@Stringly.SQL String condition, Object... binds) {
		return where(DSL.condition(condition, binds));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public Dataset where(@Stringly.SQL String condition, QueryPart... parts) {
		return where(DSL.condition(condition, parts));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public Dataset whereExists(Select<?> select) {
		return where(DSL.exists(select));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public Dataset whereNotExists(Select<?> select) {
		return where(DSL.notExists(select));
	}
}
