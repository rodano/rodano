package ch.rodano.api.scope;

import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeRelation;

@Schema(description = "Represents the parent-child relation between two scopes")
public record ScopeRelationDTO(
	@NotNull Long pk,
	@NotNull ZonedDateTime startDate,
	@NotNull ZonedDateTime stopDate,

	@JsonProperty("default")
	@JsonAlias("default")
	@NotNull boolean defaultRelation,

	@NotNull ScopeMiniDTO scope,
	@NotNull ScopeMiniDTO parent
) {

	public ScopeRelationDTO(final Scope scope, final Scope parent, final ScopeRelation relation) {
		this(
			relation.getPk(),
			relation.getStartDate(),
			relation.getEndDate(),
			relation.getDefault(),
			new ScopeMiniDTO(scope),
			new ScopeMiniDTO(parent)
		);
	}
}
