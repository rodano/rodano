package ch.rodano.batch.pojo;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowState {

	private String id;
	private Map<String, String> shortname;
	private Map<String, String> longname;
	private Map<String, String> description;

	private boolean important;
	private String color;
	private String icon;
	private String aggregateStateId;
	private String aggregateStateMatcher;
	private List<String> possibleActionIds;

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

	public boolean isImportant() {
		return important;
	}

	public void setImportant(final boolean important) {
		this.important = important;
	}

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	public String getAggregateStateId() {
		return aggregateStateId;
	}

	public void setAggregateStateId(final String aggregateStateId) {
		this.aggregateStateId = aggregateStateId;
	}

	public String getAggregateStateMatcher() {
		return aggregateStateMatcher;
	}

	public void setAggregateStateMatcher(final String aggregateStateMatcher) {
		this.aggregateStateMatcher = aggregateStateMatcher;
	}

	public List<String> getPossibleActionIds() {
		return possibleActionIds;
	}

	public void setPossibleActionIds(final List<String> possibleActionsIds) {
		this.possibleActionIds = possibleActionsIds;
	}

	@Override
	public String toString() {
		return "WorkflowState{" +
			"id='" + id + '\'' +
			", shortname=" + shortname +
			", longname=" + longname +
			", description=" + description +
			", important=" + important +
			", color='" + color + '\'' +
			", icon='" + icon + '\'' +
			", aggregateStateId='" + aggregateStateId + '\'' +
			", aggregateStateMatcher='" + aggregateStateMatcher + '\'' +
			", possibleActionIds=" + possibleActionIds +
			'}';
	}
}
