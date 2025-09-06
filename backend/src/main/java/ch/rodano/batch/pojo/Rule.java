package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rule {

	private String description;
	private RuleConstraint constraint;
	private List<RuleAction> actions;
	private Map<String, String> message;
	private List<String> tags;

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public RuleConstraint getConstraint() {
		return constraint;
	}

	public void setConstraint(final RuleConstraint constraint) {
		this.constraint = constraint;
	}

	public List<RuleAction> getActions() {
		return actions;
	}

	public void setActions(final List<RuleAction> actions) {
		this.actions = actions;
	}

	public Map<String, String> getMessage() {
		return message;
	}

	public void setMessage(final Map<String, String> message) {
		this.message = message;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(final List<String> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "Rule{" +
			"description='" + description + '\'' +
			", constraint=" + constraint +
			", actions=" + actions +
			", message=" + message +
			", tags=" + tags +
			'}';
	}
}
