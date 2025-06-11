package ch.rodano.core.database.initializer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.rodano.core.services.dao.chart.ChartDTO;
import ch.rodano.core.services.dao.chart.ChartServiceImpl;
import ch.rodano.core.services.dao.chart.ChartType;

@Service
public class TestChartsInitializer {

	private final ChartServiceImpl chartServiceImpl;

	@Autowired
	public TestChartsInitializer(
		final ChartServiceImpl chartServiceImpl
	) {
		this.chartServiceImpl = chartServiceImpl;
	}

	public void initializeCharts() {
		createEnrollmentStatusChart();
		createEnrollmentByCenterChart();
		createEnrollmentOfCentersChart();
		createGenderChart();
		createAgeAtDiagnosisChart();
		createEdssAtBaselineChart();
		createEdssProgressionChart();
	}

	private void createEnrollmentStatusChart() {
		final var chart = new ChartDTO();

		chart.setChartId("ENROLLMENT");
		chart.setChartType(ChartType.ENROLLMENT_STATUS);
		chart.setTitle("Enrollment Status");
		chart.setxLabel("");
		chart.setyLabel("Number of Patients");

		// Set chart config
		final var config = new ChartDTO.ChartConfig();
		config.setGraphType("line");
		config.setUnitFormat("absolute");
		config.setIgnoreNA(false);
		config.setShowYAxisLabel(true);
		config.setShowXAxisLabel(true);
		config.setShowDataLabels(true);
		config.setDataLabelPos("end");
		config.setDataLabelFormat("y value only");
		config.setShowLegend(false);
		config.setShowGridlines(true);
		config.setBackgroundColor("#ffffff");
		config.setHeaderColor("#000000");
		config.setColors(List.of("#F0BB78", "#F5ECD5", "#A4B465"));

		chart.setChartConfig(config);

		// Set request parameters
		final var params = new ChartDTO.RequestParams();
		params.setScopeModelId(null);
		params.setLeafScopeModelId("PATIENT");
		params.setDatasetModelId(null);
		params.setFieldModelId(null);
		params.setEventModelId(null);
		params.setStateIds(List.of());
		params.setWorkflowId(null);
		params.setCategories(List.of());
		params.setShowOtherCategory(false);

		chart.setRequestParams(params);

		// Save the chart
		chartServiceImpl.createChart(chart);
	}

	private void createEnrollmentByCenterChart() {
		final var chart = new ChartDTO();

		chart.setChartId("ENROLLMENT_BY_CENTER");
		chart.setChartType(ChartType.ENROLLMENT_BY_SCOPE);
		chart.setTitle("Enrollment by Center");
		chart.setxLabel("Centers");
		chart.setyLabel("Number of Patients");

		// Set chart config
		final var config = new ChartDTO.ChartConfig();
		config.setGraphType("bar");
		config.setUnitFormat("absolute");
		config.setIgnoreNA(false);
		config.setShowYAxisLabel(true);
		config.setShowXAxisLabel(true);
		config.setShowDataLabels(true);
		config.setDataLabelPos("end");
		config.setDataLabelFormat("y value only");
		config.setShowLegend(false);
		config.setShowGridlines(true);
		config.setBackgroundColor("#ffffff");
		config.setHeaderColor("#000000");
		config.setColors(List.of("#F0BB78", "#F5ECD5", "#A4B465"));

		chart.setChartConfig(config);

		// Set request parameters
		final var params = new ChartDTO.RequestParams();
		params.setScopeModelId("CENTER");
		params.setLeafScopeModelId("PATIENT");
		params.setDatasetModelId(null);
		params.setFieldModelId(null);
		params.setEventModelId(null);
		params.setStateIds(List.of()); // empty list
		params.setWorkflowId(null);
		params.setCategories(List.of()); // empty list
		params.setShowOtherCategory(false);

		chart.setRequestParams(params);

		// Save the chart (data will be fetched dynamically when needed)
		chartServiceImpl.createChart(chart);
	}

