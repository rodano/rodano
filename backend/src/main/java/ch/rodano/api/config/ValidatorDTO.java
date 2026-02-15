package ch.rodano.api.config;

import java.util.SortedMap;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.NonNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validator")
public class ValidatorDTO implements Comparable<ValidatorDTO> {

	@Schema(description = "Unique validator UUID")
	@NotNull
	UUID validatorId;
	@Schema(description = "Unique validator ID")
	@NotBlank
	String id;

	@NotNull
	SortedMap<String, String> shortname;
	SortedMap<String, String> longname;
	SortedMap<String, String> description;
	SortedMap<String, String> message;

	@Schema(description = "Is the validator required?")
	@NotNull
	boolean required;
	@Schema(description = "Is this validator a script?")
	@NotNull
	boolean script;

	UUID workflowId;
	UUID invalidStateId;
	UUID validStateId;

	public UUID getValidatorId() {
		return validatorId;
	}

	public void setValidatorId(final UUID validatorId) {
		this.validatorId = validatorId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public SortedMap<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final SortedMap<String, String> shortname) {
		this.shortname = shortname;
	}

	public SortedMap<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final SortedMap<String, String> longname) {
		this.longname = longname;
	}

	public SortedMap<String, String> getDescription() {
		return description;
	}

	public void setDescription(final SortedMap<String, String> description) {
		this.description = description;
	}

	public SortedMap<String, String> getMessage() {
		return message;
	}

	public void setMessage(final SortedMap<String, String> message) {
		this.message = message;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(final boolean required) {
		this.required = required;
	}

	public boolean isScript() {
		return script;
	}

	public void setScript(final boolean script) {
		this.script = script;
	}

	public UUID getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final UUID workflowId) {
		this.workflowId = workflowId;
	}

	public UUID getInvalidStateId() {
		return invalidStateId;
	}

	public void setInvalidStateId(final UUID invalidStateId) {
		this.invalidStateId = invalidStateId;
	}

	public UUID getValidStateId() {
		return validStateId;
	}

	public void setValidStateId(final UUID validStateId) {
		this.validStateId = validStateId;
	}

	@Override
	public int compareTo(@NonNull final ValidatorDTO o) {
		return this.id.compareTo(o.id);
	}
}
