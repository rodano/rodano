package ch.rodano.api.scope;

import java.util.List;

import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;

public interface ScopeRelationDTOService {

	List<ScopeRelationDTO> createDTOs(Scope scope, Actor actor, List<Role> roles);

}
