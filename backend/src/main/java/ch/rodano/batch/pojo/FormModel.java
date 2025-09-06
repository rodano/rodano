package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormModel {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private Map<String, String> printButtonLabel;

	private boolean optional;

	private List<String> workflowIds;
	private List<FormModelLayout> layouts;

	private List<Rule> rules;

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

	public Map<String, String> getPrintButtonLabel() {
		return printButtonLabel;
	}

	public void setPrintButtonLabel(final Map<String, String> printButtonLabel) {
		this.printButtonLabel = printButtonLabel;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(final boolean optional) {
		this.optional = optional;
	}

	public List<String> getWorkflowIds() {
		return workflowIds;
	}

	public void setWorkflowIds(final List<String> workflowIds) {
		this.workflowIds = workflowIds;
	}

	public List<FormModelLayout> getLayouts() {
		return layouts;
	}

	public void setLayouts(final List<FormModelLayout> layouts) {
		this.layouts = layouts;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public String toString() {
		return "FormModel{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", printButtonLabel=" + printButtonLabel +
			", optional=" + optional +
			", workflowIds=" + workflowIds +
			", layouts=" + layouts +
			", rules=" + rules +
			'}';
	}
}
