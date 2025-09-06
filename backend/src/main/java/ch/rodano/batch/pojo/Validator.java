package ch.rodano.batch.pojo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Validator {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private Map<String, String> message;

	private boolean required;
	private boolean script;
	private String workflowId;
	private String invalidStateId;
	private String validStateId;

	private RuleConstraint constraint;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Map<String, String> getShortname() {
		return shortname;
	}

	public void setShortname(final Map<String, String> shortname) {
		this.shortname = shortname;
	}

	public Map<String, String> getLongname() {
		return longname;
	}

	public void setLongname(final Map<String, String> longname) {
		this.longname = longname;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(final Map<String, String> description) {
		this.description = description;
	}

	public Map<String, String> getMessage() {
		return message;
	}

	public void setMessage(final Map<String, String> message) {
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

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}

	public String getInvalidStateId() {
		return invalidStateId;
	}

	public void setInvalidStateId(final String invalidStateId) {
		this.invalidStateId = invalidStateId;
	}

	public String getValidStateId() {
		return validStateId;
	}

	public void setValidStateId(final String validStateId) {
		this.validStateId = validStateId;
	}

	public RuleConstraint getConstraint() {
		return constraint;
	}

	public void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	@Override
	public String toString() {
		return "Validator{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", message=" + message +
			", required=" + required +
			", script=" + script +
			", workflowId='" + workflowId + '\'' +
			", invalidStateId='" + invalidStateId + '\'' +
			", validStateId='" + validStateId + '\'' +
			", constraint=" + constraint +
			'}';
	}
}
