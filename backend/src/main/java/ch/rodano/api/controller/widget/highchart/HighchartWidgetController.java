package ch.rodano.api.controller.widget.highchart;

import java.util.ArrayList;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import ch.rodano.api.controller.AbstractSecuredController;
import ch.rodano.api.request.context.RequestContextService;
import ch.rodano.configuration.model.predicate.ValueSource;
import ch.rodano.core.model.graph.highchart.instance.HighChartFactoryService;
import ch.rodano.core.model.graph.highchart.instance.HighGraph;
import ch.rodano.core.model.scope.FieldModelCriterion;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.actor.ActorService;
import ch.rodano.core.services.bll.role.RoleService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.utils.RightsService;

@Tag(name = "Widgets")
@RestController
@RequestMapping("/widget/highchart")
@Transactional(readOnly = true)
public class HighchartWidgetController extends AbstractSecuredController {
	private final HighChartFactoryService highChartFactoryService;
	private final ObjectMapper mapper;
	private final ScopeDAOService scopeDAOService;

	public HighchartWidgetController(
		final RequestContextService requestContextService,
		final StudyService studyService,
		final ActorService actorService,
		final RoleService roleService,
		final RightsService rightsService,
		final HighChartFactoryService highChartFactoryService,
		final ObjectMapper mapper,
		final ScopeDAOService scopeDAOService
	) {
		super(requestContextService, studyService, actorService, roleService, rightsService);
		this.highChartFactoryService = highChartFactoryService;
		this.mapper = mapper;
		this.scopeDAOService = scopeDAOService;
	}

	@Operation(summary = "Get highchart widget")
	@GetMapping("{chartId}")
	@ResponseStatus(HttpStatus.OK)
	public HighGraph getHighchart(
		@PathVariable final String chartId,
		@RequestParam final Optional<List<Long>> scopePks,
		@RequestParam final Optional<String> fieldModelCriteria
	) throws Exception {
		final var currentActor = currentActor();
		final var study = studyService.getStudy();

		//retrieve chart configuration
		final var configuration = study.getChart(chartId);

		//retrieve root scopes
		//TODO check rights
		final List<Scope> scopes = new ArrayList<>();
		if(scopePks.isPresent()) {
			scopes.addAll(scopeDAOService.getScopesByPks(scopePks.get()));
		}
		else {
			scopes.addAll(actorService.getRootScopes(currentActor));
		}

		//retrieve chart criteria
		final List<ValueSource> criteriaValueSources = new ArrayList<>();
		if(fieldModelCriteria.isPresent()) {
			final List<FieldModelCriterion> criteria = mapper.readValue(fieldModelCriteria.get(), TypeFactory.defaultInstance().constructCollectionType(List.class, FieldModelCriterion.class));
			for(final var criterion : criteria) {
				criteriaValueSources.add(criterion.getValueSource(study));
			}
		}

		return highChartFactoryService.getChart(configuration, study.getDefaultLanguageId(), scopes, criteriaValueSources);
	}
}
