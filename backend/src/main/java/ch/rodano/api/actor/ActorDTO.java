package ch.rodano.api.actor;

import java.time.ZonedDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.api.role.RoleDTO;

@Schema(description = "Application actor")
public class ActorDTO {

	@Schema(description = "Unique ID")
	@NotNull Long pk;
	@Schema(description = "Creation time")
	@NotNull ZonedDateTime creationTime;
	@Schema(description = "Last update time")
	@NotNull ZonedDateTime lastUpdateTime;
	@Schema(description = "Has the actor been removed?")
	@NotNull Boolean removed;
	@Schema(description = "Name")
	@NotBlank String name;
	@Schema(description = "Roles")
	@NotNull List<RoleDTO> roles;

	public Long getPk() {
		return pk;
	}

	public void setPk(final Long pk) {
		this.pk = pk;
	}

	public ZonedDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(final ZonedDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public ZonedDateTime getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(final ZonedDateTime lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public Boolean getRemoved() {
		return removed;
	}

	public void setRemoved(final Boolean removed) {
		this.removed = removed;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<RoleDTO> getRoles() {
		return roles;
	}

	public void setRoles(final List<RoleDTO> roles) {
		this.roles = roles;
	}
}
