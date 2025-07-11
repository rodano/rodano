/*
 * This file is generated by jOOQ.
 */
package ch.rodano.core.model.jooq.tables.records;


import ch.rodano.core.model.jooq.tables.Dataset;

import java.time.ZonedDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class DatasetRecord extends UpdatableRecordImpl<DatasetRecord> {

	private static final long serialVersionUID = 1L;

	/**
	 * Setter for <code>dataset.pk</code>.
	 */
	public void setPk(Long value) {
		set(0, value);
	}

	/**
	 * Getter for <code>dataset.pk</code>.
	 */
	public Long getPk() {
		return (Long) get(0);
	}

	/**
	 * Setter for <code>dataset.id</code>.
	 */
	public void setId(String value) {
		set(1, value);
	}

	/**
	 * Getter for <code>dataset.id</code>.
	 */
	public String getId() {
		return (String) get(1);
	}

	/**
	 * Setter for <code>dataset.creation_time</code>.
	 */
	public void setCreationTime(ZonedDateTime value) {
		set(2, value);
	}

	/**
	 * Getter for <code>dataset.creation_time</code>.
	 */
	public ZonedDateTime getCreationTime() {
		return (ZonedDateTime) get(2);
	}

	/**
	 * Setter for <code>dataset.last_update_time</code>.
	 */
	public void setLastUpdateTime(ZonedDateTime value) {
		set(3, value);
	}

	/**
	 * Getter for <code>dataset.last_update_time</code>.
	 */
	public ZonedDateTime getLastUpdateTime() {
		return (ZonedDateTime) get(3);
	}

	/**
	 * Setter for <code>dataset.deleted</code>.
	 */
	public void setDeleted(Boolean value) {
		set(4, value);
	}

	/**
	 * Getter for <code>dataset.deleted</code>.
	 */
	public Boolean getDeleted() {
		return (Boolean) get(4);
	}

	/**
	 * Setter for <code>dataset.scope_fk</code>.
	 */
	public void setScopeFk(Long value) {
		set(5, value);
	}

	/**
	 * Getter for <code>dataset.scope_fk</code>.
	 */
	public Long getScopeFk() {
		return (Long) get(5);
	}

	/**
	 * Setter for <code>dataset.event_fk</code>.
	 */
	public void setEventFk(Long value) {
		set(6, value);
	}

	/**
	 * Getter for <code>dataset.event_fk</code>.
	 */
	public Long getEventFk() {
		return (Long) get(6);
	}

	/**
	 * Setter for <code>dataset.dataset_model_id</code>.
	 */
	public void setDatasetModelId(String value) {
		set(7, value);
	}

	/**
	 * Getter for <code>dataset.dataset_model_id</code>.
	 */
	public String getDatasetModelId() {
		return (String) get(7);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	@Override
	public Record1<Long> key() {
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached DatasetRecord
	 */
	public DatasetRecord() {
		super(Dataset.DATASET);
	}

	/**
	 * Create a detached, initialised DatasetRecord
	 */
	public DatasetRecord(Long pk, String id, ZonedDateTime creationTime, ZonedDateTime lastUpdateTime, Boolean deleted, Long scopeFk, Long eventFk, String datasetModelId) {
		super(Dataset.DATASET);

		setPk(pk);
		setId(id);
		setCreationTime(creationTime);
		setLastUpdateTime(lastUpdateTime);
		setDeleted(deleted);
		setScopeFk(scopeFk);
		setEventFk(eventFk);
		setDatasetModelId(datasetModelId);
		resetChangedOnNotNull();
	}
}