	private void createEnrollmentOfCentersChart() {
		final var chart = new ChartDTO();

		chart.setChartId("ENROLLMENT_OF_CENTERS");
		chart.setChartType(ChartType.WORKFLOW_STATUS);
		chart.setTitle("Center Status Workflow");
		chart.setxLabel("");
		chart.setyLabel("");

		// Set chart config
		final var config = new ChartDTO.ChartConfig();
		config.setGraphType("line");
		config.setUnitFormat("absolute");
		config.setIgnoreNA(false);
		config.setShowYAxisLabel(true);
		config.setShowXAxisLabel(true);
		config.setShowDataLabels(true);
		config.setDataLabelPos("end");
		config.setDataLabelFormat("y value only");
		config.setShowLegend(false);
		config.setShowGridlines(true);
		config.setBackgroundColor("#ffffff");
		config.setHeaderColor("#000000");
		config.setColors(List.of("#F0BB78", "#F5ECD5", "#A4B465"));

		chart.setChartConfig(config);

		// Set request parameters
		final var params = new ChartDTO.RequestParams();
		params.setScopeModelId(null);
		params.setLeafScopeModelId(null);
		params.setDatasetModelId(null);
		params.setFieldModelId(null);
		params.setEventModelId(null);
		params.setStateIds(List.of());
		params.setWorkflowId("CENTER_STATUS");
		params.setCategories(List.of());
		params.setShowOtherCategory(true);

		chart.setRequestParams(params);

		// Save the chart
		chartServiceImpl.createChart(chart);
	}

	private void createGenderChart() {
		final var chart = new ChartDTO();

		chart.setChartId("GENDER");
		chart.setChartType(ChartType.STATISTICS);
		chart.setTitle("Distribution of Gender");
		chart.setxLabel("");
		chart.setyLabel("");

		// Set chart config
		final var config = new ChartDTO.ChartConfig();
		config.setGraphType("doughnut");
		config.setUnitFormat("percentage");
		config.setIgnoreNA(false);
		config.setShowYAxisLabel(true);
		config.setShowXAxisLabel(true);
		config.setShowDataLabels(true);
		config.setDataLabelPos("center");
		config.setDataLabelFormat("x and y value");
		config.setShowLegend(false);
		config.setShowGridlines(false);
		config.setBackgroundColor("#ffffff");
		config.setHeaderColor("#000000");
		config.setColors(List.of("#F564A9", "#8DD8FF", "#A4B465"));

		chart.setChartConfig(config);

		// Set request parameters
		final var params = new ChartDTO.RequestParams();
		params.setScopeModelId("STUDY");
		params.setLeafScopeModelId("PATIENT");
		params.setDatasetModelId("PATIENT_DOCUMENTATION");
		params.setFieldModelId("GENDER");
		params.setEventModelId(null);
		params.setStateIds(List.of());
		params.setWorkflowId(null);
		params.setCategories(List.of());
		params.setShowOtherCategory(false);

		chart.setRequestParams(params);

		// Save the chart
		chartServiceImpl.createChart(chart);
	}

	private void createAgeAtDiagnosisChart() {
		final var chart = new ChartDTO();

		chart.setChartId("AGE_AT_DIAGNOSIS");
		chart.setChartType(ChartType.STATISTICS);
		chart.setTitle("Age at diagnosis");
		chart.setxLabel("");
		chart.setyLabel("");

		// Set chart config
		final var config = new ChartDTO.ChartConfig();
		config.setGraphType("bar");
		config.setUnitFormat("absolute");
		config.setIgnoreNA(false);
		config.setShowYAxisLabel(true);
		config.setShowXAxisLabel(true);
		config.setShowDataLabels(true);
		config.setDataLabelPos("center");
		config.setDataLabelFormat("x and y value");
		config.setShowLegend(false);
		config.setShowGridlines(false);
		config.setBackgroundColor("#ffffff");
		config.setHeaderColor("#000000");
		config.setColors(List.of("#F0BB78", "#F5ECD5", "#A4B465"));

		chart.setChartConfig(config);

		// Set request parameters
		final var params = new ChartDTO.RequestParams();
		params.setScopeModelId("STUDY");
		params.setLeafScopeModelId("PATIENT");
		params.setDatasetModelId("PATIENT_DOCUMENTATION");
		params.setFieldModelId("AGE_AT_DIAGNOSIS");
		params.setEventModelId(null);
		params.setStateIds(List.of());
		params.setWorkflowId(null);
		params.setShowOtherCategory(false);

		// Add categories
		final var young = new ChartDTO.Category("0-18", "0", "18", true);
		final var middle = new ChartDTO.Category("19-40", "19", "40", true);
		final var old = new ChartDTO.Category("41+", "41", "999", true);

		params.setCategories(List.of(young, middle, old));

		chart.setRequestParams(params);

		// Save the chart
		chartServiceImpl.createChart(chart);
	}

