import {DatabaseIssueStatus} from '@core/model/database-issue-status';

export interface DatabaseIssueDisplay {
	icon: string;
	status: string;
}

export const DATABASE_ISSUE_STATUS_DISPLAY: Record<DatabaseIssueStatus, DatabaseIssueDisplay> = {
	FIXABLE: {
		icon: 'check_circle',
		status: 'Fixable'
	},
	FIXED: {
		icon: 'no_accounts',
		status: 'Fixed'
	},
	NOT_FIXABLE: {
		icon: 'error',
		status: 'Not fixable'
	}
};

//This function is used to get the display value for a given database issue status
//it ease the use of the DATABASE_ISSUE_STATUS_DISPLAY object in the templates
export function getDatabaseIssueStatusDisplay(status: DatabaseIssueStatus): DatabaseIssueDisplay {
	return DATABASE_ISSUE_STATUS_DISPLAY[status];
}
