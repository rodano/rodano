/*
 * This file is generated by jOOQ.
 */
package ch.rodano.core.model.jooq.tables;


import ch.rodano.core.helpers.configuration.DateConverter;
import ch.rodano.core.model.jooq.DefaultSchema;
import ch.rodano.core.model.jooq.Indexes;
import ch.rodano.core.model.jooq.Keys;
import ch.rodano.core.model.jooq.tables.Payment.PaymentPath;
import ch.rodano.core.model.jooq.tables.records.PaymentBatchRecord;

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
public class PaymentBatch extends TableImpl<PaymentBatchRecord> {

	private static final long serialVersionUID = 1L;

	/**
	 * The reference instance of <code>payment_batch</code>
	 */
	public static final PaymentBatch PAYMENT_BATCH = new PaymentBatch();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<PaymentBatchRecord> getRecordType() {
		return PaymentBatchRecord.class;
	}

	/**
	 * The column <code>payment_batch.pk</code>.
	 */
	public final TableField<PaymentBatchRecord, Long> PK = createField(DSL.name("pk"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

	/**
	 * The column <code>payment_batch.creation_time</code>.
	 */
	public final TableField<PaymentBatchRecord, ZonedDateTime> CREATION_TIME = createField(DSL.name("creation_time"), SQLDataType.LOCALDATETIME(3).nullable(false).defaultValue(DSL.field(DSL.raw("current_timestamp(3)"), SQLDataType.LOCALDATETIME)), this, "", new DateConverter());

	/**
	 * The column <code>payment_batch.last_update_time</code>.
	 */
	public final TableField<PaymentBatchRecord, ZonedDateTime> LAST_UPDATE_TIME = createField(DSL.name("last_update_time"), SQLDataType.LOCALDATETIME(3).nullable(false).defaultValue(DSL.field(DSL.raw("current_timestamp(3)"), SQLDataType.LOCALDATETIME)), this, "", new DateConverter());

	/**
	 * The column <code>payment_batch.deleted</code>.
	 */
	public final TableField<PaymentBatchRecord, Boolean> DELETED = createField(DSL.name("deleted"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("0"), SQLDataType.BOOLEAN)), this, "");

	/**
	 * The column <code>payment_batch.scope_id</code>.
	 */
	public final TableField<PaymentBatchRecord, String> SCOPE_ID = createField(DSL.name("scope_id"), SQLDataType.VARCHAR(200).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "");

	/**
	 * The column <code>payment_batch.plan_id</code>.
	 */
	public final TableField<PaymentBatchRecord, String> PLAN_ID = createField(DSL.name("plan_id"), SQLDataType.VARCHAR(50).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "");

	/**
	 * The column <code>payment_batch.status</code>.
	 */
	public final TableField<PaymentBatchRecord, String> STATUS = createField(DSL.name("status"), SQLDataType.VARCHAR(50).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "");

	/**
	 * The column <code>payment_batch.payment_date</code>.
	 */
	public final TableField<PaymentBatchRecord, ZonedDateTime> PAYMENT_DATE = createField(DSL.name("payment_date"), SQLDataType.LOCALDATETIME(3).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.LOCALDATETIME)), this, "", new DateConverter());

	/**
	 * The column <code>payment_batch.closed_date</code>.
	 */
	public final TableField<PaymentBatchRecord, ZonedDateTime> CLOSED_DATE = createField(DSL.name("closed_date"), SQLDataType.LOCALDATETIME(3).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.LOCALDATETIME)), this, "", new DateConverter());

	/**
	 * The column <code>payment_batch.printed_date</code>.
	 */
	public final TableField<PaymentBatchRecord, ZonedDateTime> PRINTED_DATE = createField(DSL.name("printed_date"), SQLDataType.LOCALDATETIME(3).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.LOCALDATETIME)), this, "", new DateConverter());

	/**
	 * The column <code>payment_batch.comment</code>.
	 */
	public final TableField<PaymentBatchRecord, String> COMMENT = createField(DSL.name("comment"), SQLDataType.VARCHAR(500).defaultValue(DSL.field(DSL.raw("NULL"), SQLDataType.VARCHAR)), this, "");

	private PaymentBatch(Name alias, Table<PaymentBatchRecord> aliased) {
		this(alias, aliased, (Field<?>[]) null, null);
	}

	private PaymentBatch(Name alias, Table<PaymentBatchRecord> aliased, Field<?>[] parameters, Condition where) {
		super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
	}

	/**
	 * Create an aliased <code>payment_batch</code> table reference
	 */
	public PaymentBatch(String alias) {
		this(DSL.name(alias), PAYMENT_BATCH);
	}

	/**
	 * Create an aliased <code>payment_batch</code> table reference
	 */
	public PaymentBatch(Name alias) {
		this(alias, PAYMENT_BATCH);
	}

	/**
	 * Create a <code>payment_batch</code> table reference
	 */
	public PaymentBatch() {
		this(DSL.name("payment_batch"), null);
	}

	public <O extends Record> PaymentBatch(Table<O> path, ForeignKey<O, PaymentBatchRecord> childPath, InverseForeignKey<O, PaymentBatchRecord> parentPath) {
		super(path, childPath, parentPath, PAYMENT_BATCH);
	}

	/**
	 * A subtype implementing {@link Path} for simplified path-based joins.
	 */
	public static class PaymentBatchPath extends PaymentBatch implements Path<PaymentBatchRecord> {

		private static final long serialVersionUID = 1L;
		public <O extends Record> PaymentBatchPath(Table<O> path, ForeignKey<O, PaymentBatchRecord> childPath, InverseForeignKey<O, PaymentBatchRecord> parentPath) {
			super(path, childPath, parentPath);
		}
		private PaymentBatchPath(Name alias, Table<PaymentBatchRecord> aliased) {
			super(alias, aliased);
		}

		@Override
		public PaymentBatchPath as(String alias) {
			return new PaymentBatchPath(DSL.name(alias), this);
		}

		@Override
		public PaymentBatchPath as(Name alias) {
			return new PaymentBatchPath(alias, this);
		}

		@Override
		public PaymentBatchPath as(Table<?> alias) {
			return new PaymentBatchPath(alias.getQualifiedName(), this);
		}
	}

	@Override
	public Schema getSchema() {
		return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
	}

	@Override
	public List<Index> getIndexes() {
		return Arrays.asList(Indexes.PAYMENT_BATCH_IDX_PAYMENT_BATCH_DELETED);
	}

	@Override
	public Identity<PaymentBatchRecord, Long> getIdentity() {
		return (Identity<PaymentBatchRecord, Long>) super.getIdentity();
	}

	@Override
	public UniqueKey<PaymentBatchRecord> getPrimaryKey() {
		return Keys.KEY_PAYMENT_BATCH_PRIMARY;
	}

	private transient PaymentPath _payment;

	/**
	 * Get the implicit to-many join path to the <code>payment</code> table
	 */
	public PaymentPath payment() {
		if (_payment == null)
			_payment = new PaymentPath(this, null, Keys.FK_PAYMENT_PAYMENT_BATCH_FK.getInverseKey());

		return _payment;
	}

	@Override
	public PaymentBatch as(String alias) {
		return new PaymentBatch(DSL.name(alias), this);
	}

	@Override
	public PaymentBatch as(Name alias) {
		return new PaymentBatch(alias, this);
	}

	@Override
	public PaymentBatch as(Table<?> alias) {
		return new PaymentBatch(alias.getQualifiedName(), this);
	}

	/**
	 * Rename this table
	 */
	@Override
	public PaymentBatch rename(String name) {
		return new PaymentBatch(DSL.name(name), null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public PaymentBatch rename(Name name) {
		return new PaymentBatch(name, null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public PaymentBatch rename(Table<?> name) {
		return new PaymentBatch(name.getQualifiedName(), null);
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public PaymentBatch where(Condition condition) {
		return new PaymentBatch(getQualifiedName(), aliased() ? this : null, null, condition);
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public PaymentBatch where(Collection<? extends Condition> conditions) {
		return where(DSL.and(conditions));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public PaymentBatch where(Condition... conditions) {
		return where(DSL.and(conditions));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public PaymentBatch where(Field<Boolean> condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public PaymentBatch where(SQL condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public PaymentBatch where(@Stringly.SQL String condition) {
		return where(DSL.condition(condition));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public PaymentBatch where(@Stringly.SQL String condition, Object... binds) {
		return where(DSL.condition(condition, binds));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	@PlainSQL
	public PaymentBatch where(@Stringly.SQL String condition, QueryPart... parts) {
		return where(DSL.condition(condition, parts));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public PaymentBatch whereExists(Select<?> select) {
		return where(DSL.exists(select));
	}

	/**
	 * Create an inline derived table from this table
	 */
	@Override
	public PaymentBatch whereNotExists(Select<?> select) {
		return where(DSL.notExists(select));
	}
}