	private void createEdssAtBaselineChart() {
		final var chart = new ChartDTO();

		chart.setChartId("EDSS_AT_BASELINE");
		chart.setChartType(ChartType.STATISTICS);
		chart.setTitle("EDSS at Baseline");
		chart.setxLabel("EDSS Score");
		chart.setyLabel("Number of Patients");

		// Set chart config
		final var config = new ChartDTO.ChartConfig();
		config.setGraphType("bar");
		config.setUnitFormat("absolute");
		config.setIgnoreNA(true);
		config.setShowYAxisLabel(true);
		config.setShowXAxisLabel(true);
		config.setShowDataLabels(true);
		config.setDataLabelPos("end");
		config.setDataLabelFormat("y value only");
		config.setShowLegend(false);
		config.setShowGridlines(true);
		config.setBackgroundColor("#ffffff");
		config.setHeaderColor("#000000");
		config.setColors(List.of("#F0BB78", "#F5ECD5", "#A4B465"));

		chart.setChartConfig(config);

		// Set request parameters
		final var params = new ChartDTO.RequestParams();
		params.setScopeModelId("STUDY");
		params.setLeafScopeModelId("PATIENT");
		params.setDatasetModelId("VISIT_DOCUMENTATION");
		params.setFieldModelId("EDSS_SCORE");
		params.setEventModelId("BASELINE");
		params.setStateIds(List.of());
		params.setWorkflowId(null);
		params.setShowOtherCategory(false);

		// Add categories
		final var cat1 = new ChartDTO.Category("0.5", "0.5", "0.5", true);
		final var cat2 = new ChartDTO.Category("1.0", "1.0", "1.0", true);
		final var cat3 = new ChartDTO.Category("1.5", "1.5", "1.5", true);
		final var cat4 = new ChartDTO.Category("2.0", "2.0", "2.0", true);
		final var cat5 = new ChartDTO.Category("2.5", "2.5", "2.5", true);
		final var cat6 = new ChartDTO.Category("3.0", "3.0", "3.0", true);
		final var cat7 = new ChartDTO.Category("3.5", "3.5", "3.5", true);
		final var cat8 = new ChartDTO.Category("4.0", "4.0", "4.0", true);
		final var cat9 = new ChartDTO.Category("4.5", "4.5", "4.5", true);
		final var cat10 = new ChartDTO.Category("5.0", "5.0", "5.0", true);
		final var cat11 = new ChartDTO.Category("5.5", "5.5", "5.5", true);
		final var cat12 = new ChartDTO.Category("6.0", "6.0", "6.0", true);
		final var cat13 = new ChartDTO.Category("6.5", "6.5", "6.5", true);
		final var cat14 = new ChartDTO.Category("7.0", "7.0", "7.0", true);
		final var cat15 = new ChartDTO.Category("7.5", "7.5", "7.5", true);
		final var cat16 = new ChartDTO.Category("8.0", "8.0", "8.0", true);
		final var cat17 = new ChartDTO.Category("8.5", "8.5", "8.5", true);
		final var cat18 = new ChartDTO.Category("9.0", "9.0", "9.0", true);
		final var cat19 = new ChartDTO.Category("9.5", "9.5", "9.5", true);
		final var cat20 = new ChartDTO.Category("10.0", "10.0", "10.0", true);

		params.setCategories(
			List.of(
				cat1, cat2, cat3, cat4, cat5, cat6, cat7, cat8, cat9, cat10,
				cat11, cat12, cat13, cat14, cat15, cat16, cat17, cat18, cat19, cat20
			));

		chart.setRequestParams(params);

		// Save the chart
		chartServiceImpl.createChart(chart);
	}

	private void createEdssProgressionChart() {
		final var chart = new ChartDTO();

		chart.setChartId("EDSS_PROGRESSION");
		chart.setChartType(ChartType.STATISTICS);
		chart.setTitle("EDSS Progression");
		chart.setxLabel("");
		chart.setyLabel("");

		// Set chart config
		final var config = new ChartDTO.ChartConfig();
		config.setGraphType("bar");
		config.setUnitFormat("absolute");
		config.setIgnoreNA(false);
		config.setShowYAxisLabel(true);
		config.setShowXAxisLabel(true);
		config.setShowDataLabels(true);
		config.setDataLabelPos("end");
		config.setDataLabelFormat("y value only");
		config.setShowLegend(false);
		config.setShowGridlines(false);
		config.setBackgroundColor("#ffffff");
		config.setHeaderColor("#000000");
		config.setColors(List.of("#F0BB78", "#F5ECD5", "#A4B465"));

		chart.setChartConfig(config);

		// Set request parameters
		final var params = new ChartDTO.RequestParams();
		params.setScopeModelId("STUDY");
		params.setLeafScopeModelId("PATIENT");
		params.setDatasetModelId("VISIT_DOCUMENTATION");
		params.setFieldModelId("WEIGHT");
		params.setEventModelId("BASELINE");
		params.setStateIds(List.of());
		params.setWorkflowId(null);
		params.setCategories(List.of());
		params.setShowOtherCategory(false);

		chart.setRequestParams(params);

		// Save the chart
		chartServiceImpl.createChart(chart);
	}
}
