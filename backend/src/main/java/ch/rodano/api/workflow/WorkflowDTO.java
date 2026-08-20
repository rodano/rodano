package ch.rodano.api.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The workflow model")
public class WorkflowDTO {
	@Schema(description = "The unique ID of the workflow")
	@NotBlank
	public String id;

	@NotNull
	public SortedMap<String, String> shortname = new TreeMap<>();
	public SortedMap<String, String> longname = new TreeMap<>();
	public SortedMap<String, String> description = new TreeMap<>();

	@Schema(description = "Is the workflow an aggregate workflow?")
	@NotNull
	public boolean aggregator;

	@Schema(description = "Aggregated workflow Id")
	public String aggregatedWorkflowId;

	@Schema(description = "Is the workflow mandatory?")
	@NotNull
	public boolean mandatory;

	@Schema(description = "Is the workflow searchable?")
	@NotNull
	public boolean searchable;
	public String actionId;

	@Schema(description = "Icon of the workflow")
	public String icon;

	@Schema(description = "Possible states of the workflow")
	@NotNull
	public List<WorkflowStateDTO> states = new ArrayList<>();
	@Schema(description = "Possible actions that can be performed on the workflow")
	@NotNull
	public List<WorkflowActionDTO> actions = new ArrayList<>();
}
