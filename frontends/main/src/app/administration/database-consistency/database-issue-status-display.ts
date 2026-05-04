import {DatabaseIssueStatus} from '@core/model/database-issue-status';

export interface DatabaseIssueDisplay {
	label: string;
	tooltip: string;
}

export const DATABASE_ISSUE_STATUS_DISPLAY: Record<DatabaseIssueStatus, DatabaseIssueDisplay> = {
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

//This function is used to get the display value for a given database issue status
//it ease the use of the DATABASE_ISSUE_STATUS_DISPLAY object in the templates
export function getDatabaseIssueStatusDisplay(status: DatabaseIssueStatus): DatabaseIssueDisplay {
	return DATABASE_ISSUE_STATUS_DISPLAY[status];
}
