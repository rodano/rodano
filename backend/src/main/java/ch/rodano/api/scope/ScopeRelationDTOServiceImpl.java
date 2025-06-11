package ch.rodano.api.scope;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.actor.Actor;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeRelation;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.utils.RightsService;

@Service
public class ScopeRelationDTOServiceImpl implements ScopeRelationDTOService {

	private final ScopeRelationService scopeRelationService;
	private final RightsService rightsService;

	public ScopeRelationDTOServiceImpl(
		final ScopeRelationService scopeRelationService,
		final RightsService rightsService
	) {
		this.scopeRelationService = scopeRelationService;
		this.rightsService = rightsService;
	}

	@Override
	public List<ScopeRelationDTO> createDTOs(final Scope scope, final Actor actor, final List<Role> roles) {
		final var relations = scopeRelationService.getNonDeletedParentRelations(scope);
		final var relationDTOs = new ArrayList<ScopeRelationDTO>();
		for(final ScopeRelation relation : relations) {
			final var parent = scopeRelationService.getParent(relation);
			if(rightsService.hasRight(parent, Rights.READ, roles)) {
				relationDTOs.add(new ScopeRelationDTO(scope, parent, relation));
			}
		}
		return relationDTOs;
	}

}
