package ch.rodano.api.scope;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeRelation;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Scope relations")
@RestController
@RequestMapping("/scopes")
@Validated
@Transactional(readOnly = true)
public class ScopeRelationsController extends AbstractSecuredController {
	private final ScopeService scopeService;
	private final ScopeDAOService scopeDAOService;
	private final ScopeDTOService scopeDTOService;
	private final ScopeRelationService scopeRelationService;
	private final ScopeRelationDTOService scopeRelationDTOService;
	private final UtilsService utilsService;

	public ScopeRelationsController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ScopeDAOService scopeDAOService,
		final ScopeDTOService scopeDTOService,
		final ScopeRelationService scopeRelationService,
		final ScopeRelationDTOService scopeRelationDTOService,
		final UtilsService utilsService,
		final ScopeService scopeService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.scopeService = scopeService;
		this.scopeDAOService = scopeDAOService;
		this.scopeDTOService = scopeDTOService;
		this.scopeRelationService = scopeRelationService;
		this.scopeRelationDTOService = scopeRelationDTOService;
		this.utilsService = utilsService;
	}

	@Operation(summary = "Get parent relations", description = "Get the parent-child relations")
	@GetMapping("{scopePk}/relations")
	@ResponseStatus(HttpStatus.OK)
	public List<ScopeRelationDTO> getParentRelations(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		utilsService.checkNotNull(Scope.class, scope, scopePk);
		return scopeRelationDTOService.createDTOs(scope, currentActor(), currentActiveRoles(scope));
	}

	@Operation(summary = "Create a scope relation")
	@PostMapping("{scopePk}/relations")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public List<ScopeRelationDTO> createScopeRelation(
		@PathVariable final Long scopePk,
		@Valid @RequestBody final ScopeRelationCreationDTO newRelation
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var parent = scopeDAOService.getScopeByPk(newRelation.parentPk());

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Scope.class, parent, newRelation.parentPk());

		final var acl = rightsService.getACL(currentActor(), scope);
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		acl.checkRight(scope.getScopeModel(), Rights.WRITE);
		acl.checkRight(parent.getScopeModel(), Rights.WRITE);

		// TODO Use the @Valid validation for the request body
		final Optional<ZonedDateTime> endDate = newRelation.endDate() == null ? Optional.empty() : Optional.of(newRelation.endDate());
		scopeRelationService.createRelation(scope, parent, newRelation.startDate(), endDate, currentContext(), "Create scope relation");

		return scopeRelationDTOService.createDTOs(scope, currentActor, currentRoles);
	}

	@Operation(summary = "Make relation default")
	@PutMapping("{scopePk}/relations/{relationPk}/default")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public List<ScopeRelationDTO> makeRelationDefault(
		@PathVariable final Long scopePk,
		@PathVariable final Long relationPk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final var relation = scopeRelationService.getRelationByPk(relationPk);
		utilsService.checkNotNull(ScopeRelation.class, relation, relationPk);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.WRITE);

		scopeRelationService.makeDefault(relation, currentContext(), "Make the relation default");

		return scopeRelationDTOService.createDTOs(scope, currentActor, currentRoles);
	}

	@Operation(summary = "End a relation")
	@PutMapping("{scopePk}/relations/{relationPk}/end")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public List<ScopeRelationDTO> endRelation(
		@PathVariable final Long scopePk,
		@PathVariable final Long relationPk,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime date
	) {
		// Convert the requestParam date to UTC
		final var endDate = date.withZoneSameInstant(ZoneId.systemDefault());
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.WRITE);

		final var relation = scopeRelationService.getRelationByPk(relationPk);
		scopeRelationService.endRelation(relation, endDate, currentContext(), "End a relation");

		return scopeRelationDTOService.createDTOs(scope, currentActor, currentRoles);
	}

	@Operation(summary = "Transfer a scope", description = "Transfer a scope from one parent scope to another")
	@PostMapping("{scopePk}/relations/transfer")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public List<ScopeRelationDTO> transfer(
		@PathVariable final Long scopePk,
		@Valid @RequestBody final ScopeRelationCreationDTO newRelation
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		final var oldParent = scopeRelationService.getDefaultParent(scope);
		final var newParent = scopeDAOService.getScopeByPk(newRelation.parentPk());

		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles(scope);

		utilsService.checkNotNull(Scope.class, scope, scopePk);
		utilsService.checkNotNull(Scope.class, oldParent, oldParent.getPk());
		utilsService.checkNotNull(Scope.class, newParent, newRelation.parentPk());

		rightsService.checkRight(currentActor, currentRoles, scope.getScopeModel(), Rights.WRITE);
		rightsService.checkRight(currentActor, currentRoles, oldParent.getScopeModel(), Rights.WRITE);
		rightsService.checkRight(currentActor, currentRoles, newParent.getScopeModel(), Rights.WRITE);

		scopeRelationService.transfer(scope, newParent, newRelation.startDate(), currentContext());

		return scopeRelationDTOService.createDTOs(scope, currentActor, currentRoles);
	}

	@Operation(summary = "Get parent scopes for a given scope model")
	@GetMapping("relations/available-parents")
	@ResponseStatus(HttpStatus.OK)
	public List<ScopeDTO> getParents(
		@RequestParam final String scopeModelId,
		@RequestParam final Rights right,
		@RequestParam final boolean onlyDefault
	) {
		final var currentRoles = currentActiveRoles();

		final var scopeModel = studyService.getStudy().getScopeModel(scopeModelId);

		if(scopeModel.isRoot()) {
			return Collections.emptyList();
		}

		//retrieve parent scope models
		final var scopeModelIds = onlyDefault ? Collections.singleton(scopeModel.getDefaultParentId()) : scopeModel.getParentIds();

		final var rootScopesPks = rightsService.filterRoles(currentRoles, scopeModel, right).stream().map(Role::getScopeFk).collect(Collectors.toSet());
		final List<Scope> scopes = scopeDAOService.getScopesByScopeModelIdHavingAncestor(scopeModelIds, rootScopesPks).stream()
			//when asking for parent to create a scope, do a special filter
			.filter(s -> Rights.WRITE != right || s.canEnroll())
			.sorted(Comparator.comparing(Scope::getCode))
			.toList();

		final var acl = rightsService.getACL(currentActor());
		return scopeDTOService.createDTOs(scopes, acl);
	}

	@Operation(summary = "Get default parent scope")
	@GetMapping("{scopePk}/parents/default")
	@ResponseStatus(HttpStatus.OK)
	public ScopeDTO getDefaultParentScope(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		utilsService.checkNotNull(Scope.class, scope, scopePk);

		if(scopeService.isRootScope(scope)) {
			return null;
		}

		final var parentScope = scopeRelationService.getDefaultParent(scope);
		final var acl = rightsService.getACL(currentActor(), parentScope);

		return scopeDTOService.createDTO(parentScope, acl);
	}

	@Operation(summary = "Get ancestor scopes", description = "Get all the scopes that are above in the scopes hierarchy")
	@GetMapping("{scopePk}/ancestors")
	@ResponseStatus(HttpStatus.OK)
	public List<ScopeDTO> getScopeAncestors(
		@PathVariable final Long scopePk,
		@RequestParam(value = "onlyDefault", defaultValue = "false") final Boolean onlyDefault
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);

		final List<Scope> ancestors;
		if(onlyDefault) {
			ancestors = scopeRelationService.getDefaultAncestors(scope);
		}
		else {
			ancestors = scopeRelationService.getAncestors(scope);
		}
		return scopeDTOService.createDTOs(ancestors, acl);
	}

	@Deprecated
	// TODO replace by a search call
	@Operation(summary = "Get children scopes", description = "Get all the scopes that are below in the scope hierarchy")
	@GetMapping("{scopePk}/children")
	@ResponseStatus(HttpStatus.OK)
	public List<ScopeDTO> getChildrenScopes(
		@PathVariable final Long scopePk,
		@RequestParam final Optional<String> scopeModelId,
		@RequestParam(defaultValue = "false") final Boolean enabled
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		utilsService.checkNotNull(Scope.class, scope, scopePk);

		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);

		final Collection<Scope> children;
		if(enabled) {
			children = scopeRelationService.getEnabledChildren(scope, ZonedDateTime.now());
		}
		else {
			children = scopeRelationService.getAllChildren(scope);
		}

		// Restrict to children of the required scope model
		final var scopes = children.stream()
			.filter(s -> scopeModelId.isEmpty() || scopeModelId.get().equals(s.getScopeModelId()))
			.toList();

		return scopeDTOService.createDTOs(scopes, acl);
	}

	@Deprecated
	// TODO replace by a search call
	@Operation(summary = "Get scope leaves", description = "Get the leaves of the scope hierarchy")
	@GetMapping("{scopePk}/leaves")
	@ResponseStatus(HttpStatus.OK)
	public List<ScopeDTO> getScopeDescendants(
		@PathVariable("scopePk") final Long scopePk,
		@RequestParam final Boolean includeRemoved
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);
		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();
		acl.checkRight(leafScopeModel, Rights.READ);

		final Collection<Scope> leaves;
		if(includeRemoved) {
			leaves = scopeRelationService.getAllEnabledDescendants(scope, leafScopeModel);
		}
		else {
			leaves = scopeRelationService.getEnabledDescendants(scope, leafScopeModel);
		}
		return scopeDTOService.createDTOs(leaves, acl);
	}
}
