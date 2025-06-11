/**
	* Converts camelCase or snake_case keys into Title Case
	* @param key
	* @private
	*/
export function formatKey(key: string): string {
	return key
		.replace(/([A-Z])/g, ' $1')
		.replace(/[_-]/g, ' ')
		.replace(/\b\w/g, char => char.toUpperCase())
		.trim();
}

/**
	* Mapping for object parameters inside the configuration. Hold user-friendly strings.
	*/
export const fieldsMetadata: Record<string, string> = {
	//Chart Configuration
	chartId: 'Chart ID',
	chart_type: 'Chart Type',
	title: 'Title',
	xLabel: 'X-Label',
	yLabel: 'Y-Label',
	chartConfig: 'Visual Configuration',
	graph_type: 'Graph Type',
	unitFormat: 'Unit Format',
	showYAxisLabel: 'Show Y-Axis Label',
	showXAxisLabel: 'Show X-Axis Label',
	showDataLabels: 'Show Data Labels',
	dataLabelPos: 'Data Label Position',
	dataLabelFormat: 'Data Label Format',
	showLegend: 'Show Legend',
	showGridlines: 'Show Gridlines',
	backgroundColor: 'Background Color',
	headerColor: 'Header Color',
	colors: 'Color Palette',
	requestParams: 'Request Parameters',
	leafScopeModelId: 'Leaf Scope Model ID',
	scopeModelId: 'Scope Model ID',
	datasetModelId: 'Dataset Model ID',
	fieldModelId: 'Field Model ID',
	eventModelId: 'Event Model ID',
	showOtherCategories: 'Show Other Category',
	categories: 'Categories'
};

export const singularFieldLabels: Record<string, string> = {
	categories: 'Category'
};

/**
	* Fields that should support "No selection" as a valid value.
	*/
export const nullableFields = new Set<string>([
	'leafScopeModelId',
	'scopeModelId',
	'datasetModelId',
	'fieldModelId',
	'workflowId',
	'eventModelId'
]);

/**
	* Returns a defined label for an object parameter. If there is none defined yet it returns a fallback in Title Case
	* @param key
	*/
export function getLabel(key: string): string {
	return fieldsMetadata[key] || formatKey(key);
}

export function getSingularLabel(key: string): string {
	return singularFieldLabels[key] || key;
}

/**
	* Returns true if a field should display a "No selection" option.
	* @param key
	*/
export function isNullableField(key: string): boolean {
	return nullableFields.has(key);
}

export const objectArrayTemplates: Record<string, any> = {
	categories: {
		label: '',
		min: null,
		max: null,
		show: true
	}
};

/**
	* Returns the default object template for a given key.
	* Falls back to an empty object if no template is found.
	* @param key
	*/
export function getObjectTemplate(key: string): any {
	return objectArrayTemplates[key] ?? {};
}
