package ch.rodano.api.workflow;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Workflow state")
public class WorkflowStateDTO {
	@NotBlank
	String id;
	@NotNull
	Map<String, String> shortname;
	@NotNull
	Map<String, String> longname;
	@NotNull
	Map<String, String> description;

	@Schema(description = "Is the state hidden?")
	@NotNull
	boolean hidden;
	@Schema(description = "Is the state important?")
	@NotNull
	boolean important;
	@Schema(description = "Should the state message be displayed?")
	@NotNull
	boolean showMessage;
	@Schema(description = "Possible actions relating to the state")
	@NotNull
	List<WorkflowActionDTO> possibleActions;

	@NotBlank
	String icon;
	@NotBlank
	String color;

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public boolean isImportant() {
		return important;
	}

	public void setImportant(final boolean important) {
		this.important = important;
	}

	public boolean isShowMessage() {
		return showMessage;
	}

	public void setShowMessage(final boolean showMessage) {
		this.showMessage = showMessage;
	}

	public List<WorkflowActionDTO> getPossibleActions() {
		return possibleActions;
	}

	public void setPossibleActions(final List<WorkflowActionDTO> possibleActions) {
		this.possibleActions = possibleActions;
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
}
