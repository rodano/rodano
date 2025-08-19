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
