package ch.rodano.core.services.bll.widget.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.rodano.configuration.model.chart.Chart;
import ch.rodano.configuration.model.chart.ChartRange;
import ch.rodano.configuration.model.chart.ChartType;
import ch.rodano.configuration.model.language.LanguageStatic;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.test.DatabaseTest;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Charts")
@SpringTestConfiguration
public class ChartServiceTest extends DatabaseTest {

	private static final String DEFAULT_LANGUAGE = LanguageStatic.en.name();
	private static final String[] LANGUAGES = new String[] { DEFAULT_LANGUAGE };

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ScopeDAOService scopeDAOService;

	@Autowired
	private EnrollmentChartFactoryService enrollmentChartFactoryService;

	@Autowired
	private EnrollmentByScopeFactoryService enrollmentByScopeFactoryService;

	@Autowired
	private WorkflowStatusChartFactoryService workflowStatusChartFactoryService;

	@Autowired
	private StatisticsChartFactoryService statisticsChartFactoryService;

	@Test
	void testEnrollmentByScopeChart() {
		final var scopeModel = studyService.getStudy().getScopeModel("PATIENT");

		final var chartModel = new Chart();
		chartModel.setStudy(studyService.getStudy());
		chartModel.setType(ChartType.ENROLLMENT_BY_SCOPE);
		chartModel.setScopeModelId("CENTER");
		chartModel.setLeafScopeModelId(scopeModel.getId());

		final var datasets = enrollmentByScopeFactoryService.buildChartDatasets(chartModel, LANGUAGES, Collections.singleton(scopeService.getRootScope()));
		assertNotNull(datasets);
		assertEquals(1, datasets.size());

		final var dataset = datasets.getFirst();
		assertEquals(scopeModel.getLocalizedPluralShortname(DEFAULT_LANGUAGE), dataset.label());

		final var points = dataset.data();
		assertEquals(4, points.size());

		assertEquals((Integer) 1, dataset.getValue("AT-01").orElseThrow());
		assertEquals((Integer) 0, dataset.getValue("AT-02").orElseThrow());
		assertEquals((Integer) 2, dataset.getValue("FR-01").orElseThrow());
		assertEquals((Integer) 1, dataset.getValue("FR-02").orElseThrow());
	}

	@Test
	void testEnrollmentStatusChart() {
		final var chartModel = new Chart();
		chartModel.setStudy(studyService.getStudy());
		chartModel.setType(ChartType.ENROLLMENT);
		chartModel.setLeafScopeModelId("PATIENT");

		final var rootScope = scopeService.getRootScope();

		final var datasets = enrollmentChartFactoryService.buildChartDatasets(chartModel, Collections.singleton(rootScope));
		assertNotNull(datasets);
		assertEquals(1, datasets.size());

		final var dataset = datasets.getFirst();
		assertEquals(rootScope.getCode(), dataset.label());

		final var points = dataset.data();
		//there are 4 patients in the test database
		//do not try to check the number of entries as the creation date for the patient is randomly generated
		assertEquals((Integer) 4, points.getLast().y());
	}

	@Test
	void testStatisticsChart() {
		final var chartModel = new Chart();
		chartModel.setStudy(studyService.getStudy());
		chartModel.setType(ChartType.STATISTICS);
		chartModel.setLeafScopeModelId("PATIENT");
		chartModel.setDatasetModelId("PATIENT_DOCUMENTATION");
		chartModel.setFieldModelId("GENDER");

		final var scopes = new ArrayList<Scope>();
		scopes.add(scopeService.getRootScope());

		var datasets = statisticsChartFactoryService.buildChartDatasets(chartModel, LANGUAGES, scopes, Collections.emptyList());
		assertNotNull(datasets);
		assertEquals(1, datasets.size());

		final var dataset = datasets.getFirst();
		assertEquals(scopes.getFirst().getCodeAndShortname(), dataset.label());

		final var points = dataset.data();
		assertEquals(3, points.size());

		assertEquals((Integer) 1, dataset.getValue("MALE").orElseThrow());
		assertEquals((Integer) 1, dataset.getValue("FEMALE").orElseThrow());
		assertEquals((Integer) 2, dataset.getValue("").orElseThrow());

		//statistics for 2 scopes
		scopes.add(scopeDAOService.getScopeByCode("FR"));

		datasets = statisticsChartFactoryService.buildChartDatasets(chartModel, LANGUAGES, scopes, Collections.emptyList());
		assertNotNull(datasets);
		assertEquals(2, datasets.size());

		assertEquals(3, datasets.get(0).data().size());
		assertEquals(3, datasets.get(1).data().size());

	}

