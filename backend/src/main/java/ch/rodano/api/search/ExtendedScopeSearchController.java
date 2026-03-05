package ch.rodano.api.search;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.feature.FeatureStatic;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.model.scope.ScopeSortBy;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.utils.RightsService;


@Tag(name = "Search")
@RestController
@RequestMapping(value = "/extended-search")
@Transactional(readOnly = true)
public class ExtendedScopeSearchController extends AbstractSecuredController {
	private static final Logger LOG = LoggerFactory.getLogger(ExtendedScopeSearchController.class);

	final Integer defaultPageSize;
	final ExtendedScopeResultService extendedScopeResultService;
	private final ObjectMapper mapper;
	private final ScopeService scopeService;


	public ExtendedScopeSearchController(final RequestContextService requestContextService,
					     final StudyService studyService,
					     final ActorService actorService,
					     final RoleService roleService,
					     final RightsService rightsService,
					     final ScopeService scopeService,
					     final ExtendedScopeResultService extendedScopeResultService,
					     final ObjectMapper mapper,
					     @Value("${rodano.pagination.maximum-page-size}") final Integer defaultPageSize
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.defaultPageSize = defaultPageSize;
		this.extendedScopeResultService = extendedScopeResultService;
		this.mapper = mapper;
		this.scopeService = scopeService;
	}

	private <T> T readFromURI(final String input, final JavaType type) {
		final var string = UriUtils.decode(input, "UTF-8");
		try {
			return mapper.readValue(string, type);
		}
		catch (final JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@GetMapping
	public PagedResult<ExtendedScopeSearchResultDTO> search(
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
		@Parameter(description = "Include removed (deleted) scopes? Requires MANAGE_DELETED_DATA feature") @RequestParam final Optional<Boolean> includeDeleted,
		@Parameter(description = "Order the results by which property?") @RequestParam final Optional<String> sortBy,
		@Parameter(description = "Use the ascending order?") @RequestParam final Optional<Boolean> orderAscending,
		@Parameter(description = "Page size") @RequestParam final Optional<Integer> pageSize,
		@Parameter(description = "Page index") @RequestParam final Optional<Integer> pageIndex
	) {

		final var acl = rightsService.getACL(currentActor());
		final var currentRoles = currentActiveRoles();

		// Determine whether removed scopes should be included. Only allow including removed scopes
		// when the actor actually has the MANAGE_DELETED_DATA right.
		final var canManageDeleted = acl.hasRight(FeatureStatic.MANAGE_DELETED_DATA);
		final boolean includeDeletedFinal = includeDeleted.map(val -> canManageDeleted && val).orElse(canManageDeleted);

		final var stateType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, List.class);
		final Optional<Map<String, List<String>>> workflowStatesMap = workflowStates.map(s -> readFromURI(s, stateType));
		final var criteriaType = TypeFactory.defaultInstance().constructCollectionType(List.class, FieldModelCriterion.class);
		final Optional<List<FieldModelCriterion>> fieldModelCriterionList = fieldModelCriteria.map(s -> readFromURI(s, criteriaType));

		LOG.info(scopeModelId.isEmpty() ? "Searching scopes with provided criteria" : "Searching scopes of model {} with provided criteria", scopeModelId.orElse(""));

		final var search = new ScopeSearch()
			.setCode(code.filter(StringUtils::isNotBlank))
			.setIds(ids)
			.setPks(pks)
			.setParentPks(parentPks)
			.setAncestorPks(ancestorPks)
			.setScopeModelId(scopeModelId)
			.setScopeModelAncestorPks(scopeService.buildActorRightPredicate(currentRoles, scopeModelId))
			.setWorkflowStates(workflowStatesMap)
			.setFieldModelCriteria(fieldModelCriterionList)
			.setLeaf(leaf)
			.setFullText(fullText.filter(StringUtils::isNotBlank))
			.setIncludeDeleted(includeDeletedFinal)
			.setPageSize(pageSize.isEmpty() ? Optional.of(defaultPageSize) : pageSize)
			.setPageIndex(pageIndex.isEmpty() ? Optional.of(0) : pageIndex)
			.setScopeModelId(scopeModelId.isEmpty()? Optional.ofNullable(studyService.getStudy().getLeafScopeModel().getId()) : scopeModelId);

		//set sort if provided
		sortBy.ifPresent(sort -> {
			if (Arrays.stream(ScopeSortBy.class.getEnumConstants()).anyMatch(e -> e.name().equals(sort))) {
				search.setSortBy(ScopeSortBy.valueOf(sort));
			}
			else {
				search.setExtendedSortBy(sort);
			}
		});
		orderAscending.map(search::setSortAscending);

		return extendedScopeResultService.search(search);
	}

}
