import {ChartType, DataLabelFormats, DataLabelPositions, GraphType, UnitFormats} from '../chartType';
import {ChartDTO} from '@core/model/chart-dto';

const enrollmentByCenterChart: ChartDTO = {
	chartId: 'ENROLLMENT_BY_CENTER',
	chartType: ChartType.ENROLLMENT_BY_SCOPE,
	title: 'Enrollment by Scope Chart',
	xLabel: '',
	yLabel: 'Number of Patients',
	chartConfig: {
		graphType: GraphType.BAR,
		unitFormat: UnitFormats.ABSOLUTE,
		ignoreNA: false,
		showYAxisLabel: true,
		showXAxisLabel: true,
		showDataLabels: true,
		dataLabelPos: DataLabelPositions.CENTER,
		dataLabelFormat: DataLabelFormats.ONLY_Y,
		showLegend: false,
		showGridlines: true,
		backgroundColor: '#ffffff',
		headerColor: '#000000',
		colors: ['#003f5c', '#58508d', '#8a508f', '#bc5090', '#de5a79', '#ff6361', '#ff8531', '#ffa600']
	},
	requestParams: {
		stateIds: ['WITHDRAWN', 'ONGOING'],
		workflowId: 'ENROLLMENT_STATUS',
		leafScopeModelId: 'PATIENT',
		datasetModelId: null,
		fieldModelId: null,
		eventModelId: null,
		scopeModelId: 'CENTER',
		showOtherCategory: null,
		ignoreUserRights: false,
		categories: null
	},
	data: {
		series: [{
			label: 'CENTER',
			values: [
				['AT-01', 180],
				['AT-02', 120],
				['FR-01', 100],
				['FR-02', 80],
				['DE-01', 60],
				['DE-02', 40]
			]
		}]
	}
};

const enrollmentByDateChart: ChartDTO = {
	chartId: 'ENROLLMENT',
	chartType: ChartType.ENROLLMENT_STATUS,
	title: 'Enrollment Status Chart',
	xLabel: '',
	yLabel: 'Number of Patients',
	chartConfig: {
		graphType: GraphType.LINE,
		unitFormat: UnitFormats.ABSOLUTE,
		ignoreNA: false,
		showXAxisLabel: true,
		showYAxisLabel: true,
		showDataLabels: false,
		dataLabelPos: DataLabelPositions.CENTER,
		dataLabelFormat: DataLabelFormats.ONLY_Y,
		showLegend: false,
		showGridlines: true,
		backgroundColor: '#ffffff',
		headerColor: '#000000',
		colors: ['#003f5c']
	},
	requestParams: {
		stateIds: null,
		workflowId: null,
		leafScopeModelId: null,
		datasetModelId: null,
		fieldModelId: null,
		eventModelId: null,
		scopeModelId: 'PATIENT',
		showOtherCategory: null,
		ignoreUserRights: false,
		categories: null
	},
	data: {
		series: [
			{
				label: 'TEST',
				values: [
					[1360665094000, 0],
					[1376388011000, 37],
					[1392211508000, 59],
					[1407920829000, 66],
					[1424365225000, 75],
					[1439387667000, 88],
					[1455706179000, 100]
				]
			}
		]
	}
};

const statisticsGenderChart: ChartDTO = {
	chartId: 'STATISTICS_GENDER',
	chartType: ChartType.STATISTICS,
	title: 'Statistics Chart',
	xLabel: 'Gender',
	yLabel: 'Number of Patients (%)',
	chartConfig: {
		graphType: GraphType.DOUGHNUT,
		unitFormat: UnitFormats.PERCENTAGE,
		ignoreNA: false,
		showXAxisLabel: true,
		showYAxisLabel: true,
		showDataLabels: true,
		dataLabelPos: DataLabelPositions.CENTER,
		dataLabelFormat: DataLabelFormats.X_AND_Y,
		showLegend: true,
		showGridlines: false,
		backgroundColor: '#ffffff',
		headerColor: '#000000',
		colors: ['#ff0800', '#6a98af']
	},
	requestParams: {
		stateIds: null,
		workflowId: 'ENROLLMENT_STATUS',
		leafScopeModelId: 'PATIENT',
		datasetModelId: 'PATIENT_DOCUMENTATION',
		fieldModelId: 'GENDER',
		eventModelId: null,
		scopeModelId: 'PATIENT',
		showOtherCategory: false,
		ignoreUserRights: false,
		categories: []
	},
	data: {
		series: [
			{
				label: 'TEST',
				values: [
					['FEMALE', 60],
					['MALE', 40]
				]
			}
		]
	}
};

const workflowStatusChart: ChartDTO = {
	chartId: 'WORKFLOW_STATUS',
	chartType: ChartType.WORKFLOW_STATUS,
	title: 'Workflow Status Chart',
	xLabel: '',
	yLabel: 'Number of Patients',
	chartConfig: {
		graphType: GraphType.LINE,
		unitFormat: UnitFormats.ABSOLUTE,
		ignoreNA: false,
		showXAxisLabel: true,
		showYAxisLabel: true,
		showDataLabels: false,
		dataLabelPos: DataLabelPositions.START,
		dataLabelFormat: DataLabelFormats.ONLY_Y,
		showLegend: false,
		showGridlines: true,
		backgroundColor: '#ffffff',
		headerColor: '#000000',
		colors: ['#003f5c']
	},
	requestParams: {
		stateIds: [],
		workflowId: 'CENTER_STATUS',
		leafScopeModelId: null,
		datasetModelId: null,
		fieldModelId: null,
		eventModelId: null,
		scopeModelId: null,
		showOtherCategory: null,
		ignoreUserRights: false,
		categories: null
	},
	data: {
		series: [
			{
				label: 'TEST',
				values: [
					[1360665094000, 0],
					[1376388011000, 37],
					[1392211508000, 59],
					[1424365225000, 75],
					[1471248837000, 108],
					[1487172384000, 112],
					[1518533197000, 120]
				]
			}
		]
	}
};

export const charts: ChartDTO[] = [
	enrollmentByDateChart,
	enrollmentByCenterChart,
	statisticsGenderChart,
	workflowStatusChart
];
