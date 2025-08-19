package ch.rodano.api.controller.widget.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.chart.ChartType;
import ch.rodano.core.model.chart.ChartDTO;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.widget.chart.ChartFactoryService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Widgets")
@RestController
@RequestMapping("/widget/chart")
@Transactional(readOnly = true)
public class ChartWidgetController extends AbstractSecuredController {
	private final ObjectMapper mapper;
	private final ChartFactoryService chartFactoryService;
	private final ScopeService scopeService;
	private final ScopeDAOService scopeDAOService;

	public ChartWidgetController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final ChartFactoryService chartFactoryService,
		final ObjectMapper mapper,
		final ScopeService scopeService,
		final ScopeDAOService scopeDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.mapper = mapper;
		this.chartFactoryService = chartFactoryService;
		this.scopeService = scopeService;
		this.scopeDAOService = scopeDAOService;
	}

	@SuppressWarnings("unchecked")
	@Operation(summary = "Get chart widget")
	@GetMapping("{chartId}")
	@ResponseStatus(HttpStatus.OK)
	public ChartDTO<?, ?> getChart(
		@PathVariable final String chartId,
		@RequestParam final Optional<List<Long>> scopePks,
		@RequestParam(name = "criteria") final Optional<String> encodedCriteria
	) throws JsonMappingException, JsonProcessingException {
		final var currentActor = currentActor();
		final var study = studyService.getStudy();
		final var chart = study.getChart(chartId);

		//retrieve root scopes
		final List<Scope> scopes = new ArrayList<>();
		if(chart.getOverrideUserRights()) {
			scopes.add(scopeService.getRootScope());
		}
		else {
			if(scopePks.isPresent()) {
				scopes.addAll(scopeDAOService.getScopesByPks(scopePks.get()));
				for(final var scope : scopes) {
					final var currentRoles = currentActiveRoles(scope);
					switch(chart.getType()) {
						case ChartType.WORKFLOW_STATUS: {
							final var workflow = study.getWorkflow(chart.getWorkflowId());
							rightsService.checkRight(currentActor, currentRoles, workflow);
							break;
						}
						default:
							rightsService.checkRight(currentActor, currentRoles, scope);
					}
				}
			}
			else {
				scopes.addAll(actorService.getRootScopes(currentActor));
			}
		}

		//retrieve chart criteria
		List<FieldModelCriterion> criteria = Collections.emptyList();
		if(encodedCriteria.isPresent()) {
			criteria = (List<FieldModelCriterion>) mapper.readValue(encodedCriteria.get(), TypeFactory.defaultInstance().constructCollectionType(List.class, FieldModelCriterion.class));
		}

		return chartFactoryService.getChart(chart, currentActor, scopes, criteria);
	}
}
