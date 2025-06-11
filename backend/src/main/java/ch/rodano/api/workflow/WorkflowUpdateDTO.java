package ch.rodano.api.workflow;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

public record WorkflowUpdateDTO(
	@Schema(description = "Workflow id when creating a new workflow") @NotBlank
	String workflowId,

	@Schema(description = "Action id")
	String actionId,

	@Schema(description = "Rationale")
	String rationale,

	@Schema(description = "User email for authentication") @Email
	String email,

	@Schema(description = "User password for authentication")
	String password 
) {
}
