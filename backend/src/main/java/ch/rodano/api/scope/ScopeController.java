package ch.rodano.api.scope;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.config.EventModelDTO;
import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.RationaleDTO;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.exception.http.BadArgumentException;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.export.ExportFormat;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.model.scope.ScopeSortBy;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.export.scope.ScopeExportService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.study.SubstudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;
import ch.rodano.core.utils.UtilsService;

@Tag(name = "Scope")
@RestController
@RequestMapping("/scopes")
@Validated
@Transactional(readOnly = true)
public class ScopeController extends AbstractSecuredController {
	private final SubstudyService substudyService;
	private final ScopeService scopeService;
	private final ScopeExportService scopeExportService;
	private final ScopeDTOService scopeDTOService;
	private final ScopeRelationService scopeRelationService;
	private final ObjectMapper mapper;
	private final EventService eventService;
	private final UtilsService utilsService;
	private final ScopeDAOService scopeDAOService;

	private final Integer defaultPageSize;

	public ScopeController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final SubstudyService substudyService,
		final ScopeService scopeService,
		final ScopeExportService scopeExportService,
		final ScopeDTOService scopeDTOService,
		final ScopeRelationService scopeRelationService,
		final ObjectMapper mapper,
		final EventService eventService,
		final UtilsService utilsService,
		final ScopeDAOService scopeDAOService,
		@Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.substudyService = substudyService;
		this.scopeService = scopeService;
		this.scopeExportService = scopeExportService;
		this.scopeDTOService = scopeDTOService;
		this.scopeRelationService = scopeRelationService;
		this.mapper = mapper;
		this.eventService = eventService;
		this.utilsService = utilsService;
		this.scopeDAOService = scopeDAOService;
		this.defaultPageSize = defaultPageSize;
	}

	@Operation(summary = "Get a scope")
	@GetMapping("{scopePk}")
	@ResponseStatus(HttpStatus.OK)
	public ScopeDTO getScope(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.READ);

		return scopeDTOService.createDTO(scope, acl);
	}

	@Operation(summary = "Search for scopes", description = "List all scopes depending on given criteria")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public PagedResult<ScopeDTO> search(
		@Parameter(description = "Scope model ID") @RequestParam final Optional<String> scopeModelId,
		@Parameter(description = "Full text search on code, shortname and longname") @RequestParam final Optional<String> fullText,
		@Parameter(description = "Scope code") @RequestParam final Optional<String> code,
		@Parameter(description = "Scope IDs") @RequestParam final Optional<List<String>> ids,
		@Parameter(description = "Scope PKs") @RequestParam final Optional<List<Long>> pks,
		@Parameter(description = "Scope parent PKs") @RequestParam final Optional<List<Long>> parentPks,
		@Parameter(description = "Scope ancestor PKs") @RequestParam final Optional<List<Long>> ancestorPks,
		@Parameter(description = "Scope workflow states") @RequestParam final Optional<String> workflowStates,
		@Parameter(description = "Field model criteria in a serialized form") @RequestParam final Optional<String> fieldModelCriteria,
		@Parameter(description = "Only include the leaf scopes?") @RequestParam final Optional<Boolean> leaf,
		@Parameter(description = "Order the results by which property?") @RequestParam final Optional<ScopeSortBy> sortBy,
		@Parameter(description = "Use the ascending order?") @RequestParam final Optional<Boolean> orderAscending,
		@Parameter(description = "Page size") @RequestParam final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam final Optional<Integer> pageIndex
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		final var acl = rightsService.getACL(currentActor);

		final var stateType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, List.class);
		final Optional<Map<String, List<String>>> workflowStatesMap = workflowStates.map(s -> readFromURI(s, stateType));
		final var criteriaType = TypeFactory.defaultInstance().constructCollectionType(List.class, FieldModelCriterion.class);
		final Optional<List<FieldModelCriterion>> fieldModelCriterionList = fieldModelCriteria.map(s -> readFromURI(s, criteriaType));

		final var search = new ScopeSearch()
			.setCode(code.filter(StringUtils::isNotBlank))
			.setIds(ids)
			.setPks(pks)
			.setParentPks(parentPks)
			.setAncestorPks(ancestorPks)
			.setScopeModelId(scopeModelId)
			//hard-code filter in predicate according to actor rights
			.setScopeModelAncestorPks(buildActorRightPredicate(currentRoles, scopeModelId))
			.setWorkflowStates(workflowStatesMap) //this makes the Optional<> method useless
			.setFieldModelCriteria(fieldModelCriterionList) // here too
			.setLeaf(leaf)
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			.setIncludeDeleted(acl.hasRight(FeatureStatic.MANAGE_DELETED_DATA))
			.setPageSize(pageSize.isEmpty() ? Optional.of(defaultPageSize) : pageSize)
			.setPageIndex(pageIndex.isEmpty() ? Optional.of(0) : pageIndex);
		//set sort if provided
		sortBy.map(search::setSortBy);
		orderAscending.map(search::setSortAscending);

		//rights on each scope will be checked while creating the DTOs
		return scopeService.search(search).withObjectsTransformation(s -> scopeDTOService.createDTOs(s, acl));
	}

	@Operation(summary = "Export scopes in CSV format")
	@GetMapping("export")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<StreamingResponseBody> export(
		@Parameter(description = "Scope model ID") @RequestParam final Optional<String> scopeModelId,
		@Parameter(description = "Full text search on code, shortname and longname") @RequestParam final Optional<String> fullText,
		@Parameter(description = "Scope code") @RequestParam final Optional<String> code,
		@Parameter(description = "Scope IDs") @RequestParam final Optional<List<String>> ids,
		@Parameter(description = "Scope PKs") @RequestParam final Optional<List<Long>> pks,
		@Parameter(description = "Scope parent PKs") @RequestParam final Optional<List<Long>> parentPks,
		@Parameter(description = "Scope ancestor PKs") @RequestParam final Optional<List<Long>> ancestorPks,
		@Parameter(description = "Scope workflow states") @RequestParam final Optional<String> workflowStates,
		@Parameter(description = "Field model criteria in a serialized form") @RequestParam final Optional<String> fieldModelCriteria,
		@Parameter(description = "Only include the leaf scopes?") @RequestParam final Optional<Boolean> leaf
	) {
		final var currentActor = currentActor();
		final var currentRoles = currentActiveRoles();
		final var acl = rightsService.getACL(currentActor);

		final var stateType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, List.class);
		final Optional<Map<String, List<String>>> workflowStatesMap = workflowStates.map(s -> readFromURI(s, stateType));
		final var criteriaType = TypeFactory.defaultInstance().constructCollectionType(List.class, FieldModelCriterion.class);
		final Optional<List<FieldModelCriterion>> fieldModelCriterionList = fieldModelCriteria.map(s -> readFromURI(s, criteriaType));

		final var predicate = new ScopeSearch()
			.setCode(code.filter(StringUtils::isNotBlank))
			.setIds(ids)
			.setPks(pks)
			.setParentPks(parentPks)
			.setAncestorPks(ancestorPks)
			.setScopeModelId(scopeModelId)
			//hard-code filter in predicate according to actor rights
			.setScopeModelAncestorPks(buildActorRightPredicate(currentRoles, scopeModelId))
			.setWorkflowStates(workflowStatesMap) //this makes the Optional<> method useless
			.setFieldModelCriteria(fieldModelCriterionList) // here too
			.setLeaf(leaf)
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			.setIncludeDeleted(acl.hasRight(FeatureStatic.MANAGE_DELETED_DATA))
			.setSortBy(ScopeSearch.DEFAULT_SORT_BY)
			.setSortAscending(ScopeSearch.DEFAULT_SORT_ASCENDING);

		final var languages = actorService.getLanguages(currentActor);

		//return response
		final StreamingResponseBody stream = os -> scopeExportService.exportScopes(os, predicate, languages);
		final var exportLabel = scopeModelId.map(s -> studyService.getStudy().getScopeModel(s).getLocalizedPluralShortname(languages).toLowerCase()).orElse("scopes");
		final var filename = studyService.getStudy().generateFilename(exportLabel, ExportFormat.CSV);
		return exportResponse(ExportFormat.CSV, stream, filename);
	}

	@Operation(summary = "Create a candidate scope", description = "Provides a scope skeleton from which an actual scope can be created")
	@GetMapping("candidate")
	@ResponseStatus(HttpStatus.OK)
	public ScopeCandidateDTO createCandidateScope(
		@RequestParam final Long parentScopePk,
		@RequestParam final String scopeModelId
	) {
		final var parentScope = scopeDAOService.getScopeByPk(parentScopePk);
		final var selectedScopeModel = studyService.getStudy().getScopeModel(scopeModelId);

		utilsService.checkNotNull(Scope.class, parentScope, parentScopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), parentScope);
		acl.checkRight(selectedScopeModel, Rights.WRITE);

		final var candidateScope = scopeService.createCandidate(selectedScopeModel, ZonedDateTime.now(), parentScope);
		return new ScopeCandidateDTO(parentScopePk, candidateScope);
	}

	@Operation(summary = "Create a scope")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public ScopeDTO createScope(
		@Valid @RequestBody final ScopeCandidateDTO scopeDTO
	) {
		final var parentScope = scopeDAOService.getScopeByPk(scopeDTO.parentScopePk());

		utilsService.checkNotNull(Scope.class, parentScope, scopeDTO.parentScopePk());

		final var scopeModel = studyService.getStudy().getScopeModel(scopeDTO.modelId());

		//check rights
		final var acl = rightsService.getACL(currentActor(), parentScope);
		acl.checkRight(scopeModel, Rights.WRITE);

		final var candidateScope = scopeDTOService.generateScope(scopeDTO);
		candidateScope.setScopeModel(scopeModel);

		// Set the virtuality of the scope as defined by its scope model
		candidateScope.setVirtual(scopeModel.isVirtual());

		// Create a new scope
		final var newScope = scopeService.createFromCandidate(candidateScope, scopeModel, parentScope, currentContext(), null);

		//acl for the newly created scope are the same as for the parent scope
		return scopeDTOService.createDTO(newScope, acl);
	}

	@Operation(summary = "Remove scope")
	@PutMapping("{scopePk}/remove")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ScopeDTO removeScope(
		@PathVariable("scopePk") final Long scopePk,
		@Valid @RequestBody final RationaleDTO rationale
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		scopeService.delete(scope, currentContext(), rationale.message());

		return scopeDTOService.createDTO(scope, acl);
	}

	@Operation(summary = "Restore scope")
	@PutMapping("{scopePk}/restore")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ScopeDTO restoreScope(
		@PathVariable("scopePk") final Long scopePk,
		@Valid @RequestBody final RationaleDTO rationale
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(FeatureStatic.MANAGE_DELETED_DATA);
		//no need to check if actor has the right to write the scope (MANAGE_DELETED_DATA is sufficient and surpass WRITE scope model right)

		scopeService.restore(scope, currentContext(), rationale.message());

		return scopeDTOService.createDTO(scope, acl);
	}

	@Operation(summary = "Update scope")
	@PutMapping("{scopePk}")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ScopeDTO updateScope(
		@PathVariable final Long scopePk,
		@Valid @RequestBody final ScopeDTO scopeDTO
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		//update the existing scope with the data from the DTO
		scopeDTOService.updateScope(scope, scopeDTO);
		scopeService.save(scope, currentContext(), "Update scope");

		return scopeDTOService.createDTO(scope, acl);
	}

	@Operation(summary = "Auto enroll", description = "Enroll a scope in a substudy")
	@PostMapping("{scopePk}/enrollment/auto")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public void autoEnroll(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		substudyService.enrollScopesInSubstudy(scope, currentContext(), "Auto enrollment");
	}

	// TODO leave is a more appropriate verb
	@Operation(summary = "Clean enroll", description = "Un-enroll a scope from a substudy")
	@PostMapping("{scopePk}/enrollment/clean")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public void cleanEnroll(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		for(final var relation : scopeRelationService.getChildRelations(scope)) {
			scopeRelationService.endRelation(relation, ZonedDateTime.now(), currentContext(), "Clean enrollment");
		}
	}

	@Operation(summary = "Count enroll-able scopes")
	@PostMapping("{scopePk}/enrollment/count")
	@ResponseStatus(HttpStatus.OK)
	public Integer countEnrollableScopes(
		@PathVariable final Long scopePk,
		@Valid @RequestBody final List<FieldModelCriterion> criteria
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//only virtual scope can have an enrollment
		if(!scope.getScopeModel().isVirtual()) {
			throw new BadArgumentException("Cannot count enrollment for non virtual scope");
		}

		for(final var criterion : criteria) {
			if(!criterion.isValid() || !criterion.hasValidValue(studyService.getStudy())) {
				throw new BadArgumentException(String.format("The following enrollment criterion is invalid: %s", criterion));
			}
		}

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(scope.getScopeModel(), Rights.WRITE);

		final var targetScopeModel = scope.getScopeModel().getDescendantsScopeModel().get(0);
		return substudyService.findPotentialScopes(Collections.singleton(scopeService.getRootScope()), targetScopeModel, criteria).size();
	}

	@Operation(summary = "Lock scope")
	@PutMapping("{scopePk}/lock")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ScopeDTO lockScope(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(FeatureStatic.LOCK);

		scopeService.lock(scope, currentContext());

		return scopeDTOService.createDTO(scope, acl);
	}

	@Operation(summary = "Unlock scope")
	@PutMapping("{scopePk}/unlock")
	@ResponseStatus(HttpStatus.OK)
	@Transactional
	public ScopeDTO unlockScope(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		acl.checkRight(FeatureStatic.LOCK);

		scopeService.unlock(scope, currentContext());

		return scopeDTOService.createDTO(scope, acl);
	}

	@Operation(summary = "Get available event models", description = "Return a list of all event models that can be created for a scope")
	@GetMapping("{scopePk}/available-event-models")
	@ResponseStatus(HttpStatus.OK)
	public List<EventModelDTO> getAvailableEventModels(
		@PathVariable final Long scopePk
	) {
		final var scope = scopeDAOService.getScopeByPk(scopePk);

		utilsService.checkNotNull(Scope.class, scope, scopePk);

		//check rights
		final var acl = rightsService.getACL(currentActor(), scope);
		if(!acl.hasRight(scope.getScopeModel(), Rights.READ)) {
			return Collections.emptyList();
		}

		return eventService.getEventModels(scope).stream()
			.filter(e -> !e.isPreventAdd())
			.filter(e -> acl.hasRight(e, Rights.WRITE))
			.map(EventModelDTO::new)
			.toList();
	}

	private <T> T readFromURI(final String input, final JavaType type) {
		final var string = UriUtils.decode(input, "UTF-8");
		try {
			return mapper.readValue(string, type);
		}
		catch(final JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	//TODO move this in the scope service
	private Optional<Map<String, List<Long>>> buildActorRightPredicate(final List<Role> roles, final Optional<String> scopeModelId) {
		//if a scope model is provided, hard-code ancestors pks
		if(scopeModelId.isPresent()) {
			//retrieve all ancestors on which current actor has the right to read scope having specified scope model
			final List<Long> ancestorFks = rightsService.filterRoles(roles, studyService.getStudy().getScopeModel(scopeModelId.get()), Rights.READ).stream()
				.map(Role::getScopeFk)
				.distinct()
				.toList();
			return Optional.of(Collections.singletonMap(scopeModelId.get(), ancestorFks));
		}
		//otherwise, return all couples scope model / list of ancestors that the provided actor has the right to READ
		final Map<String, List<Long>> scopeModelAncestorPks = new HashMap<>();
		for(final var role : roles) {
			final var roleScopeModelIds = role.getProfile().getScopeModels(Rights.READ).stream().map(ScopeModel::getId).toList();
			for(final String roleScopeModelId : roleScopeModelIds) {
				scopeModelAncestorPks.putIfAbsent(roleScopeModelId, new ArrayList<>());
				scopeModelAncestorPks.get(roleScopeModelId).add(role.getScopeFk());
			}
		}
		return Optional.of(scopeModelAncestorPks);
	}

}
