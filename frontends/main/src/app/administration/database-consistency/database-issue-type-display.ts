import {DatabaseIssueType} from '@core/model/database-issue-type';

export interface DatabaseIssueTypeDisplay {
	label: string;
}

export const DATABASE_ISSUE_TYPE_DISPLAY: Record<DatabaseIssueType, DatabaseIssueTypeDisplay> = {
	MISSING_IN_DATABASE: {
		label: 'Missing in database'
	},
	MISSING_IN_CONFIGURATION: {
		label: 'Missing in configuration'
	}
};

export function getDatabaseIssueTypeDisplay(type: DatabaseIssueType): DatabaseIssueTypeDisplay {
	return DATABASE_ISSUE_TYPE_DISPLAY[type];
}
