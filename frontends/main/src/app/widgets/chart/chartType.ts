export enum ChartType {
	STATISTICS = 'STATISTICS',
	ENROLLMENT_BY_SCOPE = 'ENROLLMENT_BY_SCOPE',
	ENROLLMENT_STATUS = 'ENROLLMENT_STATUS',
	WORKFLOW_STATUS = 'WORKFLOW_STATUS'
}

export enum RequestParameters {
	LEAF_SCOPE_MODEL_ID = 'leafScopeModelId',
	SCOPE_MODEL_ID = 'scopeModelId',
	FIELD_MODEL_ID = 'fieldModelId',
	DATASET_MODEL_ID = 'datasetModelId',
	EVENT_MODEL_ID = 'eventModelId',
	WORKFLOW_ID = 'workflowId',
	STATE_IDS = 'stateIds',
	SHOW_OTHER_CATEGORIES = 'showOtherCategory',
	IGNORE_USER_RIGHTS = 'ignoreUserRights',
	CATEGORIES = 'categories'
}

export interface Category {
	label: string;
	min: string | null;
	max: string | null;
	show: boolean;
}

export interface RequestParams {
	scopeModelId: string | null;
	leafScopeModelId: string | null;
	datasetModelId: string | null;
	fieldModelId: string | null;
	eventModelId: string | null;
	stateIds: string[] | null;
	workflowId: string | null;
	showOtherCategory: boolean | null;
	ignoreUserRights: boolean | null;
	categories: Category[] | null;
}

export enum GraphType {
	BAR = 'bar',
	HORIZONTAL_BAR = 'horizontalBar',
	PIE = 'pie',
	DOUGHNUT = 'doughnut',
	RADAR = 'radar',
	POLAR_AREA = 'polarArea',
	LINE = 'line',
	AREA = 'area'
}

/**
	* Mapping from chart_type to graph_type -
	* Inside the configuration section, the user should only be able to select possible graph_types based on the chart_type
	* he selected
	*/
export const CHART_TYPE_TO_GRAPH_TYPE: Record<ChartType, GraphType[]> = {
	[ChartType.ENROLLMENT_STATUS]: [GraphType.LINE, GraphType.AREA],
	[ChartType.ENROLLMENT_BY_SCOPE]: [GraphType.BAR, GraphType.HORIZONTAL_BAR, GraphType.PIE, GraphType.DOUGHNUT, GraphType.RADAR, GraphType.POLAR_AREA],
	[ChartType.STATISTICS]: [GraphType.BAR, GraphType.HORIZONTAL_BAR, GraphType.PIE, GraphType.DOUGHNUT, GraphType.RADAR, GraphType.POLAR_AREA],
	[ChartType.WORKFLOW_STATUS]: [GraphType.LINE, GraphType.AREA]
};

/**
	* Mapping from chart_type to requestParameters
	* Since the modifiable request parameters change based on the currently selected chart_type we create this mapping
	* which allows dynamically rendering the correct ones.
	*/
export const CHART_TYPE_TO_REQUEST_PARAMS: Record<ChartType, RequestParameters[]> = {
	[ChartType.ENROLLMENT_BY_SCOPE]: [
		RequestParameters.LEAF_SCOPE_MODEL_ID,
		RequestParameters.SCOPE_MODEL_ID,
		RequestParameters.IGNORE_USER_RIGHTS
	],
	[ChartType.ENROLLMENT_STATUS]: [
		RequestParameters.LEAF_SCOPE_MODEL_ID,
		RequestParameters.IGNORE_USER_RIGHTS
	],
	[ChartType.STATISTICS]: [
		RequestParameters.SCOPE_MODEL_ID,
		RequestParameters.LEAF_SCOPE_MODEL_ID,
		RequestParameters.DATASET_MODEL_ID,
		RequestParameters.FIELD_MODEL_ID,
		RequestParameters.EVENT_MODEL_ID,
		RequestParameters.SHOW_OTHER_CATEGORIES,
		RequestParameters.IGNORE_USER_RIGHTS,
		RequestParameters.CATEGORIES
	],
	[ChartType.WORKFLOW_STATUS]: [
		RequestParameters.WORKFLOW_ID,
		RequestParameters.STATE_IDS,
		RequestParameters.IGNORE_USER_RIGHTS
	]
};

export enum UnitFormats {
	ABSOLUTE = 'absolute',
	PERCENTAGE = 'percentage'
}

export enum DataLabelPositions {
	START = 'start',
	CENTER = 'center',
	END = 'end'
}

export enum DataLabelFormats {
	ONLY_X = 'x value only',
	ONLY_Y = 'y value only',
	X_AND_Y = 'x and y value'
}
