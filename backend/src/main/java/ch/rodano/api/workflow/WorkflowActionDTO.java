package ch.rodano.api.workflow;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.configuration.model.workflow.Action;

@Schema(description = "An action that can be performed on a workflow")
public class WorkflowActionDTO {
	@Schema(description = "Action ID")
	@NotBlank
	private String id;
	@Schema(description = "Workflow ID")
	@NotBlank
	private String workflowId;

	@NotNull
	private SortedMap<String, String> shortname;
	@NotNull
	private SortedMap<String, String> longname;
	@NotNull
	private SortedMap<String, String> description;

	@Schema(description = "Does the action require a signature?")
	@NotNull
	private boolean requireSignature;
	@Schema(description = "The signature requirement text")
	@NotNull
	private SortedMap<String, String> requireSignatureText;

	@Schema(description = "Can a message be attached to the action execution?")
	@NotNull
	private boolean documentable;
	@Schema(description = "Action message options")
	@NotNull
	private List<Map<String, String>> documentableOptions;

	@NotBlank
	private String icon;

	WorkflowActionDTO() {}

	public WorkflowActionDTO(final Action action) {
		id = action.getId();
		workflowId = action.getWorkflow().getId();

		shortname = action.getShortname();
		longname = action.getLongname();
		description = action.getDescription();

		requireSignature = action.isRequireSignature();
		documentable = action.isDocumentable();
		documentableOptions = action.getDocumentableOptions();

		icon = StringUtils.defaultIfBlank(action.getIcon(), action.getWorkflow().getIcon());

		requireSignatureText = action.getRequiredSignatureText();
	}

	/**
	 * Create a list of DTO actions from actions
	 *
	 * @param actions The action
	 * @return A list of DTO actions
	 */
	public static List<WorkflowActionDTO> fromActions(final Collection<Action> actions) {
		return actions.stream().map(WorkflowActionDTO::new).toList();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(final String parentId) {
		this.workflowId = parentId;
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

	public SortedMap<String, String> getRequireSignatureText() {
		return requireSignatureText;
	}

	public void setRequireSignatureText(final SortedMap<String, String> requireSignatureText) {
		this.requireSignatureText = requireSignatureText;
	}

	public boolean isRequireSignature() {
		return requireSignature;
	}

	public void setRequireSignature(final boolean requireSignature) {
		this.requireSignature = requireSignature;
	}

	public boolean isDocumentable() {
		return documentable;
	}

	public void setDocumentable(final boolean documentable) {
		this.documentable = documentable;
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
}
