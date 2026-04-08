import {DatabaseIssueType} from '@core/model/database-issue-type';

export interface DatabaseIssueTypeDisplay {
	label: string;
	tooltip: string;
}

export const DATABASE_ISSUE_TYPE_DISPLAY: Record<DatabaseIssueType, DatabaseIssueTypeDisplay> = {
	MISSING_IN_DATABASE: {
		label: 'Missing in database',
		tooltip: 'Entity does not exist in the database but is required by the configuration'
	},
	MISSING_IN_CONFIGURATION: {
		label: 'Missing in configuration',
		tooltip: 'Entity exists in the database but is not defined in the configuration'
	}
};

export function getDatabaseIssueTypeDisplay(type: DatabaseIssueType): DatabaseIssueTypeDisplay {
	return DATABASE_ISSUE_TYPE_DISPLAY[type];
}
