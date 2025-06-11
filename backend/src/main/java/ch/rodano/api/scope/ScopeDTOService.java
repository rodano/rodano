package ch.rodano.api.scope;

import java.util.Collection;
import java.util.List;

import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.utils.ACL;

public interface ScopeDTOService {
	/**
	 * Generate a scope from a scope DTO
	 *
	 * @return A generated scope
	 */
	Scope generateScope(ScopeCandidateDTO scopeCandidateDTO);

	/**
	 * Update an already existing scope with a scope DTO
	 *
	 * @param scope  Scope to be updated
	 * @param scopeDTO    The DTO containing new info.
	 */
	void updateScope(Scope scope, ScopeDTO scopeDTO);

	/**
	 * Create the list of scope DTOs for a collection of scopes
	 */
	List<ScopeDTO> createDTOs(Collection<Scope> scopes, ACL acl);

	ScopeDTO createDTO(Scope scope, ACL acl);

}
