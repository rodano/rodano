package ch.rodano.core.model.charts;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ch.rodano.core.services.dao.chart.ChartServiceImpl;
import ch.rodano.test.SpringTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringTestConfiguration
public class ChartServiceImplTest {

	private final ChartServiceImpl chartServiceImpl;

	@Autowired
	public ChartServiceImplTest(
		ChartServiceImpl chartServiceImpl
	) {
		this.chartServiceImpl = chartServiceImpl;
	}

	@Test
	void testEnrollmentByScopeChart() {
		var params = new HashMap<String, Object>();
		params.put("scopeModelId", "CENTER");
		params.put("leafScopeModelId", "PATIENT");
		params.put("chartType", "ENROLLMENT_BY_SCOPE");

		var data = chartServiceImpl.getDynamicData(params);
		assertNotNull(data);

		// Assert we have exactly one series
		var series = data.getSeries();
		assertEquals(1, series.size());

		var chartSeries = series.getFirst();
		assertEquals("ENROLLMENT_BY_CENTER", chartSeries.getLabel());

		var values = chartSeries.getValues();
		assertEquals(3, values.size());

		// Assert specific entries exist
		assertTrue(values.contains(List.of("AT-01", 1)));
		assertTrue(values.contains(List.of("FR-01", 2)));
		assertTrue(values.contains(List.of("FR-02", 1)));
	}

	@Test
	void testEnrollmentStatusChart() {
		var params = new HashMap<String, Object>();
		params.put("leafScopeModelId", "PATIENT");
		params.put("chartType", "ENROLLMENT_STATUS");

		var data = chartServiceImpl.getDynamicData(params);
		assertNotNull(data);

		// We have again exactly one series
		var series = data.getSeries();
		assertEquals(1, series.size());

		var chartSeries = series.getFirst();
		assertEquals("ENROLLMENT_STATUS", chartSeries.getLabel());

		// Since the Test Database initializes with 4 patients, we will have 5 entries
		// This is because the "zero" point is added in the beginning + one for each patient
		var values = chartSeries.getValues();
		assertEquals(5, values.size());
	}

	@Test
	void testStatisticsChart() {
		var params = new HashMap<String, Object>();
		params.put("leafScopeModelId", "PATIENT");
		params.put("chartType", "STATISTICS");
		params.put("scopeModelId", "STUDY");
		params.put("datasetModelId", "PATIENT_DOCUMENTATION");
		params.put("fieldModelId", "GENDER");

		var data = chartServiceImpl.getDynamicData(params);
		System.out.println(data.toPrettyString(""));
		assertNotNull(data);

		// We have exactly one series
		var series = data.getSeries();
		assertEquals(1, series.size());

		var chartSeries = series.getFirst();
		assertEquals("TEST", chartSeries.getLabel());

		// In the initialized DB we have at the beginning one male,
		// one female and two patients with no gender specified
		var values = chartSeries.getValues();
		assertTrue(values.contains(List.of("MALE", 1)));
		assertTrue(values.contains(List.of("FEMALE", 1)));
		assertTrue(values.contains(List.of("", 2)));
	}

	@Test
	void testStatisticsChartWithCategories() {
		var params = new HashMap<String, Object>();
		params.put("leafScopeModelId", "PATIENT");
		params.put("chartType", "STATISTICS");
		params.put("scopeModelId", "STUDY");
		params.put("datasetModelId", "PATIENT_DOCUMENTATION");
		params.put("fieldModelId", "AGE_AT_DIAGNOSIS");

		// Define categories
		var categories = List.of(
			Map.of("label", "0-18", "min", "0", "max", "18", "show", true),
			Map.of("label", "19-40", "min", "19", "max", "40", "show", true),
			Map.of("label", "41+", "min", "41", "max", "200", "show", true)
		);
		params.put("categories", categories);
		params.put("showOtherCategory", false);

		var data = chartServiceImpl.getDynamicData(params);
		System.out.println(data.toPrettyString(""));
		assertNotNull(data);

		// Check that the categories are there
		// Because the MS Form is not saved in the beginning and thus DATE_OF_DIAGNOSIS is not set
		// all the categories have no patients yet
		var values = data.getSeries().getFirst().getValues();
		assertTrue(values.contains(List.of("0-18", 0)));
		assertTrue(values.contains(List.of("19-40", 0)));
		assertTrue(values.contains(List.of("41+", 0)));

		// Check that the "Other" category is present when specified
		// It will have value 4 because in the beginning all 4 patients have "N/A" as AGE_AT_DIAGNOSIS
		params.put("showOtherCategory", true);
		data = chartServiceImpl.getDynamicData(params);
		System.out.println(data.toPrettyString(""));
		values = data.getSeries().getFirst().getValues();
		assertTrue(values.contains(List.of("Other", 4)));
	}

	@Test
	void testWorkflowStatusChart() {
		var params = new HashMap<String, Object>();
		params.put("chartType", "WORKFLOW_STATUS");
		params.put("workflowId", "SIGNATURE");
		params.put("stateIds", List.of("SIGNED", "UNSIGNED"));

		var data = chartServiceImpl.getDynamicData(params);
		System.out.println(data.toPrettyString(""));
		assertNotNull(data);

		// We have two series for every state
		var series = data.getSeries();
		assertEquals(2, series.size());
	}
}