	@Test
	void testStatisticsChartWithCategories() {
		final var chartModel = new Chart();
		chartModel.setStudy(studyService.getStudy());
		chartModel.setType(ChartType.STATISTICS);
		chartModel.setLeafScopeModelId("PATIENT");
		chartModel.setDatasetModelId("PATIENT_DOCUMENTATION");
		chartModel.setFieldModelId("AGE_AT_DIAGNOSIS");

		// Define ranges
		final var ranges = new ArrayList<ChartRange>();
		ranges.add(new ChartRange(Map.of(DEFAULT_LANGUAGE, "0-18"), 0d, 18d));
		ranges.add(new ChartRange(Map.of(DEFAULT_LANGUAGE, "19-40"), 19d, 40d));
		ranges.add(new ChartRange(Map.of(DEFAULT_LANGUAGE, "41+"), 41d, 200d));
		chartModel.setRanges(ranges);

		final var scopes = new ArrayList<Scope>();
		scopes.add(scopeService.getRootScope());

		var datasets = statisticsChartFactoryService.buildChartDatasets(chartModel, LANGUAGES, scopes, Collections.emptyList());
		assertNotNull(datasets);
		assertEquals(1, datasets.size());

		var dataset = datasets.getFirst();
		assertEquals(scopes.getFirst().getCodeAndShortname(), dataset.label());

		var points = dataset.data();
		assertEquals(3, points.size());

		//no age is set when the database is initialized
		assertEquals((Integer) 0, dataset.getValue("0-18").orElseThrow());
		assertEquals((Integer) 0, dataset.getValue("19-40").orElseThrow());
		assertEquals((Integer) 0, dataset.getValue("41+").orElseThrow());

		//add an "other" category
		ranges.add(ChartRange.otherChartRange(Map.of(DEFAULT_LANGUAGE, "Other")));

		datasets = statisticsChartFactoryService.buildChartDatasets(chartModel, LANGUAGES, scopes, Collections.emptyList());
		assertNotNull(datasets);
		assertEquals(1, datasets.size());

		dataset = datasets.getFirst();

		points = dataset.data();
		assertEquals(4, points.size());

		//check the "other" category
		// It will have value 4 because in the beginning all 4 patients have "N/A" as AGE_AT_DIAGNOSIS
		assertEquals((Integer) 4, dataset.getValue("Other").orElseThrow());
	}

	@Test
	void testWorkflowStatusChart() {
		final var workflow = studyService.getStudy().getWorkflow("SIGNATURE");

		final var chartModel = new Chart();
		chartModel.setStudy(studyService.getStudy());
		chartModel.setType(ChartType.WORKFLOW_STATUS);
		chartModel.setWorkflowId(workflow.getId());
		chartModel.setIncludedStateIds(Set.of("SIGNED", "UNSIGNED"));

		final var datasets = workflowStatusChartFactoryService.buildChartDatasets(chartModel, LANGUAGES);
		assertNotNull(datasets);
		assertEquals(1, datasets.size());

		final var dataset = datasets.getFirst();
		assertEquals(workflow.getLocalizedShortname(DEFAULT_LANGUAGE), dataset.label());

		// There is 1 point per state
		final var points = dataset.data();
		assertEquals(2, points.size());
	}
}
