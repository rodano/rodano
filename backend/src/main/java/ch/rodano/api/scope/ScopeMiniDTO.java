package ch.rodano.api.scope;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.scope.Scope;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScopeMiniDTO {
	@Schema(description = "Scope pk")
	@NotNull
	protected Long pk;
	@NotBlank
	protected String id;
	@NotBlank
	protected String code;
	@NotBlank
	protected String shortname;

	protected String longname;

	@Schema(description = "Scope model ID")
	@NotBlank
	protected String modelId;
	@NotNull
	protected boolean virtual;
	@NotNull
	protected ZonedDateTime startDate;
	protected ZonedDateTime stopDate;

	public ScopeMiniDTO() {}

	public ScopeMiniDTO(final Scope scope) {
		pk = scope.getPk();
		id = scope.getId();
		code = scope.getCode();
		shortname = scope.getShortname();
		longname = scope.getLongname();
		modelId = scope.getScopeModelId();
		virtual = scope.getVirtual();
		startDate = scope.getStartDate();
		stopDate = scope.getStopDate();
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(final String shortname) {
		this.shortname = shortname;
	}

	public String getLongname() {
		return longname;
	}

	public void setLongname(final String longname) {
		this.longname = longname;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(final String modelId) {
		this.modelId = modelId;
	}

	public ZonedDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(final ZonedDateTime startDate) {
		this.startDate = startDate;
	}

	public ZonedDateTime getStopDate() {
		return stopDate;
	}

	public void setStopDate(final ZonedDateTime stopDate) {
		this.stopDate = stopDate;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(final boolean virtual) {
		this.virtual = virtual;
	}

	public String getLabel() {
		return Scope.formatCodeAndShortname(code, shortname);
	}
}
