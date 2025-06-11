package ch.rodano.core.services.dao.chart.data;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.dao.chart.ChartDTO;
import ch.rodano.core.services.dao.scope.ScopeDAOService;

@Service
public class EnrollmentByScopeChartService {

	private final ScopeRelationService scopeRelationService;
	private final ScopeDAOService scopeDAOService;

	public EnrollmentByScopeChartService(
		final ScopeRelationService scopeRelationService,
		final ScopeDAOService scopeDAOService
	) {
		this.scopeRelationService = scopeRelationService;
		this.scopeDAOService = scopeDAOService;
	}

	public List<ChartDTO.ChartDataSeries> generateValues(
		final String leafScopeModelId,
		final String scopeModelId
	) {

		final var containerScopes = scopeDAOService.getScopesByScopeModelId(scopeModelId);

		final List<List<Object>> values = new ArrayList<>();

		for (var container : containerScopes) {
			final var descendants = scopeRelationService.getEnabledDescendants(container);

			final var count = descendants.stream()
				.filter(desc -> leafScopeModelId.equals(desc.getScopeModelId()))
				.count();

			if (count > 0) {
				values.add(List.of(container.getCode(), (int) count));
			}
		}

		final var label = "ENROLLMENT_BY_" + scopeModelId;
		return List.of(new ChartDTO.ChartDataSeries(label, values));
	}
}

