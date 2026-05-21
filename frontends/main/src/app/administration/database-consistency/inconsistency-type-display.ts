import {ConfigurationInconsistencyType} from '@core/model/configuration-inconsistency-type';

export interface InconsistencyTypeDisplay {
	label: string;
	tooltip: string;
}

export const INCONSISTENCY_TYPE_DISPLAY: Record<ConfigurationInconsistencyType, InconsistencyTypeDisplay> = {
	MISSING_IN_DATABASE: {
		label: 'Missing in database',
		tooltip: 'Entity does not exist in the database but is required by the configuration'
	},
	MISSING_IN_CONFIGURATION: {
		label: 'Missing in configuration',
		tooltip: 'Entity exists in the database but is not defined in the configuration'
	}
};

export function getInconsistencyTypeDisplay(type: ConfigurationInconsistencyType): InconsistencyTypeDisplay {
	return INCONSISTENCY_TYPE_DISPLAY[type];
}
