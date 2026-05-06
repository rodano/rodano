import {InconsistencyStatus} from '@core/model/inconsistency-status';

export interface InconsistencyStatusDisplay {
	label: string;
	tooltip: string;
}

export const INCONSISTENCY_STATUS_DISPLAY: Record<InconsistencyStatus, InconsistencyStatusDisplay> = {
	FIXABLE: {
		label: 'Fixable',
		tooltip: 'Issue detected but not fixed (dry run)'
	},
	FIXED: {
		label: 'Fixed',
		tooltip: 'Issue has been fixed'
	},
	NOT_FIXABLE: {
		label: 'Not fixable',
		tooltip: 'Issue cannot be automatically fixed'
	}
};

export function getInconsistencyStatusDisplay(status: InconsistencyStatus): InconsistencyStatusDisplay {
	return INCONSISTENCY_STATUS_DISPLAY[status];
}
