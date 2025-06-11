package ch.rodano.core.services.dao.chart.data;

import java.util.List;
import java.util.Map;

import ch.rodano.core.model.scope.FieldModelCriterion;

public interface ChartDataRepository {
	Object getEnrollmentStatusData(Map<String, Object> requestParams);
	Object getEnrollmentByScopeData(Map<String, Object> requestParams);
	Object getStatisticsData(Map<String, Object> requestParams, List<String> selectedRootScopeIds, List<FieldModelCriterion> criteria);
	Object getWorkflowStatusData(Map<String, Object> requestParams);
}
