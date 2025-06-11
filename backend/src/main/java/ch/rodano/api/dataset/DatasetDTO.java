package ch.rodano.api.dataset;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.config.DatasetModelDTO;
import ch.rodano.api.field.FieldDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "A dataset is a semantic grouping of fields.")
public class DatasetDTO {
	@Schema(description = "Scope reference")
	@NotNull
	Long scopePk;
	@NotEmpty
	String scopeId;
	@NotEmpty
	String scopeCodeAndShortname;

	@Schema(description = "Event reference")
	Long eventPk;
	String eventId;
	String eventShortname;

	@Schema(description = "Dataset pk")
	@NotNull
	Long pk;
	@Schema(description = "Dataset ID")
	@NotBlank
	String id;

	@NotNull
	ZonedDateTime creationTime;
	@NotNull
	ZonedDateTime lastUpdateTime;

	@Schema(description = "Dataset model")
	@NotNull
	DatasetModelDTO model;
	@Schema(description = "Dataset model ID")
	@NotBlank
	String modelId;

	@NotNull
	boolean removed;
	@Schema(description = "Is the dataset attached to a removed entity?")
	@NotNull
	boolean inRemoved;
	@NotNull
	@Schema(description = "Is the dataset attached to a locked entity?")
	boolean inLocked;

	@Schema(description = "Can the user update the dataset?")
	@NotNull
	boolean canWrite;
	@Schema(description = "Can the user remove the dataset?")
	@NotNull
	boolean canBeRemoved;

	@Schema(description = "Fields associated with the dataset")
	@NotNull
	List<FieldDTO> fields;

	public final Long getScopePk() {
		return scopePk;
	}

	public final void setScopePk(final Long scopePk) {
		this.scopePk = scopePk;
	}

	public String getScopeId() {
		return scopeId;
	}

	public void setScopeId(final String scopeId) {
		this.scopeId = scopeId;
	}

	public final String getScopeCodeAndShortname() {
		return scopeCodeAndShortname;
	}

	public final void setScopeCodeAndShortname(final String scopeCodeAndShortname) {
		this.scopeCodeAndShortname = scopeCodeAndShortname;
	}

	public final Long getEventPk() {
		return eventPk;
	}

	public final void setEventPk(final Long eventPk) {
		this.eventPk = eventPk;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(final String eventId) {
		this.eventId = eventId;
	}

	public final String getEventShortname() {
		return eventShortname;
	}

	public final void setEventShortname(final String eventShortname) {
		this.eventShortname = eventShortname;
	}

	public DatasetModelDTO getModel() {
		return model;
	}

	public void setModel(final DatasetModelDTO model) {
		this.model = model;
	}

	public final String getModelId() {
		return modelId;
	}

	public final void setModelId(final String modelId) {
		this.modelId = modelId;
	}

	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public final boolean isRemoved() {
		return removed;
	}

	public final void setRemoved(final boolean removed) {
		this.removed = removed;
	}

	public boolean isInRemoved() {
		return inRemoved;
	}

	public void setInRemoved(final boolean inRemoved) {
		this.inRemoved = inRemoved;
	}

	public boolean isInLocked() {
		return inLocked;
	}

	public void setInLocked(final boolean inLocked) {
		this.inLocked = inLocked;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(final boolean canWrite) {
		this.canWrite = canWrite;
	}

	public boolean isCanBeRemoved() {
		return canBeRemoved;
	}

	public void setCanBeRemoved(final boolean canBeRemoved) {
		this.canBeRemoved = canBeRemoved;
	}

	public final List<FieldDTO> getFields() {
		return fields;
	}

	public final void setFields(final List<FieldDTO> fields) {
		this.fields = fields;
	}

	public final Long getPk() {
		return pk;
	}

	public final void setPk(final Long pk) {
		this.pk = pk;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

}
