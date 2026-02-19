package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowAction {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;
	private Map<String, String> requiredSignatureText;
	private List<Map<String, String>> documentableOptions;

	private boolean documentable;
	private boolean requireSignature;

	private String icon;

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

	public Map<String, String> getRequiredSignatureText() {
		return requiredSignatureText;
	}

	public void setRequiredSignatureText(final Map<String, String> requiredSignatureText) {
		this.requiredSignatureText = requiredSignatureText;
	}

	public boolean isDocumentable() {
		return documentable;
	}

	public void setDocumentable(final boolean documentable) {
		this.documentable = documentable;
	}

	public boolean isRequireSignature() {
		return requireSignature;
	}

	public void setRequireSignature(final boolean requireSignature) {
		this.requireSignature = requireSignature;
	}

	public List<Map<String, String>> getDocumentableOptions() {
		return documentableOptions;
	}

	public void setDocumentableOptions(final List<Map<String, String>> documentableOptions) {
		this.documentableOptions = documentableOptions;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public String toString() {
		return "WorkflowAction{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", requiredSignatureText=" + requiredSignatureText +
			", documentableOptions=" + documentableOptions +
			", documentable=" + documentable +
			", requireSignature=" + requireSignature +
			", icon='" + icon + '\'' +
			", rules=" + rules +
			'}';
	}
}
