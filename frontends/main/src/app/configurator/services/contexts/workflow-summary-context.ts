import {WorkflowSummaryManagerService} from '../manager/workflow-summary-manager.service';
import {WorkflowSummary} from '@core/model/workflow-summary';

export interface WorkflowSummaryContext {
	workflowSummaryManager: WorkflowSummaryManagerService;
	workflowSummaries: WorkflowSummary[];
	originalWorkflowSummaries: WorkflowSummary[];
	modifiedWorkflowSummaryIds: Set<string>;
}
